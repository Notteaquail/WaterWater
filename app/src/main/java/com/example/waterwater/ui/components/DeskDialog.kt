package com.example.waterwater.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.waterwater.viewmodel.DeskViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeskDialog(
    viewModel: DeskViewModel,
    onDismiss: () -> Unit
) {
    val partnerId by viewModel.partnerId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    var bindingCodeInput by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // 列表状态，用于自动滚动
    val listState = rememberLazyListState()

    val ghibliYellow = Color(0xFFFFF9C4) 
    val ghibliDarkWood = Color(0xFF5D4037)
    val ghibliPaper = Color(0xFFFFFDE7)
    val ghibliGreen = Color(0xFF689F38)

    // 自动滚动逻辑：当消息数量变化时，滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.55f)
                .border(2.dp, ghibliDarkWood.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ghibliYellow)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                if (partnerId == null) {
                    // === 未绑定界面 (保持不变) ===
                    DeskBindingView(
                        myId = viewModel.myUserId,
                        onDismiss = onDismiss,
                        ghibliDarkWood = ghibliDarkWood,
                        ghibliGreen = ghibliGreen,
                        bindingCodeInput = bindingCodeInput,
                        onCodeChange = { bindingCodeInput = it },
                        onBind = { viewModel.bindPartner(bindingCodeInput) },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(viewModel.myUserId))
                            Toast.makeText(context, "契约码已复制喵！", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // === 已绑定界面：秘密留言板 ===
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { viewModel.unbind() },
                            modifier = Modifier.align(Alignment.TopStart).size(32.dp)
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = "解除", tint = Color.Red.copy(alpha = 0.4f))
                        }
                        
                        Text(
                            text = "秘密课桌 📜",
                            color = ghibliDarkWood,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = ghibliDarkWood.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 留言展示区
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(ghibliPaper.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .border(1.dp, ghibliDarkWood.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                        color = ghibliDarkWood.copy(alpha = 0.4f),
                                        fontSize = 10.sp
                                    )
                                    // 使用刻字动效组件
                                    CarvingText(
                                        text = msg.content,
                                        color = ghibliDarkWood,
                                        isNew = msg.timestamp > (System.currentTimeMillis() - 5000) // 5秒内的新消息才有动效
                                    )
                                    HorizontalDivider(
                                        color = ghibliDarkWood.copy(alpha = 0.05f),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        textStyle = TextStyle(color = ghibliDarkWood, fontSize = 15.sp),
                        placeholder = { Text("在木板上刻下心事...", color = ghibliDarkWood.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = ghibliDarkWood,
                            unfocusedTextColor = ghibliDarkWood,
                            focusedContainerColor = Color.White.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                            focusedIndicatorColor = ghibliGreen,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ghibliGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("刻下留言", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 刻字动效组件：模拟文字一点点显现的效果
 */
@Composable
fun CarvingText(text: String, color: Color, isNew: Boolean) {
    var visibleText by remember { mutableStateOf(if (isNew) "" else text) }

    LaunchedEffect(text) {
        if (isNew && visibleText != text) {
            for (i in 1..text.length) {
                visibleText = text.substring(0, i)
                // 模拟人写字的速度：稍微慢一些，并且带有一点随机波动，更自然
                delay(Random.nextLong(150, 300)) 
            }
        } else {
            visibleText = text
        }
    }

    Text(
        text = visibleText,
        color = color,
        fontSize = 16.sp,
        fontFamily = FontFamily.Serif,
        lineHeight = 20.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeskBindingView(
    myId: String,
    onDismiss: () -> Unit,
    ghibliDarkWood: Color,
    ghibliGreen: Color,
    bindingCodeInput: String,
    onCodeChange: (String) -> Unit,
    onBind: () -> Unit,
    onCopy: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = ghibliDarkWood.copy(alpha = 0.5f))
        }
        Text(
            text = "结成契约 🖋️", 
            color = ghibliDarkWood, 
            fontSize = 20.sp, 
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "我的契约码 (点击复制)", color = ghibliDarkWood.copy(alpha = 0.6f), fontSize = 12.sp)
        Surface(
            color = ghibliDarkWood.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onCopy() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = myId,
                    color = ghibliDarkWood,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    tint = ghibliDarkWood.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = bindingCodeInput,
            onValueChange = { onCodeChange(it.uppercase()) },
            label = { Text("输入对方的契约码") },
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ghibliGreen,
                unfocusedBorderColor = ghibliDarkWood.copy(alpha = 0.3f),
                focusedLabelColor = ghibliGreen,
                focusedTextColor = ghibliDarkWood,
                unfocusedTextColor = ghibliDarkWood
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBind,
            colors = ButtonDefaults.buttonColors(containerColor = ghibliGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("开启私密空间")
        }
    }
}
