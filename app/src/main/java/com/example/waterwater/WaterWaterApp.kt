package com.example.waterwater

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import cn.leancloud.LeanCloud
import com.example.waterwater.alarm.AlarmScheduler
import com.example.waterwater.data.database.ReminderDatabase
import com.example.waterwater.data.repository.ReminderRepository

class WaterWaterApp : Application() {

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
    val alarmScheduler by lazy { AlarmScheduler(this) }

    companion object {
        const val CHANNEL_ID = "cat_reminder_channel_v2"
    }

    override fun onCreate() {
        super.onCreate()
        
        // === 初始化 LeanCloud ===
        // 请在此处填入你在 LeanCloud 控制台获取的凭证
        LeanCloud.initialize(
            this,
            "htmh3FMIyawOXwMvmkCUy89j-gzGzoHsz",
            "jYePTQUn0P5vw4oZ9CQmsyyP",
            "https://htmh3fmi.lc-cn-n1-shared.com" // 通常以 https:// 开头
        )
        
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
