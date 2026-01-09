package com.example.waterwater

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.waterwater.alarm.AlarmScheduler
import com.example.waterwater.data.database.ReminderDatabase
import com.example.waterwater.data.repository.ReminderRepository

class WaterWaterApp : Application() {

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }

    // 新增 Scheduler 实例
    val alarmScheduler by lazy { AlarmScheduler(this) }

    companion object {
        const val CHANNEL_ID = "cat_reminder_channel_v2"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "喵喵提醒通知"
            val descriptionText = "用于发送喝水和休息提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}