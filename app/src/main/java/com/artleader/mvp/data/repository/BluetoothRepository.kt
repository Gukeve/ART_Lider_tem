package com.artleader.mvp.data.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.artleader.mvp.bluetooth.BluetoothManager
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class BluetoothRepository(
    private val manager: BluetoothManager,
    private val messageDao: MessageDao
) {
    fun isEnabled() = manager.isEnabled()
    fun bondedDevices(): List<BluetoothDevice> = manager.bondedDevices()
    suspend fun connect(device: BluetoothDevice): BluetoothSocket = manager.connect(device)
    suspend fun accept(): BluetoothSocket = manager.accept()
    suspend fun send(socket: BluetoothSocket, text: String) = manager.send(socket, text)
    suspend fun receive(socket: BluetoothSocket): String? = manager.receive(socket)

    suspend fun saveMessage(deviceName: String, text: String, isMine: Boolean) {
        messageDao.insert(MessageEntity(deviceName = deviceName, text = text, isMine = isMine, timestamp = System.currentTimeMillis()))
    }

    fun messages(deviceName: String): Flow<List<MessageEntity>> = messageDao.messagesForDevice(deviceName)
}
