package com.artleader.mvp.bluetooth.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.artleader.mvp.bluetooth.mesh.MeshPacket
import com.artleader.mvp.bluetooth.mesh.MeshRouter
import com.artleader.mvp.bluetooth.mesh.NearbyPeer
import com.artleader.mvp.bluetooth.mesh.PacketType
import com.artleader.mvp.bluetooth.mesh.PeerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.UUID

class BleMessengerService : Service() {
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val peerRegistry = PeerRegistry()

    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets

    private var displayName: String = "Peer"
    private var peerId: Long = 0L
    private var router: MeshRouter? = null

    private val bluetoothManager by lazy { getSystemService(BluetoothManager::class.java) }
    private val advertiser get() = bluetoothManager?.adapter?.bluetoothLeAdvertiser
    private val scanner get() = bluetoothManager?.adapter?.bluetoothLeScanner

    inner class LocalBinder : Binder() {
        fun service(): BleMessengerService = this@BleMessengerService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        displayName = intent?.getStringExtra(EXTRA_DISPLAY_NAME).orEmpty().ifBlank { "Peer" }
        peerId = intent?.getLongExtra(EXTRA_PEER_ID, 0L)?.takeIf { it != 0L }
            ?: displayName.hashCode().toLong()
        router = MeshRouter(
            localPeerId = peerId,
            onRelay = ::broadcastPacket,
            onDeliver = { packet -> serviceScope.launch { _incomingPackets.emit(packet) } }
        )
        peerRegistry.upsert(NearbyPeer(peerId = peerId, displayName = displayName, isOnline = true))
        startAutoDiscovery()
        sendHello()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        stopAutoDiscovery()
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun sendMessage(text: String) {
        val packet = MeshPacket(
            senderId = peerId,
            targetId = null,
            type = PacketType.MESSAGE,
            payload = text.encodeToByteArray()
        )
        router?.handle(packet)
        broadcastPacket(packet)
    }

    private fun sendHello() {
        val hello = MeshPacket(
            senderId = peerId,
            targetId = null,
            type = PacketType.HELLO,
            payload = displayName.encodeToByteArray(),
            ttl = 1
        )
        broadcastPacket(hello)
    }

    private fun handlePacket(packet: MeshPacket) {
        if (packet.type == PacketType.HELLO) {
            peerRegistry.upsert(
                NearbyPeer(
                    peerId = packet.senderId,
                    displayName = packet.payload.decodeToString().ifBlank { "Peer" },
                    isOnline = true,
                    lastSeenAt = packet.timestamp
                )
            )
        }
        router?.handle(packet)
    }

    private fun broadcastPacket(packet: MeshPacket) {
        // The GATT/advertising transport is intentionally best-effort: packet relay stays in
        // MeshRouter, while BLE callbacks below provide automatic peer discovery.
        advertise(packet)
    }

    @SuppressLint("MissingPermission")
    private fun startAutoDiscovery() {
        if (!hasBlePermissions()) return
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build())
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        runCatching { scanner?.startScan(filters, settings, scanCallback) }
        advertise(null)
    }

    @SuppressLint("MissingPermission")
    private fun stopAutoDiscovery() {
        if (!hasBlePermissions()) return
        runCatching { scanner?.stopScan(scanCallback) }
        runCatching { advertiser?.stopAdvertising(advertiseCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun advertise(packet: MeshPacket?) {
        if (!hasBlePermissions()) return
        val dataBuilder = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
        val payload = packet?.toJson()?.encodeToByteArray()
            ?.take(MAX_ADVERTISE_BYTES)
            ?.toByteArray()
            ?: ByteBuffer.allocate(Long.SIZE_BYTES).putLong(peerId).array()
        dataBuilder.addServiceData(ParcelUuid(SERVICE_UUID), payload)
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()
        runCatching {
            advertiser?.stopAdvertising(advertiseCallback)
            advertiser?.startAdvertising(settings, dataBuilder.build(), advertiseCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val bytes = result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID)) ?: return
            val packet = runCatching { MeshPacket.fromJson(bytes.decodeToString()) }.getOrNull()
            if (packet != null) {
                handlePacket(packet)
            } else if (bytes.size == Long.SIZE_BYTES) {
                val remotePeerId = ByteBuffer.wrap(bytes).long
                if (remotePeerId != peerId) {
                    peerRegistry.upsert(NearbyPeer(remotePeerId, runCatching { result.device?.name }.getOrNull() ?: "Peer"))
                }
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {}

    private fun hasBlePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setContentTitle("Art Leader Mesh")
        .setContentText("BLE mesh активен")
        .setOngoing(true)
        .also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, "BLE Mesh", NotificationManager.IMPORTANCE_LOW)
                getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }
        .build()

    companion object {
        const val EXTRA_DISPLAY_NAME = "extra_display_name"
        const val EXTRA_PEER_ID = "extra_peer_id"
        private const val CHANNEL_ID = "ble_mesh"
        private const val NOTIFICATION_ID = 42
        private const val MAX_ADVERTISE_BYTES = 20
        val SERVICE_UUID: UUID = UUID.fromString("3b2b7e6e-f792-4d2b-bfd0-c7e9ed9f421a")

        @Volatile
        var instance: BleMessengerService? = null
            private set
    }
}
