package com.productivityapp.app.ui.reminders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productivityapp.app.ui.ai.ActionType
import com.productivityapp.app.ui.ai.ProposedAction
import com.productivityapp.app.ui.common.VerticalWheelPicker

@Composable
fun ReminderActionWidget(
    action: ProposedAction,
    onConfirm: (ProposedAction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(action.category ?: "Personal") }
    var selectedPriority by remember { mutableStateOf(action.priority ?: "Medium") }
    var isConfirmed by remember { mutableStateOf(false) }

    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isConfirmed) Color(0xFF22C55E).copy(alpha = 0.3f) else Color(0xFF818CF8).copy(alpha = 0.2f)),
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        if (isConfirmed) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Reminder Set", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF818CF8).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(action.type) {
                                ActionType.CREATE_REMINDER -> Icons.Default.Notifications
                                ActionType.DELETE -> Icons.Default.Delete
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when(action.type) {
                                ActionType.CREATE_REMINDER -> "New Reminder"
                                ActionType.DELETE -> "Delete Reminder"
                                else -> "Reminder Action"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = action.title ?: "Reminder Action",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (action.type == ActionType.CREATE_REMINDER) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = listOf("High", "Medium", "Low"),
                                initialSelection = selectedPriority,
                                onItemSelected = { selectedPriority = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Category", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = listOf("Work", "Personal", "Finance", "Social", "Health", "Travel"),
                                initialSelection = selectedCategory,
                                onItemSelected = { selectedCategory = it }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Dismiss", color = Color.Gray, fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = { 
                            onConfirm(action.copy(
                                category = selectedCategory,
                                priority = selectedPriority
                            )) 
                            isConfirmed = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF818CF8).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Confirm", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
