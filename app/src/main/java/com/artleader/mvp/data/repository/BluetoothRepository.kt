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
    fun bondedDevices(): List<BluetoothDevice> = manager.bondedDevices()
    suspend fun connect(device: BluetoothDevice): BluetoothSocket = manager.connect(device)
    suspend fun accept(): BluetoothSocket = manager.accept()
    suspend fun send(socket: BluetoothSocket, text: String) = manager.send(socket, text)
    suspend fun receive(socket: BluetoothSocket): String? = manager.receive(socket)

    fun observeConversations(): Flow<List<ConversationEntity>> = conversationDao.observeConversations()
    fun observePeers(): Flow<List<PeerEntity>> = peerDao.observePeers()
    fun messages(chatId: String): Flow<List<MessageEntity>> = messageDao.messagesForChat(chatId)

    suspend fun discoverPeers() {
        val bonded = bondedDevices().mapIndexed { index, d ->
            PeerEntity(
                peerId = d.address,
                username = d.name ?: "Peer ${index + 1}",
                avatar = "",
                deviceId = d.address,
                bluetoothId = d.address,
                online = true
            )
        }
        peerDao.upsertAll(bonded)
    }

    suspend fun createPrivateChat(withUser: PeerEntity, ownerId: String = "me"): String {
        val id = "private_${withUser.peerId}"
        conversationDao.upsert(ConversationEntity(id, withUser.username, "private", ownerId, "$ownerId,${withUser.peerId}"))
        return id
    }

    suspend fun saveMessage(chatId: String, text: String, isMine: Boolean, senderId: String = "me", senderName: String = "Вы") {
        messageDao.insert(MessageEntity(UUID.randomUUID().toString(), chatId, senderId, senderName, null, text, 6, "delivered", System.currentTimeMillis(), isMine))
    }
}
