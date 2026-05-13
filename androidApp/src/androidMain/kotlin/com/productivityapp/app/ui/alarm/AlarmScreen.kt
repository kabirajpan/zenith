package com.productivityapp.app.ui.alarm

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

data class AlarmItem(
    val id: Int,
    val time: String,
    val label: String,
    val days: List<String>,
    val isEnabled: Boolean
)

@Composable
fun AlarmScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Active", "Recurring")
    var showAddModal by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<AlarmItem?>(null) }
    
    val alarms = remember { mutableStateListOf<AlarmItem>() }

    val filteredAlarms by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            alarms.filter { alarm ->
                val matchesSearch = alarm.label.contains(searchQuery, ignoreCase = true) || 
                                   alarm.time.contains(searchQuery, ignoreCase = true)
                val matchesCategory = when(selectedCategory) {
                    "Active" -> alarm.isEnabled
                    "Recurring" -> alarm.days.size > 1
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
        // Compact Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24), // Gold for Alarms
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alarms",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { 
                alarmToEdit = null
                showAddModal = true 
            }) {
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
                        Text("Search labels or times...", color = Color.Gray, fontSize = 13.sp)
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF818CF8)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Compact Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFFBBF24).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color(0xFFFBBF24) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredAlarms) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { 
                        val index = alarms.indexOfFirst { it.id == alarm.id }
                        if (index != -1) {
                            alarms[index] = alarms[index].copy(isEnabled = !alarms[index].isEnabled)
                        }
                    },
                    onClick = {
                        alarmToEdit = alarm
                        showAddModal = true
                    }
                )
            }
        }
    }

    if (showAddModal) {
        AlarmEntryModal(
            initialAlarm = alarmToEdit,
            onDismiss = { 
                showAddModal = false
                alarmToEdit = null
            },
            onSave = { time, label, days ->
                if (alarmToEdit != null) {
                    val index = alarms.indexOfFirst { it.id == alarmToEdit!!.id }
                    if (index != -1) {
                        alarms[index] = alarmToEdit!!.copy(time = time, label = label, days = days)
                    }
                } else {
                    alarms.add(AlarmItem(alarms.size + 1, time, label, days, true))
                }
                showAddModal = false
                alarmToEdit = null
            }
        )
    }
}

@Composable
fun AlarmEntryModal(
    initialAlarm: AlarmItem? = null,
    onDismiss: () -> Unit, 
    onSave: (String, String, List<String>) -> Unit
) {
    // Parse initial time if editing
    val initialHour = initialAlarm?.time?.split(":")?.get(0)?.toIntOrNull() ?: 7
    val initialMinute = initialAlarm?.time?.split(":")?.get(1)?.split(" ")?.get(0)?.toIntOrNull() ?: 0
    val initialAmPm = if (initialAlarm?.time?.contains("PM") == true) "PM" else "AM"

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var selectedAmPm by remember { mutableStateOf(initialAmPm) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var selectedDays by remember { mutableStateOf(initialAlarm?.days?.toSet() ?: setOf<String>()) }
    val allDays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.80f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFBBF24), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialAlarm != null) "Edit Alarm" else "New Alarm", 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }

                // Apple-inspired Wheel Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Selection Highlight
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.9f).height(40.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(8.dp)
                    ) {}

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPicker(
                            items = (1..12).toList(),
                            initialValue = 7,
                            onValueChange = { selectedHour = it }
                        )
                        Text(":", color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        WheelPicker(
                            items = (0..59).toList(),
                            initialValue = 0,
                            format = { it.toString().padStart(2, '0') },
                            onValueChange = { selectedMinute = it }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        WheelPicker(
                            items = listOf("AM", "PM"),
                            initialValue = "AM",
                            onValueChange = { selectedAmPm = it }
                        )
                    }
                }

                // Label Input
                AlarmInputField(
                    label = "Alarm Label",
                    value = label,
                    onValueChange = { label = it },
                    placeholder = "e.g. Morning Routine"
                )

                // Day Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Repeat", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        allDays.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.05f))
                                    .clickable { 
                                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Standardized Button Row (Match Vault)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            val formattedTime = "${selectedHour}:${selectedMinute.toString().padStart(2, '0')} $selectedAmPm"
                            onSave(formattedTime, label, selectedDays.toList()) 
                        },
                        modifier = Modifier.weight(0.6f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFFBBF24),
                            disabledBackgroundColor = Color(0xFFFBBF24)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (initialAlarm != null) "Update Alarm" else "Save Alarm", 
                            color = Color(0xFF0F172A).copy(alpha = if (label.isNotBlank() || initialAlarm != null) 1f else 0.5f), 
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
fun AlarmInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Box(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color.Gray.copy(alpha = 0.4f), fontSize = 13.sp)
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFBBF24)),
                    modifier = Modifier.fillMaxWidth()
                )
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

    Box(modifier = Modifier.height(itemHeight * visibleItems).width(50.dp)) {
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
                        color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                        fontSize = if (isSelected) 22.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.graphicsLayer(
                            alpha = if (isSelected) 1f else 0.5f,
                            scaleX = if (isSelected) 1.1f else 0.9f,
                            scaleY = if (isSelected) 1.1f else 0.9f
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmItem, onToggle: () -> Unit, onClick: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    
    // Update time every minute
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now()
            kotlinx.coroutines.delay(60000)
        }
    }

    val timeUntil = remember(alarm.time, alarm.isEnabled, currentTime) {
        if (!alarm.isEnabled) return@remember "Alarm disabled"
        
        try {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            val alarmTime = LocalTime.parse(alarm.time, formatter)
            
            var diffMinutes = ChronoUnit.MINUTES.between(currentTime, alarmTime)
            if (diffMinutes <= 0) {
                diffMinutes += 24 * 60 // Add 24 hours if alarm is for tomorrow
            }
            
            val hours = diffMinutes / 60
            val mins = diffMinutes % 60
            "Alarm in ${hours}h ${mins}m"
        } catch (e: Exception) {
            "Alarm set"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.time, 
                        color = if (alarm.isEnabled) Color.White else Color.Gray, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    if (!alarm.label.isBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• " + alarm.label, 
                            color = Color.Gray, 
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
                
                Text(
                    text = timeUntil,
                    color = if (alarm.isEnabled) Color(0xFFFBBF24).copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Day Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val allDays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
                    allDays.forEach { day ->
                        val isDayActive = alarm.days.contains(day)
                        Box(
                            modifier = Modifier
                                .size(20.dp) // Increased slightly for better centering
                                .clip(CircleShape)
                                .background(if (isDayActive) Color(0xFFFBBF24).copy(alpha = 0.2f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(1),
                                color = if (isDayActive) Color(0xFFFBBF24) else Color.Gray.copy(alpha = 0.3f),
                                fontSize = 10.sp,
                                fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Normal,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
            
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFBBF24),
                    checkedTrackColor = Color(0xFFFBBF24).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                )
            )
        }
    }
}
