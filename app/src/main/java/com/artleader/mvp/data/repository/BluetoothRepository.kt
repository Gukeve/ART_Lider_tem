package com.artleader.mvp.data.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.artleader.mvp.bluetooth.BluetoothManager
import com.artleader.mvp.data.local.dao.ChatDao
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.dao.MessengerUserDao
import com.artleader.mvp.data.local.entity.ChatEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.MessengerUserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BluetoothRepository(
    private val manager: BluetoothManager,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val userDao: MessengerUserDao
) {
    fun isEnabled() = manager.isEnabled()
    fun bondedDevices(): List<BluetoothDevice> = manager.bondedDevices()
    suspend fun connect(device: BluetoothDevice): BluetoothSocket = manager.connect(device)
    suspend fun accept(): BluetoothSocket = manager.accept()
    suspend fun send(socket: BluetoothSocket, text: String) = manager.send(socket, text)
    suspend fun receive(socket: BluetoothSocket): String? = manager.receive(socket)

    fun observeChats(): Flow<List<ChatEntity>> = chatDao.observeChats()
    fun observeUsers(): Flow<List<MessengerUserEntity>> = userDao.observeUsers()
    fun messages(chatId: String): Flow<List<MessageEntity>> = messageDao.messagesForChat(chatId)

    suspend fun seedUsers() = userDao.upsertAll(
        listOf(
            MessengerUserEntity("u1", "Юля", "", "dev-y", "bt-y", true),
            MessengerUserEntity("u2", "Данил", "", "dev-d", "bt-d", false),
            MessengerUserEntity("u3", "Дима", "", "dev-m", "bt-m", true)
        )
    )

    suspend fun createPrivateChat(withUser: MessengerUserEntity, ownerId: String = "me"): String {
        val id = "private_${withUser.userId}"
        chatDao.upsert(ChatEntity(id, withUser.username, "private", ownerId, "$ownerId,${withUser.userId}"))
        return id
    }

    suspend fun createGroupChat(name: String, memberIds: List<String>, ownerId: String = "me"): String {
        val id = "group_${UUID.randomUUID()}"
        chatDao.upsert(ChatEntity(id, name, "group", ownerId, (memberIds + ownerId).distinct().joinToString(",")))
        return id
    }

    suspend fun saveMessage(
        chatId: String,
        text: String,
        isMine: Boolean,
        senderId: String = "me",
        senderName: String = "Вы",
        targetId: String? = null,
        ttl: Int = 6
    ) {
        messageDao.insert(
            MessageEntity(
                messageId = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = senderId,
                senderName = senderName,
                targetId = targetId,
                encryptedPayload = text,
                ttl = ttl,
                deliveryState = "delivered",
                timestamp = System.currentTimeMillis(),
                isMine = isMine
            )
        )
    }
}
