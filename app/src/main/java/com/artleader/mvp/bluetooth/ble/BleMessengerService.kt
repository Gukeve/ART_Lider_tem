package com.artleader.mvp.bluetooth.ble

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import com.artleader.mvp.bluetooth.mesh.MeshPacket
import com.artleader.mvp.bluetooth.mesh.NearbyPeer
import com.artleader.mvp.bluetooth.mesh.PacketType
import com.artleader.mvp.bluetooth.mesh.PeerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * BLE Mesh Service — runs as a foreground service.
 *
 * Architecture (bitchat-inspired):
 *   • Every device is BOTH peripheral (advertises) AND central (scans).
 *   • As peripheral: hosts a GATT server with a NUS-like write characteristic.
 *     Nearby centrals connect and write packets to us.
 *   • As central: scans for our service UUID, connects to peripherals, discovers
 *     their write characteristic, and writes packets to them.
 *   • Packet routing: incoming packets are delivered to the app AND optionally
 *     relayed to other connected peers (TTL-based store-and-forward).
 *
 * Transport: BLE GATT write (no-response for speed, or with-response for
 * guaranteed delivery — configurable per characteristic).
 *
 * This gives automatic bidirectional peer discovery and message transfer
 * without any manual server/client selection in the UI.
 */
@SuppressLint("MissingPermission")
class BleMessengerService : Service() {

    companion object {
        const val TAG = "BleMessenger"

        // Nordic UART Service (NUS) UUIDs — same as bitchat
        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_RX_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_TX_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        // Intents / extras
        const val EXTRA_DISPLAY_NAME = "display_name"
        const val EXTRA_PEER_ID      = "peer_id"

        // Notification
        private const val NOTIF_CHANNEL_ID = "ble_mesh"
        private const val NOTIF_ID         = 1001

        // Singleton access for ViewModel binding
        @Volatile var instance: BleMessengerService? = null
    }

    // ── Scope ────────────────────────────────────────────────────────────────
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── BT System ────────────────────────────────────────────────────────────
    private lateinit var btManager: BluetoothManager
    private var btAdapter: BluetoothAdapter? = null
    private var leScanner: BluetoothLeScanner? = null
    private var leAdvertiser: BluetoothLeAdvertiser? = null

    // ── GATT server (peripheral role) ────────────────────────────────────────
    private var gattServer: BluetoothGattServer? = null
    private val connectedCentrals = mutableSetOf<BluetoothDevice>()

    // ── GATT clients (central role, one per connected peripheral) ────────────
    private val gattClients = mutableMapOf<String, BluetoothGatt>()           // address -> gatt
    private val peerWriteChars = mutableMapOf<String, BluetoothGattCharacteristic>() // address -> RX char
    private val peerRssiByAddress = mutableMapOf<String, Int>()

    // ── Mesh state ───────────────────────────────────────────────────────────
    val peerRegistry   = PeerRegistry()
    private val dedup  = PacketDeduplicator()

    // ── Public flows (observed by ViewModel) ─────────────────────────────────
    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets.asSharedFlow()

    // ── Local identity ───────────────────────────────────────────────────────
    var myPeerId: Long    = 0L
    var myDisplayName: String = "Unknown"

    // ── Prune timer ──────────────────────────────────────────────────────────
    private var pruneJob: Job? = null

