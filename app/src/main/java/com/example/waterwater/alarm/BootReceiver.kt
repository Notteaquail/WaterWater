package com.example.waterwater.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.waterwater.WaterWaterApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling alarms...")

            val app = context.applicationContext as WaterWaterApp
            val repository = app.repository
            val scheduler = AlarmScheduler(context)

            // 使用协程在后台线程查询数据库
            CoroutineScope(Dispatchers.IO).launch {
                repository.getEnabledRemindersList().forEach { reminder ->
                    if (reminder.timeInMillis > System.currentTimeMillis()) {
                        scheduler.schedule(reminder, showToast = false)
                    }
                }
            }
        }
    }
}