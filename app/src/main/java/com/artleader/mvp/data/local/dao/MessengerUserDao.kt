package com.artleader.mvp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.artleader.mvp.data.local.entity.MessengerUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessengerUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<MessengerUserEntity>)

    @Query("SELECT * FROM messenger_users ORDER BY username")
    fun observeUsers(): Flow<List<MessengerUserEntity>>
}
