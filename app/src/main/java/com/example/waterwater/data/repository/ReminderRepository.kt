package com.example.waterwater.data.repository

import com.example.waterwater.data.database.ReminderDao
import com.example.waterwater.model.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    val enabledReminders: Flow<List<Reminder>> = reminderDao.getEnabledReminders()

    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun toggleReminderEnabled(reminder: Reminder) {
        reminderDao.updateReminder(reminder.copy(isEnabled = !reminder.isEnabled))
    }
}