package com.productivityapp.app.ui.tasks

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.app.ui.reminders.RemindersRepository

data class TaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val time: String,
    val priority: String,
    val category: String,
    val isCompleted: Boolean = false,
    val estimatedMins: Int = 30,
    val energyLevel: String = "Medium", // Low, Medium, High
    val completedAt: Long? = null
)

object TasksRepository {
    val tasks = mutableStateListOf<TaskItem>(
        TaskItem(title = "Finish Zenith AI Integration", category = "Work", priority = "High", energyLevel = "High", estimatedMins = 120, time = "Today"),
        TaskItem(title = "Weekly budget review", category = "Finance", priority = "Medium", energyLevel = "Low", estimatedMins = 30, time = "Today"),
        TaskItem(title = "30-min Cardio", category = "Health", priority = "High", energyLevel = "High", estimatedMins = 30, time = "Today"),
        TaskItem(title = "Call Mom", category = "Social", priority = "Medium", energyLevel = "Low", estimatedMins = 15, time = "Today"),
        TaskItem(title = "Plan weekend trip", category = "Travel", priority = "Low", energyLevel = "Medium", estimatedMins = 45, time = "Today")
    )
    
    fun addTask(
        title: String, 
        category: String, 
        priority: String, 
        description: String = "", 
        estimatedMins: Int = 30, 
        energyLevel: String = "Medium"
    ) {
        val newTask = TaskItem(
            title = title,
            description = description,
            time = "Today",
            priority = priority,
            category = category,
            estimatedMins = estimatedMins,
            energyLevel = energyLevel
        )
        tasks.add(0, newTask)
        
        // Intelligent Alarm-Reminder Nexus
        RemindersRepository.addReminder(
            title = "Task: $title",
            date = "Today",
            time = "Soon",
            category = category,
            priority = priority,
            nexusId = newTask.id
        )
    }
    
    fun toggleTask(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val isNowCompleted = !tasks[index].isCompleted
            tasks[index] = tasks[index].copy(isCompleted = isNowCompleted)
            
            // Sync with Heatmap Tracker
            if (isNowCompleted) {
                com.productivityapp.app.ui.dashboard.ProductivityTracker.recordCompletion()
            } else {
                com.productivityapp.app.ui.dashboard.ProductivityTracker.removeCompletion()
            }
        }
    }
    
    fun deleteTask(id: String) {
        tasks.removeAll { it.id == id }
    }

    fun updateTask(
        id: String,
        title: String,
        category: String,
        priority: String,
        description: String,
        estimatedMins: Int,
        energyLevel: String
    ) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            tasks[index] = tasks[index].copy(
                title = title,
                category = category,
                priority = priority,
                description = description,
                estimatedMins = estimatedMins,
                energyLevel = energyLevel
            )
        }
    }
}
