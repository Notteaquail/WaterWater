package com.example.waterwater.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.waterwater.MainActivity
import com.example.waterwater.model.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * @param reminder 提醒对象
     * @param showToast 是否显示 Toast 提示
     */
    fun schedule(reminder: Reminder, showToast: Boolean = true) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("TITLE", reminder.title)
            putExtra("DESCRIPTION", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // === 核心修改点：使用 setAlarmClock ===
            // 这种方式优先级最高，专用于闹钟应用，很难被系统杀后台
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                reminder.timeInMillis,
                // 当用户点击系统通知栏的闹钟图标时，跳转到哪里？跳转到我们的 MainActivity
                getMainActivityPendingIntent()
            )

            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            // ==========================================

            if (showToast) {
                Handler(Looper.getMainLooper()).post {
                    try {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val date = Date(reminder.timeInMillis)
                        Toast.makeText(context, "已设置闹钟：${sdf.format(date)}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            Log.d("AlarmScheduler", "Success (AlarmClock): ${reminder.timeInMillis}")

        } catch (e: SecurityException) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "设置失败：缺少闹钟权限", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(reminder: Reminder) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 辅助方法：点击闹钟图标跳转到主页
    private fun getMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}