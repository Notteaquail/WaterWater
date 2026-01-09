package com.example.waterwater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.waterwater.model.CatMood
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import java.util.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    existingReminder: Reminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(existingReminder?.title ?: "") }
    var description by remember { mutableStateOf(existingReminder?.description ?: "") }

    // 从时间戳提取小时和分钟
    val calendar = remember {
        Calendar.getInstance().apply {
            existingReminder?.let { timeInMillis = it.timeInMillis }
                ?: add(Calendar.MINUTE, 5)
        }
    }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    // 重复类型状态
    var selectedRepeatType by remember { mutableStateOf(existingReminder?.repeatType ?: RepeatType.NONE) }
    // 重复间隔状态 (默认为 1)
    var repeatIntervalStr by remember { mutableStateOf(existingReminder?.repeatInterval?.toString() ?: "1") }

    var selectedMood by remember { mutableStateOf(existingReminder?.catMood ?: CatMood.HAPPY) }

    var showTimePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (existingReminder != null) "编辑提醒 ✏️" else "新建提醒 🐱",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("提醒事项") },
                    placeholder = { Text("例如：喝水、吃药...") },
                    singleLine = true,
                    isError = titleError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // === 时间选择 ===
                Text(
                    text = "提醒时间",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "点击修改",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 重复设置 ===
                Text(
                    text = "重复类型",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                RepeatTypeSelector(
                    selectedType = selectedRepeatType,
                    onTypeSelected = { selectedRepeatType = it }
                )

                // 如果是分钟或小时，显示输入框
                if (selectedRepeatType == RepeatType.HOURLY || selectedRepeatType == RepeatType.MINUTELY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repeatIntervalStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                repeatIntervalStr = input
                            }
                        },
                        label = { Text(if (selectedRepeatType == RepeatType.HOURLY) "每几小时？" else "每几分钟？") },
                        suffix = { Text(if (selectedRepeatType == RepeatType.HOURLY) "小时" else "分钟") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 猫咪心情 ===
                Text(
                    text = "猫咪表情",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                CatMoodSelector(
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }

                            // 解析间隔，防止空或0
                            val interval = repeatIntervalStr.toIntOrNull() ?: 1
                            val finalInterval = if (interval < 1) 1 else interval

                            val reminderTimeMillis = calculateReminderTime(selectedHour, selectedMinute)

                            val reminder = Reminder(
                                id = existingReminder?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                timeInMillis = reminderTimeMillis,
                                repeatType = selectedRepeatType,
                                repeatInterval = finalInterval, // 保存间隔
                                catMood = selectedMood,
                                isEnabled = existingReminder?.isEnabled ?: true,
                                createdAt = existingReminder?.createdAt ?: System.currentTimeMillis()
                            )
                            onConfirm(reminder)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (existingReminder != null) "保存" else "添加")
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedHour = timePickerState.hour
                selectedMinute = timePickerState.minute
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/**
 * 计算提醒时间戳
 * 允许1分钟的“过去”误差，超过1分钟才算明天
 */
private fun calculateReminderTime(hour: Int, minute: Int): Long {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // 如果设定的时间比现在早超过 1 分钟，才认为是明天
    // (例如现在 10:00，设 10:00:30，不算明天；设 09:00，算明天)
    if (calendar.timeInMillis <= now - 60 * 1000) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatTypeSelector(
    selectedType: RepeatType,
    onTypeSelected: (RepeatType) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState), // 修正：在这里添加滚动修饰符
        horizontalArrangement = Arrangement.spacedBy(8.dp) // 增加一点间距
    ) {
        RepeatType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = { Text(type.toDisplayString(), fontSize = 11.sp) },
                // 不需要在这里加 padding 了，由 Row 的 spacedBy 控制
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CatMoodSelector(
    selectedMood: CatMood,
    onMoodSelected: (CatMood) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CatMood.entries.forEach { mood ->
            val isSelected = mood == selectedMood
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMoodSelected(mood) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .padding(12.dp)
            ) {
                Text(text = mood.toEmoji(), fontSize = 28.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mood.toDisplayString(),
                    fontSize = 10.sp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("确定") }
        },
        text = { content() }
    )
}

fun RepeatType.toDisplayString(): String = when (this) {
    RepeatType.NONE -> "不重复"
    RepeatType.MINUTELY -> "分钟"
    RepeatType.HOURLY -> "小时"
    RepeatType.DAILY -> "每天"
    RepeatType.WEEKLY -> "每周"
    RepeatType.MONTHLY -> "每月"
}

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