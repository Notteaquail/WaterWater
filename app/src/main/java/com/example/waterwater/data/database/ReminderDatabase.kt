package com.example.waterwater.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.waterwater.model.Reminder

@Database(
    entities = [Reminder::class],
    version = 2,
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "cat_reminder_database"
                )
                .fallbackToDestructiveMigration() // 2. 添加这一行：版本升级时清空旧数据
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}