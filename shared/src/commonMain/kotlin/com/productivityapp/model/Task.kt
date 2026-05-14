package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

@Serializable
enum class TaskStatus {
    PENDING, IN_PROGRESS, DONE
}

@Serializable
enum class TaskCategory {
    WORK, PERSONAL, HEALTH, STUDY, FINANCE, SOCIAL, TRAVEL, OTHER
}

@Serializable
data class Task(
    val id: String = "", // Will be assigned on creation
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val category: TaskCategory = TaskCategory.WORK,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val hasReminder: Boolean = false,
    val hasAlarm: Boolean = false,
    val reminderId: String? = null,
    val alarmId: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val completedAt: Long? = null
)
