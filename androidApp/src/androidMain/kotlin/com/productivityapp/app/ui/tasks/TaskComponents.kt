package com.productivityapp.app.ui.tasks

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.ExperimentalFoundationApi
import com.productivityapp.model.TaskPriority
import com.productivityapp.model.TaskCategory
import com.productivityapp.model.TaskStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTaskModal(
    initialCategory: String = "WORK",
    taskToEdit: com.productivityapp.model.Task? = null,
    onDismiss: () -> Unit, 
    onTaskCreated: (String, TaskCategory, TaskPriority, String, String, String, Boolean, Boolean) -> Unit
) {
    val safeInitialCategory = remember(initialCategory) {
        try {
            TaskCategory.valueOf(initialCategory.uppercase())
        } catch (e: Exception) {
            TaskCategory.WORK
        }
    }

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(taskToEdit?.priority ?: TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: safeInitialCategory) }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: "") }
    var dueTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "") }
    var hasReminder by remember { mutableStateOf(taskToEdit?.hasReminder ?: false) }
    var hasAlarm by remember { mutableStateOf(taskToEdit?.hasAlarm ?: false) }
    
    val focusRequester = remember { FocusRequester() }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (taskToEdit == null) "Create Task" else "Edit Task", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskSheetInput(label = "TITLE", value = title, placeholder = "What needs to be done?", onValueChange = { title = it }, isFocused = true, focusRequester = focusRequester)
                    TaskSheetInput(label = "DESCRIPTION", value = description, placeholder = "Add context...", onValueChange = { description = it })

                    // Fused Selection Section
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SectionLabel("PRIORITY", modifier = Modifier.weight(1f))
                            SectionLabel("CATEGORY", modifier = Modifier.weight(1f))
                        }

                        // Unified Compact Wheels
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ZenithCompactWheelPicker(
                                    items = TaskPriority.values().toList(), 
                                    initialValue = selectedPriority, 
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFFFF5722),
                                    format = { it.name },
                                    onValueChange = { selectedPriority = it }
                                )
                                ZenithCompactWheelPicker(
                                    items = TaskCategory.values().toList(), 
                                    initialValue = selectedCategory, 
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFFFF5722),
                                    format = { it.name },
                                    onValueChange = { selectedCategory = it }
                                )
                            }
                        }
                    }

                    // Schedule Section
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScheduleBlock(label = "DATE", value = if (dueDate.isEmpty()) "Today" else dueDate, subValue = "Due Date", icon = Icons.Default.CalendarToday, modifier = Modifier.weight(1f)) {
                            dueDate = "May 24" 
                        }
                        ScheduleBlock(label = "TIME", value = if (dueTime.isEmpty()) "09:00 AM" else dueTime, subValue = "Set Time", icon = Icons.Default.AccessTime, modifier = Modifier.weight(1f)) {
                            dueTime = "09:00 AM"
                        }
                    }

                    // Toggle Section (Left & Right)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            SheetSwitchItem(label = "Reminder", subLabel = "Notify", icon = Icons.Default.Notifications, isActive = hasReminder, onToggle = { hasReminder = it })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SheetSwitchItem(label = "Alarm", subLabel = "Wake", icon = Icons.Default.Alarm, isActive = hasAlarm, onToggle = { hasAlarm = it })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)), shape = RoundedCornerShape(12.dp), elevation = null) {
                        Text("Cancel", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { 
                        if (title.isNotBlank()) {
                            onTaskCreated(title, selectedCategory, selectedPriority, description, dueDate, dueTime, hasReminder, hasAlarm)
                        }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)), shape = RoundedCornerShape(12.dp), elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)) {
                        Text(if (taskToEdit == null) "Create" else "Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun TaskSheetInput(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit, isFocused: Boolean = false, focusRequester: FocusRequester? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SectionLabel(label)
        Spacer(modifier = Modifier.height(2.dp))
        Surface(modifier = Modifier.fillMaxWidth().then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier), color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (value.isNotEmpty() && isFocused) Color(0xFFFF5722) else Color.White.copy(alpha = 0.05f))) {
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                if (value.isEmpty()) Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 11.sp)
                androidx.compose.foundation.text.BasicTextField(value = value, onValueChange = onValueChange, textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp), cursorBrush = SolidColor(Color(0xFFFF5722)), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ZenithWheelPicker(
    items: List<T>,
    initialValue: T,
    modifier: Modifier = Modifier,
    format: (T) -> String = { it.toString() },
    onValueChange: (T) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val itemHeight = 35.dp
    val visibleItems = 3
    val startIndex = items.indexOf(initialValue).coerceAtLeast(0)
    
    // Infinite scroll setup
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % items.size) + startIndex - 1
    )

    // Pixel-perfect center tracking
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) -1
            else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { Math.abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: -1
            }
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex != -1) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onValueChange(items[centerIndex % items.size])
        }
    }

    // Gentle Manual Snapping (Less 'High' magnet feel)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            val targetItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                Math.abs((it.offset + it.size / 2f) - viewportCenter) 
            }
            
            targetItem?.let {
                listState.animateScrollToItem(it.index - 1)
            }
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems).wrapContentWidth()) {
        // Shared Highlight Bar
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 4.dp),
            color = Color.White.copy(alpha = 0.06f),
            shape = RoundedCornerShape(8.dp)
        ) {}

        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(Int.MAX_VALUE) { i ->
                val item = items[i % items.size]
                val isSelected = i == centerIndex

                // Smooth highlight color transition
                val animatedColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
                )
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == i }
                            if (itemInfo != null) {
                                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val distanceFromCenter = Math.abs(viewportCenter - itemCenter)
                                
                                val normalizedDistance = (distanceFromCenter / (itemHeight.toPx() * 2f)).coerceIn(0f, 1f)
                                
                                alpha = 1f - (normalizedDistance * 0.5f)
                                val s = 1.1f - (normalizedDistance * 0.15f)
                                scaleX = s
                                scaleY = s
                                rotationX = (viewportCenter - itemCenter) / 10f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = format(item),
                        color = animatedColor,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleBlock(label: String, value: String, subValue: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.height(40.dp).clickable { onClick() }, color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SheetSwitchItem(label: String, subLabel: String, icon: ImageVector, isActive: Boolean, onToggle: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SectionLabel(label.uppercase())
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(38.dp),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = if (isActive) Color(0xFFFF9100) else Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (subLabel.isEmpty()) label else subLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Switch(checked = isActive, onCheckedChange = onToggle, modifier = Modifier.scale(0.6f), colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF5722)))
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.Gray.copy(alpha = 0.5f),
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}

