package com.productivityapp.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog

@Composable
fun AddTaskModal(
    initialCategory: String, 
    taskToEdit: TaskItem? = null,
    onDismiss: () -> Unit, 
    onTaskCreated: (String, String, String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: initialCategory) }
    var selectedPriority by remember { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var estimatedMins by remember { mutableStateOf(taskToEdit?.estimatedMins ?: 30) }
    var energyLevel by remember { mutableStateOf(taskToEdit?.energyLevel ?: "Medium") }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.90f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF818CF8), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (taskToEdit != null) "Edit Task" else "New Task", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                TaskInputField(
                    label = "What's on your mind?",
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.focusRequester(focusRequester)
                )

                TaskInputField(
                    label = "Context / Notes (Optional)",
                    value = description,
                    onValueChange = { description = it }
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category Selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        VerticalWheelPicker(
                            options = listOf("Work", "Personal", "Finance", "Social", "Health", "Travel"),
                            initialSelection = selectedCategory,
                            onItemSelected = { selectedCategory = it }
                        )
                    }
                    
                    // Priority Selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        VerticalWheelPicker(
                            options = listOf("Low", "Medium", "High"),
                            initialSelection = selectedPriority,
                            onItemSelected = { selectedPriority = it }
                        )
                    }

                    // Energy Selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Energy", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        VerticalWheelPicker(
                            options = listOf("Low", "Medium", "High"),
                            initialSelection = energyLevel,
                            onItemSelected = { energyLevel = it }
                        )
                    }
                }

                // Estimate Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Estimate (Mins)", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${estimatedMins}m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                        Slider(
                            value = estimatedMins.toFloat(),
                            onValueChange = { estimatedMins = it.toInt() },
                            valueRange = 5f..120f,
                            steps = 11,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF818CF8), activeTrackColor = Color(0xFF818CF8)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.4f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { 
                            if (title.isNotBlank()) onTaskCreated(title, selectedCategory, selectedPriority, description, estimatedMins, energyLevel) 
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF818CF8),
                            disabledBackgroundColor = Color(0xFF818CF8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Text(
                            if (taskToEdit != null) "Update Task" else "Create Task", 
                            color = if (title.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF818CF8)),
                modifier = modifier.padding(12.dp).fillMaxWidth()
            )
        }
    }
}
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VerticalWheelPicker(
    options: List<String>,
    initialSelection: String,
    onItemSelected: (String) -> Unit
) {
    val itemHeight = 32.dp
    val visibleItems = 3
    val initialIndex = options.indexOf(initialSelection).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            if (centerIndex in options.indices) {
                onItemSelected(options[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(itemHeight * visibleItems)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Selection Highlight (Glassy lines)
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(itemHeight))
            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(options.size) { index ->
                val option = options[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            val itemOffset = listState.layoutInfo.visibleItemsInfo
                                .find { it.index == index }
                                ?.let { it.offset + it.size / 2 } ?: 0
                            val viewportCenter = listState.layoutInfo.viewportEndOffset / 2
                            val distanceFromCenter = kotlin.math.abs(itemOffset - viewportCenter).toFloat()
                            val normalizedDistance = (distanceFromCenter / (itemHeight.toPx() * 1.5f)).coerceIn(0f, 1f)
                            
                            alpha = 1f - (normalizedDistance * 0.6f)
                            scaleX = 1f - (normalizedDistance * 0.2f)
                            scaleY = 1f - (normalizedDistance * 0.2f)
                            rotationX = normalizedDistance * 45f * (if (itemOffset < viewportCenter) 1f else -1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
@Composable
fun TaskDetailModal(
    task: TaskItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.90f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.category.uppercase(),
                            color = Color(0xFF818CF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { 
                        onEdit()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Details", color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "No additional notes for this task.",
                        color = Color.Gray.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailItem("Priority", task.priority, Icons.Default.Star)
                    DetailItem("Estimate", "${task.estimatedMins}m", Icons.Default.Timer)
                    DetailItem("Energy", task.energyLevel, Icons.Default.FlashOn)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { 
                            onToggle()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (task.isCompleted) Color.Gray.copy(alpha = 0.1f) else Color(0xFF818CF8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.Undo else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (task.isCompleted) Color.Gray else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (task.isCompleted) "Reopen Task" else "Complete Task",
                            color = if (task.isCompleted) Color.Gray else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = Color.Gray, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
