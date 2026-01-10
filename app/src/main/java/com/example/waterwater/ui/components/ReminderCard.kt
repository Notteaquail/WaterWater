package com.example.waterwater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterwater.model.CatMood
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import com.example.waterwater.model.toEmoji
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 吉卜力调色盘：更偏淡暖黄色的羊皮纸感
    val ghibliPaperColor = Color(0xFFFFFDE7).copy(alpha = 0.85f) 
    val ghibliStrokeColor = Color(0xFFD7CCC8).copy(alpha = 0.6f)
    val ghibliAccent = Color(0xFF4E342E) // 深褐色
    val ghibliGreen = Color(0xFF689F38) // 森林绿（用于开关）

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.5.dp, ghibliStrokeColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ghibliPaperColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圆形图标区域，带有一点水彩感
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = reminder.catMood.toEmoji(), fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ghibliAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏰ ${formatTime(reminder.timeInMillis)}",
                        fontSize = 12.sp,
                        color = ghibliAccent.copy(alpha = 0.6f)
                    )

                    if (reminder.repeatType != RepeatType.NONE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🍃 ${getRepeatText(reminder)}",
                            fontSize = 11.sp,
                            color = ghibliGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ghibliGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFBC8F8F), // 莫兰迪粉，柔和的删除色
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.CHINA)
    return sdf.format(Date(timeInMillis))
}

private fun getRepeatText(reminder: Reminder): String {
    return when (reminder.repeatType) {
        RepeatType.NONE -> ""
        RepeatType.MINUTELY -> "每 ${reminder.repeatInterval} 分"
        RepeatType.HOURLY -> "每 ${reminder.repeatInterval} 时"
        RepeatType.DAILY -> "每天"
        RepeatType.WEEKLY -> "每周"
        RepeatType.MONTHLY -> "每月"
    }
}
