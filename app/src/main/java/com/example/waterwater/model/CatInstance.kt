package com.example.waterwater.model

import androidx.compose.ui.geometry.Offset
import java.util.UUID

/**
 * 猫咪品种定义
 */
enum class CatBreed {
    BLACK_WHITE_LONG,   // 黑白长毛
    GOLDEN_LONG,        // 长毛金渐层
    CREAM_BRITISH,      // 乳色英短
    MUNCHKIN_SHORT,     // 曼基康矮脚
    ONE_EYE_GOLDEN      // 独眼曼基康金渐层
}

/**
 * 猫咪实例模型
 */
data class CatInstance(
    val id: String = UUID.randomUUID().toString(),
    val breed: CatBreed,
    var offset: Offset = Offset(0f, 0f), // 存储相对于初始位置的偏移
    var scale: Float = 1.0f,             // 缩放系数
    var isThinkingEnabled: Boolean = true // 是否允许弹出思考气泡
)