    // ─────────────────────────────────────────────────────────────────────────
    // Service lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        instance = this
        btManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        startPruneTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        myDisplayName = intent?.getStringExtra(EXTRA_DISPLAY_NAME) ?: myDisplayName
        myPeerId      = intent?.getLongExtra(EXTRA_PEER_ID, myPeerId) ?: myPeerId
        tryStart()
        return START_STICKY
    }

    private val binder = LocalBinder()

    inner class LocalBinder : android.os.Binder() {
        fun service(): BleMessengerService = this@BleMessengerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        stopMesh()
        scope.cancel()
        instance = null
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Start / Stop
    // ─────────────────────────────────────────────────────────────────────────

    private fun tryStart() {
        if (btAdapter?.isEnabled != true) {
            Log.w(TAG, "Bluetooth not enabled — cannot start mesh")
            return
        }
        leScanner    = btAdapter?.bluetoothLeScanner
        leAdvertiser = btAdapter?.bluetoothLeAdvertiser
        startGattServer()
        startAdvertising()
        startScanning()
    }

    private fun stopMesh() {
        pruneJob?.cancel()
        stopScanning()
        stopAdvertising()
        gattClients.values.forEach { it.disconnect(); it.close() }
        gattClients.clear()
        peerWriteChars.clear()
        gattServer?.close()
        gattServer = null
        connectedCentrals.clear()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GATT Server (peripheral role) — receives writes from remote centrals
    // ─────────────────────────────────────────────────────────────────────────

    private fun startGattServer() {
        val rxChar = BluetoothGattCharacteristic(
            CHAR_RX_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val txChar = BluetoothGattCharacteristic(
            CHAR_TX_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                    BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        ).also { char ->
            char.addDescriptor(
                BluetoothGattDescriptor(
                    CCCD_UUID,
                    BluetoothGattDescriptor.PERMISSION_WRITE or
                            BluetoothGattDescriptor.PERMISSION_READ
                )
            )
        }

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        service.addCharacteristic(rxChar)
        service.addCharacteristic(txChar)

        gattServer = btManager.openGattServer(this, gattServerCallback)
        gattServer?.addService(service)
        Log.d(TAG, "GATT server started")
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED    -> {
                    Log.d(TAG, "Central connected: ${device.address}")
                    connectedCentrals.add(device)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Central disconnected: ${device.address}")
                    connectedCentrals.remove(device)
                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
            if (characteristic.uuid == CHAR_RX_UUID) {
                handleIncomingBytes(value, device.address)
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int,
            descriptor: BluetoothGattDescriptor, preparedWrite: Boolean,
            responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BLE Advertising (peripheral role) — makes us discoverable
    // ─────────────────────────────────────────────────────────────────────────

    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        leAdvertiser?.startAdvertising(settings, data, advertiseCallback)
            ?: Log.w(TAG, "BLE advertising not supported on this device")
    }

    private fun stopAdvertising() {
        try { leAdvertiser?.stopAdvertising(advertiseCallback) } catch (_: Exception) {}
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "BLE advertising started")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE advertising failed: $errorCode")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BLE Scanning (central role) — discovers nearby peripherals
    // ─────────────────────────────────────────────────────────────────────────

    private fun startScanning() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        leScanner?.startScan(listOf(filter), settings, scanCallback)
            ?: Log.w(TAG, "BLE scanner not available")
    }

    private fun stopScanning() {
        try { leScanner?.stopScan(scanCallback) } catch (_: Exception) {}
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val addr   = device.address
            if (gattClients.containsKey(addr)) return // already connected
            peerRssiByAddress[addr] = result.rssi
            Log.d(TAG, "Found peer: $addr (RSSI ${result.rssi})")
            connectToPeripheral(device, result.rssi)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed: $errorCode")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GATT Client (central role) — connects to a discovered peripheral
    // ─────────────────────────────────────────────────────────────────────────

    private fun connectToPeripheral(device: BluetoothDevice, rssi: Int) {
        val gatt = device.connectGatt(this, false, makeGattCallback(rssi), BluetoothDevice.TRANSPORT_LE)
        if (gatt != null) {
            gattClients[device.address] = gatt
        }
    }

    private fun makeGattCallback(rssi: Int) = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to peripheral ${gatt.device.address}")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from ${gatt.device.address}")
                    val addr = gatt.device.address
                    peerWriteChars.remove(addr)
                    gattClients.remove(addr)
                    gatt.close()
                    // Mark peer offline; will re-discover via scan
                    val peerId = addrToPeerId(addr)
                    peerRegistry.markOffline(peerId)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            val rxChar = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHAR_RX_UUID) ?: return
            peerWriteChars[gatt.device.address] = rxChar
            Log.d(TAG, "Services discovered on ${gatt.device.address} — sending ANNOUNCE")
            // Send our ANNOUNCE so the remote device learns our displayName
            sendAnnounce(gatt.device.address)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingBytes(value, gatt.device.address)
        }

        // API < 33 fallback
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            handleIncomingBytes(characteristic.value ?: return, gatt.device.address)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Packet handling
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleIncomingBytes(bytes: ByteArray, fromAddress: String) {
        val packet = MeshPacket.fromBytes(bytes) ?: return
        if (dedup.isDuplicate(packet.packetId)) return

        scope.launch {
            when (packet.type) {
                PacketType.ANNOUNCE -> handleAnnounce(packet, fromAddress)
                PacketType.MESSAGE  -> handleMessage(packet, fromAddress)
                PacketType.PING     -> sendPong(fromAddress, packet.senderId)
                PacketType.PONG     -> handlePong(packet)
                PacketType.HELLO,
                PacketType.ACK      -> handleMessage(packet, fromAddress)
            }

            // Relay if TTL allows and it's not addressed only to us
            val isForMe = packet.targetId == myPeerId
            if (!isForMe && packet.shouldRelay) {
                relay(packet.relayed(), excludeAddress = fromAddress)
            }
        }
    }

    private fun handleAnnounce(packet: MeshPacket, fromAddress: String) {
        val name = packet.payload.decodeToString().take(64)
        val peer = NearbyPeer(
            peerId = packet.senderId,
            peerIdHex = packet.senderId.toHexString(),
            displayName = name.ifBlank { "Peer" },
            rssi = peerRssiByAddress[fromAddress] ?: Int.MIN_VALUE
        )
        peerRegistry.upsert(peer)
        Log.d(TAG, "Peer announced: ${peer.displayName} @ $fromAddress")
    }

    private suspend fun handleMessage(packet: MeshPacket, fromAddress: String) {
        if (packet.isBroadcast || packet.targetId == myPeerId || packet.senderId == myPeerId) {
            _incomingPackets.emit(packet)
        }
    }

    private fun handlePong(packet: MeshPacket) {
        val peer = peerRegistry.get(packet.senderId) ?: return
        peerRegistry.upsert(peer.copy(lastSeenAt = System.currentTimeMillis()))
    }

    private fun sendPong(toAddress: String, targetId: Long) {
        val pkt = MeshPacket.create(PacketType.PONG, myPeerId, targetId)
        writeToAddress(toAddress, pkt.toBytes())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sending
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Send a text message to all connected peers (broadcast) or a specific peer.
     */
    fun sendMessage(text: String, targetPeerId: Long = MeshPacket.BROADCAST_TARGET): MeshPacket {
        val payload = text.encodeToByteArray().take(400).toByteArray()
        val packet  = MeshPacket.create(PacketType.MESSAGE, myPeerId, targetPeerId, payload)
        broadcast(packet.toBytes())
        // Add to dedup so we don't echo our own packet back
        dedup.isDuplicate(packet.packetId)
        scope.launch { _incomingPackets.emit(packet.copy(/* mark as mine for UI */)) }
        return packet
    }

    private fun sendAnnounce(toAddress: String? = null) {
        val payload = myDisplayName.encodeToByteArray()
        val packet  = MeshPacket.create(PacketType.ANNOUNCE, myPeerId, payload = payload)
        if (toAddress != null) {
            writeToAddress(toAddress, packet.toBytes())
        } else {
            broadcast(packet.toBytes())
        }
    }

    fun broadcastAnnounce() = sendAnnounce()

    private fun relay(packet: MeshPacket, excludeAddress: String) {
        val bytes = packet.toBytes()
        peerWriteChars.forEach { (addr, char) ->
            if (addr != excludeAddress) {
                writeChar(gattClients[addr] ?: return@forEach, char, bytes)
            }
        }
    }

    private fun broadcast(bytes: ByteArray) {
        peerWriteChars.forEach { (addr, char) ->
            writeChar(gattClients[addr] ?: return@forEach, char, bytes)
        }
    }

    private fun writeToAddress(address: String, bytes: ByteArray) {
        val gatt = gattClients[address] ?: return
        val char = peerWriteChars[address] ?: return
        writeChar(gatt, char, bytes)
    }

    @Suppress("DEPRECATION")
    private fun writeChar(gatt: BluetoothGatt, char: BluetoothGattCharacteristic, bytes: ByteArray) {
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                gatt.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            } else {
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                char.value = bytes
                gatt.writeCharacteristic(char)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception writing characteristic: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private fun addrToPeerId(address: String): Long =
        address.replace(":", "").toLongOrNull(16) ?: address.hashCode().toLong()

    private fun Long.toHexString() = java.lang.Long.toHexString(this)

    private fun startPruneTimer() {
        pruneJob = scope.launch {
            while (true) {
                delay(15_000)
                peerRegistry.pruneStale()
                dedup.pruneStale()
                // Re-announce ourselves periodically
                if (peerWriteChars.isNotEmpty()) broadcastAnnounce()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Foreground notification
    // ─────────────────────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "Mesh Messenger",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Art Leader BLE mesh communication" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Art Leader Messenger")
            .setContentText("Mesh networking active")
            .setSmallIcon(R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
}