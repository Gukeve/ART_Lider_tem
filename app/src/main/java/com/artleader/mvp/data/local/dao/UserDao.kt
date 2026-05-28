package com.artleader.mvp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.artleader.mvp.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE login=:login AND password=:password LIMIT 1")
    suspend fun login(login: String, password: String): UserEntity?

@Query("SELECT * FROM users WHERE login = :login LIMIT 1")
suspend fun getUser(login: String): UserEntity?

@Query("UPDATE users SET avatar = :avatar WHERE login = :login")
suspend fun updateAvatar(login: String, avatar: String)
}