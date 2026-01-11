package com.example.waterwater.utils

import android.util.Log
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import java.util.Calendar

object TimeUtils {
    /**
     * 计算初始/重新启用时的合法时间
     * 核心修复：尊重用户在 TimePicker 中选择的时间
     */
    fun getInitialValidTime(reminder: Reminder): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.timeInMillis
        
        // 1. 如果用户选的时间已经过去了，自动推到未来的第一个有效点
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            if (reminder.repeatType == RepeatType.NONE) {
                // 不重复的任务，如果选的是过去，就定在明天的这个时间
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            } else {
                addInterval(calendar, reminder)
            }
        }

        // 2. 检查并调整到活跃窗口
        return adjustToWindow(calendar, reminder)
    }

    /**
     * 闹钟触发后计算下一次
     */
    fun calculateNextIntervalTime(reminder: Reminder): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.timeInMillis

        // 1. 增加一个间隔
        addInterval(calendar, reminder)

        // 2. 确保计算出的是未来时间（处理手机关机很久的情况）
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            addInterval(calendar, reminder)
        }

        return adjustToWindow(calendar, reminder)
    }

    private fun addInterval(calendar: Calendar, reminder: Reminder) {
        val interval = if (reminder.repeatInterval > 0) reminder.repeatInterval else 1
        when (reminder.repeatType) {
            RepeatType.MINUTELY -> calendar.add(Calendar.MINUTE, interval)
            RepeatType.HOURLY -> calendar.add(Calendar.HOUR_OF_DAY, interval)
            RepeatType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, interval)
            RepeatType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            else -> {} 
        }
    }

    private fun adjustToWindow(calendar: Calendar, reminder: Reminder): Long {
        if (reminder.repeatType != RepeatType.MINUTELY && reminder.repeatType != RepeatType.HOURLY) {
            return calendar.timeInMillis
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val isAfterEnd = if (reminder.startHour <= reminder.endHour) {
            hour > reminder.endHour || (hour == reminder.endHour && minute > 0)
        } else {
            hour in (reminder.endHour + 1) until reminder.startHour || (hour == reminder.endHour && minute > 0)
        }

        val isBeforeStart = if (reminder.startHour <= reminder.endHour) {
            hour < reminder.startHour
        } else {
            false
        }

        if (isAfterEnd || isBeforeStart) {
            if (isAfterEnd) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            calendar.set(Calendar.HOUR_OF_DAY, reminder.startHour)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            while (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        Log.d("TimeUtils", "最终计算时间: ${calendar.time}")
        return calendar.timeInMillis
    }
}
