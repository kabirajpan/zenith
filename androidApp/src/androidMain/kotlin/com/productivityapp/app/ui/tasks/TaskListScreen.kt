package com.productivityapp.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskListScreen() {
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = listOf("All", "Work", "Personal", "Health")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Tasks",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    CategoryChip(
                        label = category,
                        isSelected = selectedCategory == index,
                        onClick = { selectedCategory = index }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dummyTasks) { task ->
                    TaskCard(task)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaskCard(task: TaskItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.isCompleted) Color.Gray else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = task.time,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        
        if (task.priority == "High") {
            Text(
                text = "!!!",
                color = Color(0xFFF87171),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class TaskItem(
    val title: String,
    val time: String,
    val priority: String,
    val isCompleted: Boolean = false
)

val dummyTasks = listOf(
    TaskItem("Revise Zenith Architecture", "09:00 AM", "High"),
    TaskItem("Team Sync Meeting", "11:30 AM", "Medium"),
    TaskItem("Lunch with Sarah", "01:00 PM", "Low", true),
    TaskItem("Workout - Core Session", "05:00 PM", "Medium"),
    TaskItem("Read 20 pages of Rust", "09:00 PM", "Low"),
    TaskItem("Daily Reflection", "10:30 PM", "Medium")
)
