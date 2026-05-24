package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val peerId: String,
    val username: String,
    val avatar: String,
    val deviceId: String,
    val bluetoothId: String,
    val online: Boolean,
    val lastSeenAt: Long = System.currentTimeMillis()
)
