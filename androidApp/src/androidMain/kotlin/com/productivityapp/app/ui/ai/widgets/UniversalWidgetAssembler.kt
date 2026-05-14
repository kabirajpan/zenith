package com.productivityapp.app.ui.ai.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.productivityapp.app.ui.ai.ActionType
import com.productivityapp.app.ui.ai.ProposedAction

@Composable
fun UniversalWidgetAssembler(
    action: ProposedAction,
    isProcessed: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Local ephemeral state for the proposal
    var editedTitle by remember { mutableStateOf(action.title ?: "") }
    var editedTime by remember { mutableStateOf(action.time ?: "07:00 AM") }
    var selectedPriority by remember { mutableStateOf(action.priority ?: "Medium") }
    var selectedCategory by remember { mutableStateOf(action.category ?: "General") }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var repeatDays by remember { mutableStateOf(action.repeatDays?.toSet() ?: setOf<String>()) }

    val accentColor = when (action.module) {
        "Vault" -> Color(0xFF22C55E)
        "Tasks" -> if (action.type == ActionType.TOGGLE) Color(0xFF22C55E) else Color(0xFF818CF8)
        "Reminders" -> Color(0xFFFBBF24)
        "Alarms" -> Color(0xFFFBBF24)
        else -> Color(0xFF818CF8)
    }

    UniversalActionCard(accentColor = accentColor) {
        // 1. Header Logic
        val headerIcon = when (action.module) {
            "Tasks" -> if (action.type == ActionType.TOGGLE) Icons.Default.CheckCircle else Icons.Default.Task
            "Reminders" -> Icons.Default.Notifications
            "Alarms" -> Icons.Default.Alarm
            "Vault" -> Icons.Default.Lock
            else -> Icons.Default.SmartButton
        }
        
        HeaderSection(
            icon = headerIcon,
            title = (action.module ?: "Zenith Intelligence").uppercase(),
            action = if (action.type == ActionType.TOGGLE) "Status Change" else action.type.name.replace("_", " ")
        )
        
        SectionDivider()

        // 2. Specialized Toggle View
        if (action.type == ActionType.TOGGLE) {
            ToggleStateSection(
                label = action.title ?: "Update Status",
                targetState = true // Defaulting to 'Done' for toggle-on requests
            )
        }

        // 3. Domain-Specific Sections (only if not a simple toggle)
        if (action.type != ActionType.TOGGLE) {
            when (action.module) {
                "Alarms" -> {
                    // Optimized Alarm Layout
                    DateTimeSection(
                        date = if (repeatDays.isEmpty()) "Today" else "Recurring", 
                        time = editedTime
                    )
                    RepeatSection(selectedDays = repeatDays, onToggle = { day ->
                        repeatDays = if (repeatDays.contains(day)) repeatDays - day else repeatDays + day
                    })
                    InputSection(
                        label = "ALARM LABEL (OPTIONAL)",
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        placeholder = "e.g. Wake Up"
                    )
                }
                "Tasks" -> {
                    if (action.type == ActionType.CREATE) {
                        InputSection(
                            label = "NAME / TITLE",
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            placeholder = "What's the goal?"
                        )
                        PrioritySection(selected = selectedPriority, options = listOf("Low", "Medium", "High", "Urgent"), onSelect = { selectedPriority = it })
                        CategorySection(selected = selectedCategory, options = listOf("Work", "Personal", "Health"), onSelect = { selectedCategory = it })
                    }
                }
                "Reminders" -> {
                    InputSection(
                        label = "REMINDER TITLE",
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        placeholder = "Don't forget..."
                    )
                    DateTimeSection(date = "Tomorrow", time = "09:00 AM")
                    ToggleSection(label = "Repeating", checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it })
                }
            }
        }

        SectionDivider()

        // 5. High-Fidelity Action Bar
        ActionSection(
            isProcessed = isProcessed,
            onConfirm = {
                // Return the edited state
                onConfirm() 
            },
            onDismiss = onDismiss
        )
    }
}
