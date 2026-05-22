package com.artleader.mvp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.artleader.mvp.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE deviceName = :deviceName ORDER BY timestamp ASC")
    fun messagesForDevice(deviceName: String): Flow<List<MessageEntity>>
}
