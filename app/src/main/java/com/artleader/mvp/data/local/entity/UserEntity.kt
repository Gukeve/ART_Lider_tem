package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(

    @PrimaryKey
    val login: String,

    val password: String,
    val displayName: String,
    val position: String,

    // NEW
    val rating: Float = 4.9f,
    val projectCount: Int = 0,
    val productionPercent: Int = 0,
    val employmentStart: String = "",
    val isAdmin: Boolean = false,
    val avatar: String = ""
)