package com.artleader.mvp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.artleader.mvp.data.local.dao.ChatDao
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.dao.MessengerUserDao
import com.artleader.mvp.data.local.dao.UserDao
import com.artleader.mvp.data.local.entity.ChatEntity
import com.artleader.mvp.data.local.entity.ChatUserEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.MessengerUserEntity
import com.artleader.mvp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class, ChatEntity::class, ChatUserEntity::class, MessengerUserEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun messengerUserDao(): MessengerUserDao

    companion object {
        fun build(context: Context): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "artleader.db"
        ).fallbackToDestructiveMigration().build()
    }
}
