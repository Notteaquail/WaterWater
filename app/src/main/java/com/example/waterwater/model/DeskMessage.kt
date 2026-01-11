package com.example.waterwater.model

import java.util.UUID

/**
 * 课桌消息模型 - 适配 Firebase 需要默认值
 */
data class DeskMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @field:JvmField
    val isRead: Boolean = false
)
