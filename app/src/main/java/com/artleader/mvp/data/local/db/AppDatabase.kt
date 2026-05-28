package com.artleader.mvp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

/**
 * Database version history:
 *   1–4  legacy
 *   5    phase-2: added PeerEntity, ConversationEntity, AttendanceEventEntity
 *   6    phase-3: mesh-ready MessageEntity (packetId, hopCount, isRelayed, attachmentJson)
 *                 extended UserEntity (employmentStart, avatar, rating, projectCount, etc.)
 */
@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        PeerEntity::class,
        AttendanceEventEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun peerDao(): PeerDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // MessageEntity new columns
                db.execSQL("ALTER TABLE messages ADD COLUMN packetId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN hopCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN isRelayed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN attachmentJson TEXT")
                // UserEntity new columns
                db.execSQL("ALTER TABLE users ADD COLUMN employmentStart TEXT NOT NULL DEFAULT '2022-01-01'")
                db.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN rating REAL NOT NULL DEFAULT 4.5")
                db.execSQL("ALTER TABLE users ADD COLUMN projectCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN productionPercent INTEGER NOT NULL DEFAULT 85")
                db.execSQL("ALTER TABLE users ADD COLUMN isAdmin INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun build(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "artleader.db")
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration() // Fallback for <5 installs
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
