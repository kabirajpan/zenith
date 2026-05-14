package com.productivityapp.app.ui.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productivityapp.app.ui.common.VerticalWheelPicker
import com.productivityapp.model.TaskCategory
import com.productivityapp.model.TaskPriority
import com.productivityapp.app.ui.ai.ProposedAction
import com.productivityapp.app.ui.ai.ActionType

@Composable
fun TaskActionWidget(
    action: ProposedAction,
    onConfirm: (ProposedAction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { 
        mutableStateOf(com.productivityapp.model.TaskCategory.entries.find { it.name.equals(action.category, ignoreCase = true) } ?: com.productivityapp.model.TaskCategory.WORK) 
    }
    var selectedPriority by remember { 
        mutableStateOf(com.productivityapp.model.TaskPriority.entries.find { it.name.equals(action.priority, ignoreCase = true) } ?: com.productivityapp.model.TaskPriority.MEDIUM) 
    }
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
                Text("Task Updated", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                ActionType.CREATE -> Icons.Default.Add
                                ActionType.TOGGLE -> Icons.Default.CheckCircle
                                ActionType.DELETE -> Icons.Default.Delete
                                else -> Icons.Default.Edit
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
                                ActionType.CREATE -> "New Task"
                                ActionType.TOGGLE -> "Toggle Task"
                                ActionType.DELETE -> "Delete Task"
                                else -> "Update Task"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = action.title ?: "Task Action",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (action.type == ActionType.CREATE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = TaskPriority.entries.map { it.name },
                                initialSelection = selectedPriority.name,
                                onItemSelected = { selectedPriority = TaskPriority.valueOf(it) }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Category", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = TaskCategory.entries.map { it.name },
                                initialSelection = selectedCategory.name,
                                onItemSelected = { selectedCategory = TaskCategory.valueOf(it) }
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
                                category = selectedCategory.name,
                                priority = selectedPriority.name
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
