package com.example.waterwater.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                    // 提醒标题
    val description: String = "",         // 描述
    val timeInMillis: Long,               // 提醒时间戳
    val repeatType: RepeatType = RepeatType.NONE,  // 重复类型
    val repeatInterval: Int = 1, // 新增：重复间隔 (例如：每 2 小时的 "2")
    val isEnabled: Boolean = true,        // 是否启用
    val catMood: CatMood = CatMood.HAPPY, // 猫咪心情
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatType {
    NONE,       // 不重复
    MINUTELY,   // 新增：每x分钟
    HOURLY,     // 新增：每x小时
    DAILY,      // 每天
    WEEKLY,     // 每周
    MONTHLY     // 每月
}

enum class CatMood {
    HAPPY,      // 开心
    SLEEPY,     // 困困
    HUNGRY,     // 饿了
    PLAYFUL     // 想玩
}