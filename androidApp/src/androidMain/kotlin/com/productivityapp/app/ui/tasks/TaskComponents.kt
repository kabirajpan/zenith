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
import androidx.compose.ui.window.Dialog

@Composable
fun AddTaskModal(
    initialCategory: String, 
    onDismiss: () -> Unit, 
    onTaskCreated: (String, String, String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var estimatedMins by remember { mutableStateOf(30) }
    var energyLevel by remember { mutableStateOf("Medium") }
    
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
                    Text("New Task", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Category Selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Work", "Personal").forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF818CF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(cat, color = if (isSelected) Color(0xFF818CF8) else Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    
                    // Priority Selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Low", "High").forEach { prio ->
                                val isSelected = selectedPriority == prio
                                val color = if (prio == "High") Color(0xFFF87171) else Color(0xFF34D399)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { selectedPriority = prio }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(prio, color = if (isSelected) color else Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Intelligence Hub: Time & Energy
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estimate (Mins)", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${estimatedMins}m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = estimatedMins.toFloat(),
                                onValueChange = { estimatedMins = it.toInt() },
                                valueRange = 5f..120f,
                                steps = 11,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF818CF8), activeTrackColor = Color(0xFF818CF8))
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Energy Required", color = Color.Gray.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Low", "Mid", "High").forEach { energy ->
                                val isSelected = energyLevel.startsWith(energy)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFBBF24).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { energyLevel = energy }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(energy, color = if (isSelected) Color(0xFFFBBF24) else Color.Gray, fontSize = 10.sp)
                                }
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
                            "Create Task", 
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
