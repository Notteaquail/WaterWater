package com.example.waterwater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.DialogProperties
import com.example.waterwater.model.CatMood
import com.example.waterwater.model.Reminder
import com.example.waterwater.model.RepeatType
import com.example.waterwater.model.toDisplayString
import com.example.waterwater.model.toEmoji
import com.example.waterwater.utils.TimeUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    existingReminder: Reminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(existingReminder?.title ?: "") }
    var description by remember { mutableStateOf(existingReminder?.description ?: "") }
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var selectedRepeatType by remember { mutableStateOf(existingReminder?.repeatType ?: RepeatType.NONE) }
    var repeatIntervalStr by remember { mutableStateOf(existingReminder?.repeatInterval?.toString() ?: "1") }
    var selectedMood by remember { mutableStateOf(existingReminder?.catMood ?: CatMood.HAPPY) }
    var showTimePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute, is24Hour = true)

    // 吉卜力调色盘
    val paperColor = Color(0xFFFFF9F0) // 暖奶油纸张色
    val accentColor = Color(0xFF4E342E) // 深咖色
    val greenColor = Color(0xFF689F38) // 森林绿

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = paperColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // 顶部装饰
                Text(text = "🐾 ${if (existingReminder != null) "修改任务" else "新任务"} 🐾", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
                
                Spacer(modifier = Modifier.height(20.dp))

                // 输入框
                GhibliTextField(value = title, onValueChange = { title = it; titleError = false }, label = "要做什么喵？", isError = titleError)
                Spacer(modifier = Modifier.height(12.dp))
                GhibliTextField(value = description, onValueChange = { description = it }, label = "备注小纸条...")

                Spacer(modifier = Modifier.height(20.dp))

                // 时间选择按钮 - 改为更像按钮的样式
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.6f), contentColor = accentColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 重复选择
                RepeatTypeSelector(selectedRepeatType) { selectedRepeatType = it }

                if (selectedRepeatType == RepeatType.HOURLY || selectedRepeatType == RepeatType.MINUTELY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GhibliTextField(
                        value = repeatIntervalStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) repeatIntervalStr = it },
                        label = "每隔多久？",
                        keyboardType = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 心情选择
                CatMoodSelector(selectedMood) { selectedMood = it }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("再想想", color = accentColor.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) { titleError = true; return@Button }
                            onConfirm(Reminder(
                                id = existingReminder?.id ?: 0, title = title.trim(), description = description.trim(),
                                timeInMillis = TimeUtils.calculateNextReminderTime(selectedHour, selectedMinute),
                                repeatType = selectedRepeatType, repeatInterval = repeatIntervalStr.toIntOrNull() ?: 1, catMood = selectedMood
                            ))
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("确定喵！", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(onDismiss = { showTimePicker = false }, onConfirm = {
            selectedHour = timePickerState.hour; selectedMinute = timePickerState.minute; showTimePicker = false
        }) { TimePicker(state = timePickerState) }
    }
}

@Composable
fun GhibliTextField(value: String, onValueChange: (String) -> Unit, label: String, isError: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text) {
    TextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = Color(0xFF4E342E).copy(alpha = 0.5f)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.5f), unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
            focusedIndicatorColor = Color(0xFF689F38), unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatTypeSelector(selectedType: RepeatType, onTypeSelected: (RepeatType) -> Unit) {
    val scrollState = rememberScrollState()
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RepeatType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedType, onClick = { onTypeSelected(type) },
                label = { Text(type.toDisplayString(), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF689F38).copy(alpha = 0.2f), selectedLabelColor = Color(0xFF689F38))
            )
        }
    }
}

@Composable
private fun CatMoodSelector(selectedMood: CatMood, onMoodSelected: (CatMood) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        CatMood.entries.forEach { mood ->
            val isSelected = mood == selectedMood
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onMoodSelected(mood) }
                    .background(if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Transparent).padding(8.dp)
            ) {
                Text(text = mood.toEmoji(), fontSize = 28.sp)
                Text(text = mood.toDisplayString(), fontSize = 11.sp, color = if (isSelected) Color(0xFF4E342E) else Color.Gray)
            }
        }
    }
}

@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onConfirm) { Text("确定") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }, text = { content() })
}
