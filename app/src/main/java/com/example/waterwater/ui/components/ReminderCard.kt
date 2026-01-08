package com.example.waterwater.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterwater.model.CatMood
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import com.example.waterwater.ui.theme.*
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
    val moodColor by animateColorAsState(
        targetValue = when (reminder.catMood) {
            CatMood.HAPPY -> MoodHappy
            CatMood.SLEEPY -> MoodSleepy
            CatMood.HUNGRY -> MoodHungry
            CatMood.PLAYFUL -> MoodPlayful
        },
        label = "moodColor"
    )

    val catEmoji = when (reminder.catMood) {
        CatMood.HAPPY -> "😺"
        CatMood.SLEEPY -> "😴"
        CatMood.HUNGRY -> "😿"
        CatMood.PLAYFUL -> "😸"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 猫咪表情
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(moodColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = catEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 提醒内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTime(reminder.timeInMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (reminder.repeatType != RepeatType.NONE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔁 ${getRepeatText(reminder.repeatType)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (reminder.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 开关和删除
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
    return sdf.format(Date(timeInMillis))
}

private fun getRepeatText(repeatType: RepeatType): String {
    return when (repeatType) {
        RepeatType.NONE -> ""
        RepeatType.DAILY -> "每天"
        RepeatType.WEEKLY -> "每周"
        RepeatType.MONTHLY -> "每月"
    }
}