package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val title: String,
    val type: String,
    val ownerId: String,
    val participantIds: String,
    val lastMessage: String = "",
    val lastTimestamp: Long = 0,
    val unreadCount: Int = 0
)
