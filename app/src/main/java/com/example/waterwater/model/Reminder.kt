package com.example.waterwater.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeInMillis: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 1,
    val isEnabled: Boolean = true,
    val catMood: CatMood = CatMood.HAPPY,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatType {
    NONE, MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY
}

enum class CatMood {
    HAPPY, SLEEPY, HUNGRY, PLAYFUL
}

// === 扩展函数转移到此处，方便全应用复用 ===

fun CatMood.toEmoji(): String = when (this) {
    CatMood.HAPPY -> "😸"
    CatMood.SLEEPY -> "😴"
    CatMood.HUNGRY -> "🍖"
    CatMood.PLAYFUL -> "😺"
}

fun CatMood.toDisplayString(): String = when (this) {
    CatMood.HAPPY -> "开心"
    CatMood.SLEEPY -> "困困"
    CatMood.HUNGRY -> "饿了"
    CatMood.PLAYFUL -> "想玩"
}

fun RepeatType.toDisplayString(): String = when (this) {
    RepeatType.NONE -> "不重复"
    RepeatType.MINUTELY -> "分钟"
    RepeatType.HOURLY -> "小时"
    RepeatType.DAILY -> "每天"
    RepeatType.WEEKLY -> "每周"
    RepeatType.MONTHLY -> "每月"
}
