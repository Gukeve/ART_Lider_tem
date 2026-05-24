package com.artleader.mvp.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.PeerEntity
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
    val devices: List<BluetoothDevice> = emptyList(),
    val knownPeers: List<PeerEntity> = emptyList(),
    val error: String? = null
)

class MessengerViewModel(private val repository: BluetoothRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MessengerUiState(bluetoothEnabled = repository.isEnabled()))
    val ui: StateFlow<MessengerUiState> = _ui.asStateFlow()

    val chats = repository.observeConversations().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val users = repository.observePeers().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val messages: StateFlow<List<MessageEntity>> = ui.flatMapLatest { repository.messages(it.selectedChatId ?: "") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var socket: BluetoothSocket? = null

    init { viewModelScope.launch { repository.discoverPeers() } }

    fun refreshDevices() {
        val enabled = repository.isEnabled()
        _ui.value = _ui.value.copy(
            bluetoothEnabled = enabled,
            connectionState = if (enabled) "Поиск устройств" else "Bluetooth выключен",
            devices = repository.bondedDevices(),
            error = null
        )
        viewModelScope.launch { repository.discoverPeers() }
    }

    fun openPrivateChat(user: PeerEntity) = viewModelScope.launch { _ui.value = _ui.value.copy(selectedChatId = repository.createPrivateChat(user)) }
    fun selectChat(chat: ConversationEntity) { _ui.value = _ui.value.copy(selectedChatId = chat.conversationId) }
    fun onBackFromChat() { _ui.value = _ui.value.copy(selectedChatId = null, error = null) }
    fun createNewMessage() = viewModelScope.launch {
        val first = users.value.firstOrNull() ?: return@launch
        _ui.value = _ui.value.copy(selectedChatId = repository.createPrivateChat(first))
    }


    fun connect(device: BluetoothDevice) = viewModelScope.launch {
        runCatching {
            socket = repository.connect(device)
            _ui.value = _ui.value.copy(connectionState = "Подключено: ${device.name ?: device.address}")
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка подключения") }
    }

    fun waitForIncoming() = viewModelScope.launch {
        runCatching {
            _ui.value = _ui.value.copy(connectionState = "Ожидание входящего подключения")
            socket = repository.accept()
            _ui.value = _ui.value.copy(connectionState = "Входящее подключение установлено")
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка ожидания входящего подключения") }
    }

    fun send(text: String) = viewModelScope.launch {
        val chatId = _ui.value.selectedChatId ?: return@launch
        runCatching {
            repository.saveMessage(chatId, text, true)
            socket?.let { repository.send(it, text) }
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка отправки") }
    }
}
