package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val encryptedPayload: String,
    val deliveryState: String,
    val timestamp: Long,
    val isMine: Boolean
)
