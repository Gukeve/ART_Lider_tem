package com.artleader.mvp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.dao.UserDao
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao

    companion object {
        fun build(context: Context): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "artleader.db"
        ).fallbackToDestructiveMigration().build()
    }
}
