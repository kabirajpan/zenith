package com.productivityapp.app.ui.tasks

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.app.ui.reminders.RemindersRepository

data class TaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val time: String,
    val priority: String,
    val category: String,
    val isCompleted: Boolean = false
)

object TasksRepository {
    val tasks = mutableStateListOf<TaskItem>()
    
    fun addTask(title: String, category: String, priority: String) {
        tasks.add(0, TaskItem(
            title = title,
            time = "Today",
            priority = priority,
            category = category
        ))
        
        // Intelligent Alarm-Reminder Nexus
        RemindersRepository.addReminder(
            title = "Task: $title",
            date = "Today",
            time = "Soon",
            category = category,
            priority = priority
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
}
