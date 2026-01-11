package com.example.waterwater.utils

import android.content.Context
import androidx.compose.ui.geometry.Offset

class CatPositionManager(context: Context) {
    private val prefs = context.getSharedPreferences("cat_positions", Context.MODE_PRIVATE)

    /**
     * 保存猫咪位置
     */
    fun savePosition(catId: String, offset: Offset) {
        prefs.edit()
            .putFloat("${catId}_x", offset.x)
            .putFloat("${catId}_y", offset.y)
            .apply()
    }

    /**
     * 读取猫咪位置，如果没有记录则返回默认位置
     */
    fun getPosition(catId: String, defaultOffset: Offset): Offset {
        val x = prefs.getFloat("${catId}_x", defaultOffset.x)
        val y = prefs.getFloat("${catId}_y", defaultOffset.y)
        return Offset(x, y)
    }
}
