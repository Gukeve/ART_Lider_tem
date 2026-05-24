package com.artleader.mvp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_events")
data class AttendanceEventEntity(
    @PrimaryKey val id: String,
    val tagId: String,
    val employeeLogin: String,
    val action: String,
    val timestamp: Long
)
