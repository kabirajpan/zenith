package com.productivityapp.app.ui.reminders

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.productivityapp.app.ui.tasks.ZenithCompactWheelPicker
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.productivityapp.model.Reminder
import com.productivityapp.model.RepeatInterval

@Composable
fun ReminderScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Today", "Scheduled", "Flagged")
    
    val reminders = RemindersRepository.reminders
    var showAddModal by remember { mutableStateOf(false) }
    var selectedReminderForDetail by remember { mutableStateOf<Reminder?>(null) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
 
    val filteredReminders by remember(searchQuery, selectedCategory) {
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
                    onDelete = { RemindersRepository.deleteReminder(item.id) },
                    onClick = { selectedReminderForDetail = item }
                )
            }
        }
    }

    if (showAddModal) {
        AddReminderModal(
            onDismiss = { showAddModal = false },
            onSave = { title, date, time, category, priority, repeatInterval, description ->
                RemindersRepository.addReminder(title, date, time, category, priority, repeatInterval = repeatInterval, description = description)
                showAddModal = false
            }
        )
    }

    selectedReminderForDetail?.let { item ->
        ReminderDetailModal(
            item = item,
            onDismiss = { selectedReminderForDetail = null },
            onDelete = { RemindersRepository.deleteReminder(item.id) },
            onToggle = { RemindersRepository.toggleReminder(item.id) },
            onEdit = { reminderToEdit = item }
        )
    }

    reminderToEdit?.let { item ->
        AddReminderModal(
            reminderToEdit = item,
            onDismiss = { reminderToEdit = null },
            onSave = { title, date, time, category, priority, repeatInterval, description ->
                RemindersRepository.updateReminder(item.id, title, date, time, category, priority, repeatInterval = repeatInterval, description = description)
                reminderToEdit = null
            }
        )
    }
}

@Composable
fun ReminderCard(item: Reminder, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
fun AddReminderModal(
    reminderToEdit: Reminder? = null,
    onDismiss: () -> Unit, 
    onSave: (String, String, String, String, String, RepeatInterval, String) -> Unit
) {
    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    
    // Robust Time Parsing
    val initialTime = reminderToEdit?.time ?: "09:00 AM"
    val timeParts = initialTime.split(":", " ")
    val initialHour = try { timeParts.getOrNull(0)?.toInt() ?: 9 } catch(e: Exception) { 9 }
    val initialMinute = try { timeParts.getOrNull(1)?.toInt() ?: 0 } catch(e: Exception) { 0 }
    val initialAmPm = if (timeParts.contains("PM")) "PM" else "AM"

    var selectedHour by remember { mutableStateOf(initialHour.toString().padStart(2, '0')) }
    var selectedMinute by remember { mutableStateOf(initialMinute.toString().padStart(2, '0')) }
    var selectedAmPm by remember { mutableStateOf(initialAmPm) }
    
    // Date Selection States (Month & Day)
    val now = java.time.LocalDate.now()
    var selectedMonth by remember { mutableStateOf(now.month.name.take(3).capitalize()) }
    var selectedDay by remember { mutableStateOf(now.dayOfMonth.toString()) }
    
    var category by remember { mutableStateOf(reminderToEdit?.category ?: "Personal") }
    var priority by remember { mutableStateOf(reminderToEdit?.priority ?: "Medium") }
    var repeatInterval by remember { mutableStateOf(reminderToEdit?.repeatInterval ?: RepeatInterval.NONE) }
    var description by remember { mutableStateOf(reminderToEdit?.description ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF818CF8), CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (reminderToEdit != null) "Edit Reminder" else "New Reminder", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                ReminderInputField(
                    label = "Title",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "What's the reminder?"
                )

                ReminderInputField(
                    label = "Description (Optional)",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Add more details..."
                )

                // Fused Category/Priority Section
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("CATEGORY", modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("PRIORITY", modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("REPEAT", modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ZenithCompactWheelPicker(
                                items = listOf("Personal", "Work", "Urgent", "Other"),
                                initialValue = category,
                                onValueChange = { category = it },
                                modifier = Modifier.weight(1f)
                            )
                            ZenithCompactWheelPicker(
                                items = listOf("Low", "Medium", "High"),
                                initialValue = priority,
                                onValueChange = { priority = it },
                                modifier = Modifier.weight(1f)
                            )
                            ZenithCompactWheelPicker(
                                items = RepeatInterval.values().map { it.name.lowercase().capitalize() },
                                initialValue = repeatInterval.name.lowercase().capitalize(),
                                onValueChange = { selected ->
                                    repeatInterval = RepeatInterval.valueOf(selected.uppercase())
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Fused Schedule Section
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("MONTH", modifier = Modifier.weight(1.2f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("DAY", modifier = Modifier.weight(0.8f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("HR", modifier = Modifier.weight(0.8f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("MIN", modifier = Modifier.weight(0.8f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                        Text("MODE", modifier = Modifier.weight(0.8f), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, letterSpacing = 1.sp)
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ZenithCompactWheelPicker(
                                items = java.time.Month.values().map { it.name.take(3).capitalize() },
                                initialValue = selectedMonth,
                                onValueChange = { selectedMonth = it },
                                modifier = Modifier.weight(1.2f)
                            )
                            ZenithCompactWheelPicker(
                                items = (1..31).map { it.toString() },
                                initialValue = selectedDay,
                                onValueChange = { selectedDay = it },
                                modifier = Modifier.weight(0.8f)
                            )
                            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.05f)))
                            ZenithCompactWheelPicker(
                                items = (1..12).toList(),
                                initialValue = selectedHour.toIntOrNull() ?: 7,
                                onValueChange = { selectedHour = it.toString() },
                                modifier = Modifier.weight(0.8f)
                            )
                            ZenithCompactWheelPicker(
                                items = (0..59).toList(),
                                initialValue = selectedMinute.toIntOrNull() ?: 0,
                                format = { it.toString().padStart(2, '0') },
                                onValueChange = { selectedMinute = it.toString().padStart(2, '0') },
                                modifier = Modifier.weight(0.8f)
                            )
                            ZenithCompactWheelPicker(
                                items = listOf("AM", "PM"),
                                initialValue = selectedAmPm,
                                onValueChange = { selectedAmPm = it },
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }

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
                                onSave(title, "$selectedMonth $selectedDay", "$selectedHour:$selectedMinute $selectedAmPm", category, priority, repeatInterval, description) 
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
                            if (reminderToEdit != null) "Update" else "Save", 
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
fun ReminderDetailModal(
    item: Reminder,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.category.uppercase(),
                            color = Color(0xFF818CF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
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
                        Text("Edit Reminder", color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRowItem("Priority", item.priority, Icons.Default.Flag)
                    DetailRowItem("Schedule", "${item.date} • ${item.time}", Icons.Default.Event)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { 
                            onToggle()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (item.isCompleted) Color.Gray.copy(alpha = 0.1f) else Color(0xFF818CF8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isCompleted) Icons.Default.Undo else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (item.isCompleted) Color.Gray else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (item.isCompleted) "Reactivate" else "Mark Done",
                            color = if (item.isCompleted) Color.Gray else Color.White,
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
fun RowScope.DetailRowItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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

@Composable
fun ReminderInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(label.uppercase(), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Box(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color.Gray.copy(alpha = 0.4f), fontSize = 11.sp)
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    cursorBrush = SolidColor(Color(0xFF818CF8)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
