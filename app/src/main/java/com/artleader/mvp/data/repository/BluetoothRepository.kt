package com.artleader.mvp.data.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.artleader.mvp.bluetooth.BluetoothManager
import com.artleader.mvp.data.local.dao.ConversationDao
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.dao.PeerDao
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.PeerEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BluetoothRepository(
    private val manager: BluetoothManager,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val peerDao: PeerDao
) {
    fun isEnabled() = manager.isEnabled()
    fun bondedDevices(): List<BluetoothDevice> = try {
        manager.bondedDevices()
    } catch (e: SecurityException) {
        emptyList()
    }

    suspend fun connect(device: BluetoothDevice): BluetoothSocket = manager.connect(device)
    suspend fun accept(): BluetoothSocket = manager.accept()
    suspend fun send(socket: BluetoothSocket, text: String) = manager.send(socket, text)
    suspend fun receive(socket: BluetoothSocket): String? = manager.receive(socket)

    fun observeConversations(): Flow<List<ConversationEntity>> = conversationDao.observeConversations()
    fun observePeers(): Flow<List<PeerEntity>> = peerDao.observePeers()
    fun messages(chatId: String): Flow<List<MessageEntity>> = messageDao.messagesForChat(chatId)

    /**
     * Discovers peers from bonded BT devices.
     * Wrapped in try-catch: BLUETOOTH_CONNECT permission may not be granted yet on Android 12+,
     * which would throw SecurityException. Graceful no-op is safe — user can retry via UI.
     */
    suspend fun discoverPeers() {
        try {
            val bonded = bondedDevices().mapIndexed { index, d ->
                @Suppress("MissingPermission")
                PeerEntity(
                    peerId      = d.address,
                    username    = try { d.name } catch (e: SecurityException) { null } ?: "Peer ${index + 1}",
                    avatar      = "",
                    deviceId    = d.address,
                    bluetoothId = d.address,
                    online      = true
                )
            }
            if (bonded.isNotEmpty()) {
                peerDao.upsertAll(bonded)
            }
        } catch (e: SecurityException) {
            // Permission not granted yet — will retry when user grants BT permissions via UI
        } catch (e: Exception) {
            // Any other BT error is non-fatal
        }
    }

    suspend fun createPrivateChat(withUser: PeerEntity, ownerId: String = "me"): String {
        val id = "private_${withUser.peerId}"
        conversationDao.upsert(
            ConversationEntity(
                conversationId = id,
                title          = withUser.username,
                type           = "private",
                ownerId        = ownerId,
                participantIds = "$ownerId,${withUser.peerId}"
            )
        )
        return id
    }

    suspend fun saveMessage(
        chatId: String,
        text: String,
        isMine: Boolean,
        senderId: String = "me",
        senderName: String = "Вы"
    ) {
        messageDao.insert(
            MessageEntity(
                messageId        = UUID.randomUUID().toString(),
                chatId           = chatId,
                senderId         = senderId,
                senderName       = senderName,
                targetId         = null,
                encryptedPayload = text,
                ttl              = 6,
                deliveryState    = "delivered",
                timestamp        = System.currentTimeMillis(),
                isMine           = isMine,
                isRelayed        = !isMine
            )
        )
    }
}