package com.artleader.mvp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.UUID

class BluetoothManager(private val adapter: BluetoothAdapter?) {
    companion object {
        val APP_UUID: UUID = UUID.fromString("3b2b7e6e-f792-4d2b-bfd0-c7e9ed9f421a")
        private const val SERVICE_NAME = "ArtLeaderMessenger"
    }

    fun isEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun bondedDevices(): List<BluetoothDevice> = adapter?.bondedDevices?.toList().orEmpty()

    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): BluetoothSocket = withContext(Dispatchers.IO) {
        val socket = device.createRfcommSocketToServiceRecord(APP_UUID)
        socket.connect()
        socket
    }

    @SuppressLint("MissingPermission")
    suspend fun accept(): BluetoothSocket = withContext(Dispatchers.IO) {
        val server: BluetoothServerSocket = adapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
            ?: error("Bluetooth недоступен")
        server.use { it.accept() }
    }

    suspend fun send(socket: BluetoothSocket, message: String) = withContext(Dispatchers.IO) {
        PrintWriter(socket.outputStream, true).println(message)
    }

    suspend fun receive(socket: BluetoothSocket): String? = withContext(Dispatchers.IO) {
        BufferedReader(InputStreamReader(socket.inputStream)).readLine()
    }
}
