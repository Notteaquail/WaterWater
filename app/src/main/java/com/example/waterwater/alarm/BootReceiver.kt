package com.example.waterwater.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.waterwater.WaterWaterApp
import com.example.waterwater.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "手机重启，正在重新对齐猫咪提醒任务...")

            val app = context.applicationContext as WaterWaterApp
            val repository = app.repository
            val scheduler = app.alarmScheduler

            CoroutineScope(Dispatchers.IO).launch {
                repository.getEnabledRemindersList().forEach { reminder ->
                    // === 核心修正：开机后重新校验所有启用中的提醒 ===
                    // 无论是否过期，都通过 TimeUtils 找到下一个最近的合法未来时间点
                    val nextValidTime = TimeUtils.getInitialValidTime(reminder)
                    val updatedReminder = reminder.copy(timeInMillis = nextValidTime)
                    
                    // 同步到数据库并重新设置闹钟
                    repository.updateReminder(updatedReminder)
                    scheduler.schedule(updatedReminder)
                    
                    Log.d("BootReceiver", "已重新挂载任务: ${reminder.title} -> 下次响铃: ${nextValidTime}")
                }
            }
        }
    }
}
