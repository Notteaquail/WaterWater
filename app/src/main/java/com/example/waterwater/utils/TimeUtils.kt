package com.example.waterwater.utils

import java.util.Calendar

object TimeUtils {
    /**
     * 计算下一次提醒的时间戳。
     * 如果设定的时间早于当前时间，则认为是明天。
     */
    fun calculateNextReminderTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 允许 1 分钟内的“过去”时间（处理设置时的延迟）
            if (timeInMillis <= System.currentTimeMillis() - 60 * 1000) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }
}
