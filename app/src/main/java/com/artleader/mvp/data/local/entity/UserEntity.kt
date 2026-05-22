package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val login: String,
    val password: String,
    val displayName: String,
    val position: String,
    val birthDate: String = "1990-01-01"
)
