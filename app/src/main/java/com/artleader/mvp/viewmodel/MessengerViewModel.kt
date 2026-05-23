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

data class MessengerUiState(
    val bluetoothEnabled: Boolean = false,
    val connectionState: String = "Bluetooth выключен",
    val selectedChatId: String? = null,
    val error: String? = null
)

class MessengerViewModel(private val repository: BluetoothRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MessengerUiState(bluetoothEnabled = repository.isEnabled()))
    val ui: StateFlow<MessengerUiState> = _ui.asStateFlow()

    val chats = repository.observeChats().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val users = repository.observeUsers().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val nearbyDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    val messages: StateFlow<List<MessageEntity>> = ui.flatMapLatest { state ->
        repository.messages(state.selectedChatId ?: "")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var socket: BluetoothSocket? = null

    init { viewModelScope.launch { repository.seedUsers() } }

    fun refreshDevices() {
        _ui.value = _ui.value.copy(bluetoothEnabled = repository.isEnabled(), connectionState = if (repository.isEnabled()) "Поиск устройств" else "Bluetooth выключен")
        nearbyDevices.value = repository.bondedDevices()
    }

    fun openPrivateChat(user: MessengerUserEntity) = viewModelScope.launch {
        val chatId = repository.createPrivateChat(user)
        _ui.value = _ui.value.copy(selectedChatId = chatId)
    }

    fun createGroup(name: String, members: List<MessengerUserEntity>) = viewModelScope.launch {
        val chatId = repository.createGroupChat(name, members.map { it.userId })
        _ui.value = _ui.value.copy(selectedChatId = chatId)
    }

    fun selectChat(chat: ChatEntity) { _ui.value = _ui.value.copy(selectedChatId = chat.chatId) }

    fun connectAsClient(device: BluetoothDevice) = viewModelScope.launch {
        runCatching {
            socket = repository.connect(device)
            _ui.value = _ui.value.copy(connectionState = "Подключено: ${device.name ?: device.address}")
            startListeningRelay()
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка подключения") }
    }

    fun hostSession() = viewModelScope.launch {
        runCatching {
            _ui.value = _ui.value.copy(connectionState = "Ожидание клиентов (host)")
            socket = repository.accept()
            _ui.value = _ui.value.copy(connectionState = "Host connected")
            startListeningRelay()
        }.onFailure { _ui.value = _ui.value.copy(error = "Host error") }
    }

    fun send(text: String) = viewModelScope.launch {
        val chatId = _ui.value.selectedChatId ?: return@launch
        runCatching {
            repository.saveMessage(chatId, text, true)
            socket?.let { repository.send(it, text) }
        }.onFailure { _ui.value = _ui.value.copy(error = "send failed") }
    }

    private fun startListeningRelay() = viewModelScope.launch {
        val current = socket ?: return@launch
        while (true) {
            val incoming = repository.receive(current) ?: break
            val chatId = _ui.value.selectedChatId ?: continue
            repository.saveMessage(chatId, incoming, false, "peer", "Peer")
        }
    }
}
