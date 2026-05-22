package com.artleader.mvp.viewmodel

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.local.entity.MessageEntity
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
    val devices: List<BluetoothDevice> = emptyList(),
    val selectedDeviceName: String? = null,
    val error: String? = null
)

class MessengerViewModel(private val repository: BluetoothRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MessengerUiState(bluetoothEnabled = repository.isEnabled()))
    val ui: StateFlow<MessengerUiState> = _ui.asStateFlow()

    private val selectedDevice = MutableStateFlow<String?>(null)
    val messages: StateFlow<List<MessageEntity>> = selectedDevice.flatMapLatest { name ->
        repository.messages(name ?: "")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var socket: BluetoothSocket? = null

    fun refreshDevices() {
        _ui.value = _ui.value.copy(
            bluetoothEnabled = repository.isEnabled(),
            devices = repository.bondedDevices(),
            connectionState = if (repository.isEnabled()) "Поиск устройств" else "Bluetooth выключен"
        )
    }

    fun waitForIncoming() = viewModelScope.launch {
        runCatching {
            _ui.value = _ui.value.copy(connectionState = "Ожидание подключения")
            socket = repository.accept()
            val name = socket?.remoteDevice?.name ?: "Unknown"
            selectedDevice.value = name
            _ui.value = _ui.value.copy(connectionState = "Подключено", selectedDeviceName = name)
            startListening()
        }.onFailure { _ui.value = _ui.value.copy(error = "Ошибка подключения: ${it.message}") }
    }

    fun connect(device: BluetoothDevice) = viewModelScope.launch {
        runCatching {
            _ui.value = _ui.value.copy(connectionState = "Подключение к ${device.name}")
            socket = repository.connect(device)
            selectedDevice.value = device.name ?: "Unknown"
            _ui.value = _ui.value.copy(connectionState = "Подключено", selectedDeviceName = selectedDevice.value)
            startListening()
        }.onFailure { _ui.value = _ui.value.copy(error = "Устройство недоступно: ${it.message}") }
    }

    fun send(text: String) = viewModelScope.launch {
        val currentSocket = socket ?: return@launch
        val device = _ui.value.selectedDeviceName ?: "Unknown"
        runCatching {
            repository.send(currentSocket, text)
            repository.saveMessage(device, text, true)
        }.onFailure { _ui.value = _ui.value.copy(error = "Соединение потеряно") }
    }

    private fun startListening() = viewModelScope.launch {
        val currentSocket = socket ?: return@launch
        val device = _ui.value.selectedDeviceName ?: "Unknown"
        while (true) {
            val incoming = repository.receive(currentSocket) ?: break
            repository.saveMessage(device, incoming, false)
        }
        _ui.value = _ui.value.copy(connectionState = "Соединение потеряно")
    }

    fun clearError() { _ui.value = _ui.value.copy(error = null) }
}
