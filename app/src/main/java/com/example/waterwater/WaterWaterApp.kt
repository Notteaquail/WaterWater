package com.example.waterwater

import android.app.Application
import com.example.waterwater.data.database.ReminderDatabase
import com.example.waterwater.data.repository.ReminderRepository

class WaterWaterApp : Application() {

    // 懒加载数据库实例
    private val database by lazy {
        ReminderDatabase.getDatabase(this)
    }

    // 懒加载 Repository
    val repository by lazy {
        ReminderRepository(database.reminderDao())
    }
}