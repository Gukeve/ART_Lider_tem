package com.artleader.mvp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

/**
 * Legacy RFCOMM Bluetooth manager.
 *
 * NOTE:
 * This manager is kept temporarily for compatibility while the project
 * transitions fully to BLE Mesh architecture.
 *
 * Current primary transport:
 * - BleMessengerService (BLE GATT Mesh)
 *
 * This class should NOT crash the application if sockets disconnect.
 */
class BluetoothManager(
    private val adapter: BluetoothAdapter?
) {

    companion object {
        val APP_UUID: UUID =
            UUID.fromString("3b2b7e6e-f792-4d2b-bfd0-c7e9ed9f421a")

        private const val SERVICE_NAME = "ArtLeaderMessenger"
    }

    /**
     * Returns true if Bluetooth is enabled.
     */
    fun isEnabled(): Boolean {
        return adapter?.isEnabled == true
    }

    /**
     * Returns bonded devices.
     *
     * Requires:
     * BLUETOOTH_CONNECT permission on Android 12+
     */
    @SuppressLint("MissingPermission")
    fun bondedDevices(): List<BluetoothDevice> {
        return adapter?.bondedDevices?.toList().orEmpty()
    }

    /**
     * Connect to remote RFCOMM device.
     *
     * Safe against discovery conflicts and socket crashes.
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(
        device: BluetoothDevice
    ): BluetoothSocket = withContext(Dispatchers.IO) {

        if (adapter == null) {
            error("Bluetooth adapter unavailable")
        }

        try {
            // Discovery heavily breaks RFCOMM stability
            adapter.cancelDiscovery()

            val socket =
                device.createRfcommSocketToServiceRecord(APP_UUID)

            socket.connect()

            socket

        } catch (e: Exception) {
            throw IOException(
                "Failed to connect to device: ${device.address}",
                e
            )
        }
    }

    /**
     * Wait for incoming RFCOMM connection.
     */
    @SuppressLint("MissingPermission")
    suspend fun accept(): BluetoothSocket =
        withContext(Dispatchers.IO) {

            val server: BluetoothServerSocket =
                adapter?.listenUsingRfcommWithServiceRecord(
                    SERVICE_NAME,
                    APP_UUID
                ) ?: error("Bluetooth unavailable")

            try {
                server.accept()
            } finally {
                try {
                    server.close()
                } catch (_: Exception) {
                }
            }
        }

    /**
     * Send text message safely.
     */
    suspend fun send(
        socket: BluetoothSocket,
        message: String
    ) = withContext(Dispatchers.IO) {

        try {
            val writer = BufferedWriter(
                OutputStreamWriter(socket.outputStream)
            )

            writer.write(message)
            writer.newLine()
            writer.flush()

        } catch (e: Exception) {

            try {
                socket.close()
            } catch (_: Exception) {
            }

            throw IOException(
                "Failed to send Bluetooth message",
                e
            )
        }
    }

    /**
     * Receive text message safely.
     *
     * Returns null instead of crashing when:
     * - peer disconnects
     * - socket closes
     * - stream fails
     */
    suspend fun receive(
        socket: BluetoothSocket
    ): String? = withContext(Dispatchers.IO) {

        try {
            val reader = BufferedReader(
                InputStreamReader(socket.inputStream)
            )

            reader.readLine()

        } catch (e: Exception) {

            try {
                socket.close()
            } catch (_: Exception) {
            }

            null
        }
    }

    /**
     * Safely close socket.
     */
    fun closeSocket(socket: BluetoothSocket?) {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
    }
}