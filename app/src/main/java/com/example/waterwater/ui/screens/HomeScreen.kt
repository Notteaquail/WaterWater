package com.example.waterwater.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterwater.ui.components.ReminderCard
import com.example.waterwater.viewmodel.ReminderViewModel
import com.example.waterwater.ui.components.AddReminderDialog
import com.example.waterwater.ui.components.DeskDialog
import com.example.waterwater.model.CatInstance
import com.example.waterwater.viewmodel.DeskViewModel
import com.example.waterwater.viewmodel.DeskViewModelFactory
import com.example.waterwater.WaterWaterApp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    modifier: Modifier = Modifier
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as WaterWaterApp
    val deskViewModel: DeskViewModel = viewModel(factory = DeskViewModelFactory(app))
    
    val reminders by viewModel.reminders.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val currentReminder by viewModel.currentReminder.collectAsState()
    
    val showDeskDialog by deskViewModel.showDeskDialog.collectAsState()
    val deskPos by deskViewModel.deskOffset.collectAsState()

    val listState = rememberLazyListState()
    val getScrollOffset = remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex * 200f) + listState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val ghibliAccent = Color(0xFF5D4037)
    val ghibliWood = Color(0xFF8D6E63)
    val scrollBaseColor = Color(0xFFBCAA91)
    val scrollBorder = ghibliAccent.copy(alpha = 0.2f)

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val panelHeight = screenHeight * 0.5f 
    val handleHeight = 100.dp
    val topAxisHeight = 16.dp

    var isExpanded by remember { mutableStateOf(false) }

    val panelOffset by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else -(panelHeight + topAxisHeight),
        animationSpec = tween(durationMillis = 600),
        label = "PanelScroll"
    )

    Box(modifier = modifier.fillMaxSize()) {
        MainScene(scrollOffsetProvider = { getScrollOffset.value })

        // --- 猫咪层 (中层) ---
        viewModel.cats.forEach { cat ->
            DraggableCatOverlay(
                cat = cat,
                scrollOffsetProvider = { getScrollOffset.value },
                onDragEnd = { viewModel.saveCatPosition(cat) }
            )
        }

        // --- 秘密课桌层 (中层, 可拖拽) ---
        DraggableDeskOverlay(
            currentOffset = deskPos,
            scrollOffsetProvider = { getScrollOffset.value },
            onClick = { deskViewModel.openDesk() },
            onDragEnd = { deskViewModel.updateDeskPosition(it) }
        )

        // --- 任务卷轴层 (顶层) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, panelOffset.toPx().roundToInt()) }
                .padding(horizontal = 12.dp) 
                .shadow(8.dp, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            scrollBaseColor.copy(alpha = 0.4f),
                            scrollBaseColor.copy(alpha = 0.6f),
                            scrollBaseColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(1.dp, scrollBorder, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topAxisHeight)
                    .background(brush = Brush.verticalGradient(listOf(ghibliAccent.copy(alpha = 0.8f), ghibliWood.copy(alpha = 0.8f))))
            )

            Box(modifier = Modifier.fillMaxWidth().height(panelHeight)) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        if (isExpanded) {
                            FloatingActionButton(
                                onClick = { viewModel.showAddDialog() },
                                containerColor = ghibliWood,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            ) { Icon(Icons.Default.Add, contentDescription = null) }
                        }
                    }
                ) { paddingValues ->
                    if (reminders.isNotEmpty()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(items = reminders, key = { _, item -> item.id }) { _, reminder ->
                                AnimatedAppearance {
                                    ReminderCard(
                                        reminder = reminder,
                                        onToggle = { viewModel.toggleReminder(reminder) },
                                        onClick = { viewModel.showEditDialog(reminder) },
                                        onDelete = { viewModel.deleteReminder(reminder) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(handleHeight)
                    .clickable { isExpanded = !isExpanded }
                    .background(brush = Brush.verticalGradient(colors = listOf(scrollBaseColor.copy(alpha = 0.7f), scrollBaseColor.copy(alpha = 0.85f), ghibliWood.copy(alpha = 0.7f))))
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(text = "猫猫任务 📜", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    if (showDialog) {
        AddReminderDialog(existingReminder = currentReminder, onDismiss = { viewModel.dismissDialog() }, onConfirm = { reminder -> viewModel.saveReminder(reminder) })
    }

    if (showDeskDialog) {
        DeskDialog(viewModel = deskViewModel, onDismiss = { deskViewModel.closeDesk() })
    }
}

@Composable
fun DraggableDeskOverlay(
    currentOffset: androidx.compose.ui.geometry.Offset,
    scrollOffsetProvider: () -> Float,
    onClick: () -> Unit,
    onDragEnd: (androidx.compose.ui.geometry.Offset) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(currentOffset.x) }
    var offsetY by remember { mutableFloatStateOf(currentOffset.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                translationY = -scrollOffsetProvider() * 0.3f 
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = { onDragEnd(androidx.compose.ui.geometry.Offset(offsetX, offsetY)) }
                )
            }
            .size(width = 74.dp, height = 80.dp)
            .border(width = 1.dp, color = Color(0xFF8D6E63).copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🖋️", fontSize = 18.sp, modifier = Modifier.alpha(0.6f))
            Text(text = "秘密", fontSize = 8.sp, color = Color(0xFF5D4037).copy(alpha = 0.5f), letterSpacing = 1.sp)
        }
    }
}

@Composable
fun DraggableCatOverlay(
    cat: CatInstance,
    scrollOffsetProvider: () -> Float,
    onDragEnd: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(cat.offset.x) }
    var offsetY by remember { mutableFloatStateOf(cat.offset.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                translationY = -scrollOffsetProvider() * 0.3f
                scaleX = cat.scale
                scaleY = cat.scale
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        cat.offset = androidx.compose.ui.geometry.Offset(offsetX, offsetY)
                    },
                    onDragEnd = { onDragEnd() }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DynamicCat(breed = cat.breed, catScale = 1.0f, isThinkingEnabled = cat.isThinkingEnabled)
        }
    }
}

@Composable
fun AnimatedAppearance(content: @Composable () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(250)), exit = fadeOut(tween(100))) { content() }
}
