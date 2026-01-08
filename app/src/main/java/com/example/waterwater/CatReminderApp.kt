package com.example.waterwater

import android.app.Application
import com.example.waterwater.data.database.ReminderDatabase
import com.example.waterwater.data.repository.ReminderRepository

class CatReminderApp : Application() {

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
}