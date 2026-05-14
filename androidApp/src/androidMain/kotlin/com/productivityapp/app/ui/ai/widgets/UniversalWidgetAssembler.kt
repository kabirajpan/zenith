package com.productivityapp.app.ui.ai.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("General") }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var repeatDays by remember { mutableStateOf(setOf<String>()) }

    UniversalActionCard {
        // 1. Header is always present
        val headerIcon = when (action.module) {
            "Tasks" -> Icons.Default.Task
            "Reminders" -> Icons.Default.Notifications
            "Alarms" -> Icons.Default.Alarm
            "Notes" -> Icons.Default.Description
            "Habits" -> Icons.Default.Repeat
            "Vault" -> Icons.Default.Lock
            "Calendar" -> Icons.Default.Event
            else -> Icons.Default.SmartButton
        }
        HeaderSection(
            icon = headerIcon,
            title = if ((action.module ?: "Unknown") != "Unknown") action.module ?: "Unknown" else "Intelligence",
            action = action.type.name.replace("_", " ")
        )
        
        SectionDivider()

        // 2. Input Section (Title/Description)
        if (action.type == ActionType.CREATE || action.type == ActionType.CREATE_REMINDER || action.type == ActionType.UPDATE) {
            InputSection(
                label = "NAME / TITLE",
                value = editedTitle,
                onValueChange = { editedTitle = it },
                placeholder = "What's the goal?"
            )
        }

        // 3. Domain-Specific Sections
        when (action.module) {
            "Tasks" -> {
                if (action.type == ActionType.CREATE) {
                    PrioritySection(selected = selectedPriority, options = listOf("Low", "Medium", "High", "Urgent"), onSelect = { selectedPriority = it })
                    CategorySection(selected = selectedCategory, options = listOf("Work", "Personal", "Health"), onSelect = { selectedCategory = it })
                } else if (action.type == ActionType.UPDATE) {
                    StatusSection(status = "TODO", onStatusChange = {})
                }
            }
            "Alarms" -> {
                DateTimeSection(date = "Today", time = "07:30 AM")
                RepeatSection(selectedDays = repeatDays, onToggle = { day ->
                    repeatDays = if (repeatDays.contains(day)) repeatDays - day else repeatDays + day
                })
            }
            "Reminders" -> {
                DateTimeSection(date = "Tomorrow", time = "09:00 AM")
                ToggleSection(label = "Repeating", checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it })
            }
            "Habits" -> {
                CategorySection(selected = selectedCategory, options = listOf("Health", "Skill", "Mind"), onSelect = { selectedCategory = it })
                RepeatSection(selectedDays = repeatDays, onToggle = { day ->
                    repeatDays = if (repeatDays.contains(day)) repeatDays - day else repeatDays + day
                })
            }
        }

        SectionDivider()

        // 10. Action Section is always last
        ActionSection(
            isProcessed = isProcessed,
            onConfirm = { 
                // In a real app, we'd pass the ephemeral state back
                onConfirm() 
            },
            onDismiss = onDismiss
        )
    }
}