@Composable
fun TaskCard(task: com.productivityapp.model.Task, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == TaskStatus.DONE,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFF9100),
                    uncheckedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.status == TaskStatus.DONE) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            Surface(
                color = when(task.priority) {
                    TaskPriority.HIGH -> Color(0xFFFF5252).copy(alpha = 0.1f)
                    TaskPriority.MEDIUM -> Color(0xFFFF9100).copy(alpha = 0.1f)
                    TaskPriority.LOW -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = task.priority.name,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = when(task.priority) {
                        TaskPriority.HIGH -> Color(0xFFFF5252)
                        TaskPriority.MEDIUM -> Color(0xFFFF9100)
                        TaskPriority.LOW -> Color(0xFF4CAF50)
                    },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TaskDetailModal(
    task: com.productivityapp.model.Task,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(task.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(task.description, color = Color.Gray, fontSize = 14.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red.copy(alpha = 0.1f))) {
                        Text("Delete", color = Color.Red)
                    }
                    Button(onClick = onEdit) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ZenithCompactWheelPicker(
    items: List<T>,
    initialValue: T,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF38BDF8),
    format: (T) -> String = { it.toString() },
    onValueChange: (T) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val itemHeight = 32.dp
    val visibleItems = 3
    val startIndex = items.indexOf(initialValue).coerceAtLeast(0)
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % items.size) + startIndex - 1
    )

    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) -1
            else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { Math.abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: -1
            }
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex != -1) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onValueChange(items[centerIndex % items.size])
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            val targetItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                Math.abs((it.offset + it.size / 2f) - viewportCenter) 
            }
            targetItem?.let { listState.animateScrollToItem(it.index - 1) }
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems).wrapContentWidth()) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(Int.MAX_VALUE) { i ->
                val item = items[i % items.size]
                val isSelected = i == centerIndex

                val animatedColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) activeColor else Color.White.copy(alpha = 0.2f),
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
                )
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .padding(horizontal = 2.dp)
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == i }
                            if (itemInfo != null) {
                                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val distanceFromCenter = Math.abs(viewportCenter - itemCenter)
                                val normalizedDistance = (distanceFromCenter / (itemHeight.toPx() * 2f)).coerceIn(0f, 1f)
                                alpha = 1f - (normalizedDistance * 0.4f)
                                val s = 1.08f - (normalizedDistance * 0.12f)
                                scaleX = s
                                scaleY = s
                                rotationX = (viewportCenter - itemCenter) / 12f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = format(item),
                        color = animatedColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
