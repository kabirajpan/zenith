package com.productivityapp.app.ui.tasks

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.app.ui.reminders.RemindersRepository

import com.productivityapp.model.Task
import com.productivityapp.model.TaskPriority
import com.productivityapp.model.TaskStatus
import com.productivityapp.model.TaskCategory

object TasksRepository {
    val tasks = mutableStateListOf<Task>(
        Task(
            id = java.util.UUID.randomUUID().toString(),
            title = "Finish Zenith AI Integration", 
            category = TaskCategory.WORK, 
            priority = TaskPriority.HIGH, 
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        Task(
            id = java.util.UUID.randomUUID().toString(),
            title = "Weekly budget review", 
            category = TaskCategory.FINANCE, 
            priority = TaskPriority.MEDIUM,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )
    
    fun addTask(
        title: String, 
        category: TaskCategory, 
        priority: TaskPriority, 
        description: String = "",
        dueDate: String? = null,
        dueTime: String? = null,
        hasReminder: Boolean = false,
        hasAlarm: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val newTask = Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            hasReminder = hasReminder,
            hasAlarm = hasAlarm,
            createdAt = now,
            updatedAt = now
        )
        tasks.add(0, newTask)
        
        // Intelligent Alarm-Reminder Nexus
        if (hasReminder) {
            RemindersRepository.addReminder(
                title = "Task: $title",
                date = dueDate ?: "Today",
                time = dueTime ?: "Soon",
                category = category.name,
                priority = priority.name,
                taskId = newTask.id
            )
        }
        
        if (hasAlarm) {
            // Future Alarm implementation
        }
    }
    
    fun toggleTask(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val currentTask = tasks[index]
            val newStatus = if (currentTask.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
            val now = System.currentTimeMillis()
            
            tasks[index] = currentTask.copy(
                status = newStatus,
                completedAt = if (newStatus == TaskStatus.DONE) now else null,
                updatedAt = now
            )
            
            // Sync with Heatmap Tracker
            if (newStatus == TaskStatus.DONE) {
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
        category: TaskCategory,
        priority: TaskPriority,
        description: String,
        dueDate: String? = null,
        dueTime: String? = null,
        status: TaskStatus = TaskStatus.PENDING,
        hasReminder: Boolean = false,
        hasAlarm: Boolean = false
    ) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val now = System.currentTimeMillis()
            tasks[index] = tasks[index].copy(
                title = title,
                category = category,
                priority = priority,
                description = description,
                dueDate = dueDate,
                dueTime = dueTime,
                status = status,
                hasReminder = hasReminder,
                hasAlarm = hasAlarm,
                updatedAt = now,
                completedAt = if (status == TaskStatus.DONE) now else null
            )
        }
    }

    fun searchTasks(query: String): List<Task> {
        val lowQuery = query.lowercase().trim()
        if (lowQuery.isBlank() || lowQuery == "task" || lowQuery == "tasks" || lowQuery == "all") return tasks.toList()
        return tasks.filter { 
            it.title.lowercase().contains(lowQuery) || 
            it.category.name.lowercase().contains(lowQuery) ||
            it.priority.name.lowercase().contains(lowQuery)
        }
    }
}
