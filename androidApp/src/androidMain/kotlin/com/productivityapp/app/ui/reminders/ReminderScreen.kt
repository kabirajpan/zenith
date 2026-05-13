package com.productivityapp.app.ui.reminders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ReminderScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Today", "Scheduled", "Flagged")
    
    val reminders = RemindersRepository.reminders
    var showAddModal by remember { mutableStateOf(false) }

    val filteredReminders by remember {
        derivedStateOf {
            reminders.filter { item ->
                val matchesSearch = item.title.contains(searchQuery, ignoreCase = true)
                val matchesCategory = when(selectedCategory) {
                    "All" -> true
                    "Today" -> item.date == "Today"
                    "Scheduled" -> item.date != "Today"
                    "Flagged" -> item.priority == "High"
                    else -> true
                }
                matchesSearch && matchesCategory
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reminders",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { showAddModal = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Compact Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text("Search reminders...", color = Color.Gray, fontSize = 13.sp)
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF818CF8)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF818CF8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color(0xFF818CF8) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredReminders) { item ->
                ReminderCard(
                    item = item,
                    onToggle = { RemindersRepository.toggleReminder(item.id) },
                    onDelete = { RemindersRepository.deleteReminder(item.id) }
                )
            }
        }
    }

    if (showAddModal) {
        AddReminderModal(
            onDismiss = { showAddModal = false },
            onSave = { title, date, time, category, priority ->
                RemindersRepository.addReminder(title, date, time, category, priority)
                showAddModal = false
            }
        )
    }
}

@Composable
fun ReminderCard(item: ReminderItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (item.isCompleted) Color(0xFF818CF8) else Color.Transparent)
                    .clickable { onToggle() }
                    .border(
                        width = 1.5.dp,
                        color = if (item.isCompleted) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = if (item.isCompleted) Color.Gray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = if (item.isCompleted) TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else TextStyle.Default
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.date} • ${item.time}", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(3.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.category, color = Color(0xFF818CF8).copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Priority Indicator
            if (item.priority == "High") {
                Icon(Icons.Default.Flag, contentDescription = "High Priority", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AddReminderModal(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    
    // Time Selection States
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedAmPm by remember { mutableStateOf("AM") }
    
    // Date Selection States (Month & Day)
    val now = java.time.LocalDate.now()
    var selectedMonth by remember { mutableStateOf(now.month) }
    var selectedDay by remember { mutableIntStateOf(now.dayOfMonth) }
    
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.80f),
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
                    Text("New Reminder", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                ReminderInputField(
                    label = "Remind me about...",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "e.g. Call the team"
                )
                
                // Date & Time Wheel Pickers
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Date & Time", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Wheels (Month & Day)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WheelPicker(
                                items = java.time.Month.values().toList(),
                                initialValue = selectedMonth,
                                format = { it.name.take(3) },
                                onValueChange = { selectedMonth = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            WheelPicker(
                                items = (1..selectedMonth.length(now.isLeapYear)).toList(),
                                initialValue = selectedDay,
                                onValueChange = { selectedDay = it }
                            )
                        }

                        // Vertical Divider
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))

                        // Time Wheels
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WheelPicker(
                                items = (1..12).toList(),
                                initialValue = selectedHour,
                                onValueChange = { selectedHour = it }
                            )
                            Text(":", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
                            WheelPicker(
                                items = (0..59).toList(),
                                initialValue = selectedMinute,
                                format = { it.toString().padStart(2, '0') },
                                onValueChange = { selectedMinute = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            WheelPicker(
                                items = listOf("AM", "PM"),
                                initialValue = selectedAmPm,
                                onValueChange = { selectedAmPm = it }
                            )
                        }
                    }
                }

                Column {
                    Text("Priority", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Medium", "High").forEach { prio ->
                            val isSelected = priority == prio
                            val color = when(prio) {
                                "High" -> Color(0xFFF87171)
                                "Medium" -> Color(0xFFFBBF24)
                                else -> Color(0xFF34D399)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                    .border(if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)) else androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(10.dp))
                                    .clickable { priority = prio }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(prio, color = if (isSelected) color else Color.Gray, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
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
                            if (title.isNotBlank()) {
                                val dateStr = if (selectedMonth == now.month && selectedDay == now.dayOfMonth) "Today" 
                                             else "${selectedMonth.name.take(3)} $selectedDay"
                                val timeStr = "${selectedHour}:${selectedMinute.toString().padStart(2, '0')} $selectedAmPm"
                                onSave(title, dateStr, timeStr, category, priority) 
                            }
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
                            "Save", 
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
fun <T> WheelPicker(
    items: List<T>,
    initialValue: T,
    format: (T) -> String = { it.toString() },
    onValueChange: (T) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val itemHeight = 40.dp
    val visibleItems = 3
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % items.size) + items.indexOf(initialValue) - 1
    )

    val centerIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + 1
        }
    }

    LaunchedEffect(centerIndex) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        onValueChange(items[centerIndex % items.size])
    }

    // Snapping Logic
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(centerIndex - 1)
        }
    }

    Box(modifier = Modifier.height(itemHeight * visibleItems).width(if (items.size > 20) 45.dp else 40.dp)) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(Int.MAX_VALUE) { i ->
                val item = items[i % items.size]
                val isSelected = i == centerIndex
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = format(item),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                        fontSize = if (isSelected) 15.sp else 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.scale(if (isSelected) 1.1f else 1f)
                    )
                }
            }
        }
        
        // Selection Overlays (Subtle lines)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.05f))
                .align(Alignment.Center)
                .offset(y = (-20).dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.05f))
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )
    }
}

@Composable
fun ReminderInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Box(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color.Gray.copy(alpha = 0.4f), fontSize = 13.sp)
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF818CF8)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
