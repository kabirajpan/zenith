package com.productivityapp.app.ui.tasks

import androidx.compose.runtime.mutableStateListOf

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
    }
    
    fun toggleTask(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isCompleted = !tasks[index].isCompleted)
        }
    }
    
    fun deleteTask(id: String) {
        tasks.removeAll { it.id == id }
    }
}
