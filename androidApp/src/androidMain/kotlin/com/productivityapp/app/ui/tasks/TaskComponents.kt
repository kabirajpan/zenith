package com.productivityapp.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddTaskModal(initialCategory: String, onDismiss: () -> Unit, onTaskCreated: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Text("Create New Task", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What's on your mind?", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    backgroundColor = Color.White.copy(alpha = 0.03f),
                    textColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Selection
            Text("Category", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Work", "Personal", "Health").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            cat, 
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Priority Selection
            Text("Priority", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High").forEach { prio ->
                    val isSelected = selectedPriority == prio
                    val color = when(prio) {
                        "High" -> Color(0xFFF87171)
                        "Medium" -> Color(0xFFFBBF24)
                        else -> Color(0xFF34D399)
                    }
                    Surface(
                        color = if (isSelected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { selectedPriority = prio }
                    ) {
                        Text(
                            prio, 
                            color = if (isSelected) color else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color.Gray)
                }
                Button(
                    onClick = { if (title.isNotBlank()) onTaskCreated(title, selectedCategory, selectedPriority) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF818CF8)),
                    shape = RoundedCornerShape(14.dp),
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Text("Create Task", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
