package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messenger_users")
data class MessengerUserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val avatar: String,
    val deviceId: String,
    val bluetoothId: String,
    val online: Boolean
)
