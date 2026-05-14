package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class RepeatInterval {
    NONE, DAILY, WEEKLY, MONTHLY
}

@Serializable
data class Reminder(
    val id: String = "", // Will be assigned on creation
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: String = "Medium",
    val taskId: String? = null, // Linked task (optional)
    val eventId: String? = null, // Linked calendar event (optional)
    val dateTime: Long = 0, // Timestamp for the reminder
    val date: String = "", // e.g., "Today" or "May 15"
    val time: String = "", // e.g., "09:00 AM"
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val isEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
