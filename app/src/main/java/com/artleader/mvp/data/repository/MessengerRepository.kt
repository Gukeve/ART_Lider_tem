package com.artleader.mvp.data.repository

import com.artleader.mvp.data.local.dao.ConversationDao
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessengerRepository(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) {
    fun observeConversations(): Flow<List<ConversationEntity>> = conversationDao.observeConversations()

    fun messagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.messagesForChat(chatId)

    suspend fun ensureConversation(conversation: ConversationEntity) {
        conversationDao.upsert(conversation)
    }

    suspend fun saveMessage(message: MessageEntity) {
        messageDao.insert(message)
        updateLastMessage(message.chatId, message.encryptedPayload, message.timestamp)
    }

    suspend fun updateLastMessage(chatId: String, text: String, timestamp: Long) {
        conversationDao.updateLastMessage(chatId, text, timestamp)
    }
}
