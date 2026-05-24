package com.artleader.mvp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.artleader.mvp.data.local.dao.AttendanceDao
import com.artleader.mvp.data.local.dao.ConversationDao
import com.artleader.mvp.data.local.dao.MessageDao
import com.artleader.mvp.data.local.dao.PeerDao
import com.artleader.mvp.data.local.dao.UserDao
import com.artleader.mvp.data.local.entity.AttendanceEventEntity
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.PeerEntity
import com.artleader.mvp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class, ConversationEntity::class, PeerEntity::class, AttendanceEventEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun peerDao(): PeerDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        fun build(context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "artleader.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}
