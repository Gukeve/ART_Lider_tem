package com.artleader.mvp.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.local.entity.ChatEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.MessengerUserEntity
import com.artleader.mvp.data.repository.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Relay-ready packet model (foundation for mesh forwarding).
 */
data class MeshPacket(
    val packetId: String,
    val sourcePeerId: String,
    val targetPeerId: String?,
    val hopPeerId: String,
    val timestamp: Long,
    val ttl: Int,
    val encryptedPayload: String,
    val deliveryState: String
)

data class MessengerUiState(
    val bluetoothEnabled: Boolean = false,
    val connectionState: String = "Bluetooth выключен",
    val selectedChatId: String? = null,
    val devices: List<BluetoothDevice> = emptyList(),
    val knownPeers: List<MessengerUserEntity> = emptyList(),
    val relayQueueSize: Int = 0,
    val error: String? = null
)

class MessengerViewModel(private val repository: BluetoothRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MessengerUiState(bluetoothEnabled = repository.isEnabled()))
    val ui: StateFlow<MessengerUiState> = _ui.asStateFlow()

    val chats = repository.observeChats().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val users = repository.observeUsers().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val messages: StateFlow<List<MessageEntity>> = ui.flatMapLatest { state ->
        repository.messages(state.selectedChatId ?: "")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val seenPacketIds = mutableSetOf<String>()
    private val forwardQueue = ArrayDeque<MeshPacket>()
    private var socket: BluetoothSocket? = null

    init {
        viewModelScope.launch { repository.seedUsers() }
        viewModelScope.launch {
            users.collect { peerList ->
                _ui.value = _ui.value.copy(knownPeers = peerList)
            }
        }
    }

    fun refreshDevices() {
        val enabled = repository.isEnabled()
        val bonded = repository.bondedDevices()
        _ui.value = _ui.value.copy(
            bluetoothEnabled = enabled,
            connectionState = if (enabled) "Поиск устройств" else "Bluetooth выключен",
            devices = bonded,
            error = null
        )
    }

    fun openPrivateChat(user: MessengerUserEntity) = viewModelScope.launch {
        val chatId = repository.createPrivateChat(user)
        _ui.value = _ui.value.copy(selectedChatId = chatId)
    }

    fun createGroup(name: String, members: List<MessengerUserEntity>) = viewModelScope.launch {
        val chatId = repository.createGroupChat(name, members.map { it.userId })
        _ui.value = _ui.value.copy(selectedChatId = chatId)
    }

    fun selectChat(chat: ChatEntity) {
        _ui.value = _ui.value.copy(selectedChatId = chat.chatId)
    }

    fun connect(device: BluetoothDevice) = connectAsClient(device)

    fun connectAsClient(device: BluetoothDevice) = viewModelScope.launch {
        runCatching {
            socket = repository.connect(device)
            _ui.value = _ui.value.copy(connectionState = "Подключено: ${device.name ?: device.address}")
            startListeningRelay()
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка подключения") }
    }

    fun waitForIncoming() = hostSession()

    fun hostSession() = viewModelScope.launch {
        runCatching {
            _ui.value = _ui.value.copy(connectionState = "Ожидание входящего подключения")
            socket = repository.accept()
            _ui.value = _ui.value.copy(connectionState = "Входящее подключение установлено")
            startListeningRelay()
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка ожидания входящего подключения") }
    }

    fun send(text: String) = viewModelScope.launch {
        val chatId = _ui.value.selectedChatId ?: return@launch
        runCatching {
            repository.saveMessage(chatId, text, true)
            socket?.let { repository.send(it, text) }
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка отправки") }
    }

    private fun startListeningRelay() = viewModelScope.launch {
        val currentSocket = socket ?: return@launch
        while (true) {
            val incoming = repository.receive(currentSocket) ?: break
            val chatId = _ui.value.selectedChatId ?: continue

            val packet = MeshPacket(
                packetId = "pkt_${System.currentTimeMillis()}_${incoming.hashCode()}",
                sourcePeerId = "peer",
                targetPeerId = null,
                hopPeerId = "peer",
                timestamp = System.currentTimeMillis(),
                ttl = 6,
                encryptedPayload = incoming,
                deliveryState = "received"
            )

            if (seenPacketIds.add(packet.packetId)) {
                repository.saveMessage(chatId, incoming, false, "peer", "Peer")
                if (packet.ttl > 1) {
                    forwardQueue.addLast(packet.copy(ttl = packet.ttl - 1))
                }
                _ui.value = _ui.value.copy(relayQueueSize = forwardQueue.size)
            }
        }
    }
}
