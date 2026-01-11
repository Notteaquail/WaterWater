package com.example.waterwater.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.waterwater.MainActivity
import com.example.waterwater.R
import com.example.waterwater.WaterWaterApp
import com.example.waterwater.model.RepeatType
import com.example.waterwater.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
        val title = intent.getStringExtra("TITLE") ?: "喝水时间到啦"
        val description = intent.getStringExtra("DESCRIPTION") ?: "快去补充水分吧 💧"

        // 1. 发送通知
        showNotification(context, reminderId, title, description)

        // 2. 处理重复逻辑
        val pendingResult = goAsync()
        val app = context.applicationContext as WaterWaterApp
        val repository = app.repository
        val scheduler = app.alarmScheduler

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val reminder = repository.getReminderById(reminderId)

                if (reminder != null && reminder.isEnabled) {
                    if (reminder.repeatType == RepeatType.NONE) {
                        repository.updateReminder(reminder.copy(isEnabled = false))
                    } else {
                        // === 核心修改点：使用升级后的 TimeUtils 计算下一次时间 ===
                        val nextTime = TimeUtils.calculateNextIntervalTime(reminder)

                        val updatedReminder = reminder.copy(timeInMillis = nextTime)
                        repository.updateReminder(updatedReminder)

                        // 修正：删除不存在的 showToast 参数
                        scheduler.schedule(updatedReminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, id: Long, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WaterWaterApp.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("🐱 $title")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            // 关键点：使用 reminderId 作为通知 ID，防止旧版残留通知堆叠
            .build()

        notificationManager.notify(id.toInt(), notification)
    }
}
