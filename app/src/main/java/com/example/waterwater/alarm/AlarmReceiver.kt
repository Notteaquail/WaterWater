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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
        val title = intent.getStringExtra("TITLE") ?: "喝水时间到啦"
        val description = intent.getStringExtra("DESCRIPTION") ?: "快去补充水分吧 💧"

        // 1. 发送通知 (前台可见)
        showNotification(context, title, description)

        // 2. 处理重复逻辑 (使用 goAsync 保持 BroadcastReceiver 存活)
        val pendingResult = goAsync()
        val app = context.applicationContext as WaterWaterApp
        val repository = app.repository
        val scheduler = app.alarmScheduler

        // 使用 IO 线程处理数据库操作
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                // 从数据库获取最新的提醒信息
                val reminder = repository.getReminderById(reminderId)

                if (reminder != null && reminder.isEnabled) {
                    if (reminder.repeatType == RepeatType.NONE) {
                        // 如果不重复，响铃后自动关闭开关
                        repository.updateReminder(reminder.copy(isEnabled = false))
                    } else {
                        // 如果需要重复，计算下一次时间
                        // 传入当前的设定时间、重复类型、以及间隔 (repeatInterval)
                        val nextTime = calculateNextTime(
                            reminder.timeInMillis,
                            reminder.repeatType,
                            reminder.repeatInterval
                        )

                        // 更新数据库中的时间
                        val updatedReminder = reminder.copy(timeInMillis = nextTime)
                        repository.updateReminder(updatedReminder)

                        // 设置下一次闹钟 (showToast = false 避免后台弹窗崩溃)
                        scheduler.schedule(updatedReminder, showToast = false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 必须调用，通知系统任务完成
                pendingResult.finish()
            }
        }
    }

    /**
     * 计算下次提醒时间
     * @param currentTime 上次设定的时间
     * @param repeatType 重复类型
     * @param interval 重复间隔 (例如每 2 小时)
     */
    private fun calculateNextTime(currentTime: Long, repeatType: RepeatType, interval: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime

        // 确保 interval 至少为 1
        val validInterval = if (interval < 1) 1 else interval

        // 基础计算
        when (repeatType) {
            RepeatType.MINUTELY -> calendar.add(Calendar.MINUTE, validInterval)
            RepeatType.HOURLY -> calendar.add(Calendar.HOUR_OF_DAY, validInterval)
            RepeatType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, validInterval)
            RepeatType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1) // 周通常按 1 算
            RepeatType.MONTHLY -> calendar.add(Calendar.MONTH, 1)       // 月通常按 1 算
            RepeatType.NONE -> {}
        }

        // 追赶机制：如果算出来的时间已经过去了(比如手机关机了很久)，继续往后推，直到是“未来”
        val now = System.currentTimeMillis()
        while (calendar.timeInMillis <= now) {
            when (repeatType) {
                RepeatType.MINUTELY -> calendar.add(Calendar.MINUTE, validInterval)
                RepeatType.HOURLY -> calendar.add(Calendar.HOUR_OF_DAY, validInterval)
                RepeatType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, validInterval)
                RepeatType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                RepeatType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                else -> break
            }
        }

        return calendar.timeInMillis
    }

    private fun showNotification(context: Context, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 点击通知跳转到 MainActivity
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WaterWaterApp.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // 请确保图标存在
            .setContentTitle("🐱 $title")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 默认声音和震动
            .setVibrate(longArrayOf(0, 500, 200, 500))   // 自定义震动
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}