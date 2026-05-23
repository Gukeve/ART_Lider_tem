package com.artleader.mvp.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "chat_users",
    primaryKeys = ["chatId", "userId"]
)
data class ChatUserEntity(
    val chatId: String,
    val userId: String,
    val role: String = "member",
    val joinedAt: Long = System.currentTimeMillis()
)
