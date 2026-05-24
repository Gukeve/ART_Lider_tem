package com.artleader.mvp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.artleader.mvp.data.local.entity.AttendanceEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AttendanceEventEntity)

    @Query("SELECT * FROM attendance_events ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<AttendanceEventEntity>>
}
