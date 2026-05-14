package com.productivityapp.app.ui.reminders

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.model.Reminder

object RemindersRepository {
    private val _reminders = mutableStateListOf<Reminder>(
        Reminder(title = "Morning Coffee & Standup", date = "Today", time = "09:00 AM", category = "Work", priority = "Medium"),
        Reminder(title = "Check Zen Note sync", date = "Today", time = "02:00 PM", category = "Work", priority = "High"),
        Reminder(title = "Grocery shopping", date = "May 15", time = "06:00 PM", category = "Personal", priority = "Low")
    )
    val reminders: List<Reminder> get() = _reminders

    fun addReminder(
        title: String, 
        date: String, 
        time: String, 
        category: String, 
        priority: String, 
        taskId: String? = null,
        repeatInterval: com.productivityapp.model.RepeatInterval = com.productivityapp.model.RepeatInterval.NONE,
        description: String = ""
    ) {
        val now = java.time.Instant.now().toEpochMilli()
        _reminders.add(0, Reminder(
            id = java.util.UUID.randomUUID().toString(),
            title = title, 
            date = date, 
            time = time, 
            category = category, 
            priority = priority, 
            taskId = taskId,
            repeatInterval = repeatInterval,
            description = description,
            createdAt = now,
            updatedAt = now
        ))
    }

    fun toggleReminder(id: String) {
        val index = _reminders.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _reminders[index]
            _reminders[index] = item.copy(
                isCompleted = !item.isCompleted,
                updatedAt = java.time.Instant.now().toEpochMilli()
            )
        }
    }

    fun updateReminder(
        id: String,
        title: String,
        date: String,
        time: String,
        category: String,
        priority: String,
        repeatInterval: com.productivityapp.model.RepeatInterval? = null,
        description: String? = null
    ) {
        val index = _reminders.indexOfFirst { it.id == id }
        if (index != -1) {
            _reminders[index] = _reminders[index].copy(
                title = title,
                date = date,
                time = time,
                category = category,
                priority = priority,
                repeatInterval = repeatInterval ?: _reminders[index].repeatInterval,
                description = description ?: _reminders[index].description,
                updatedAt = java.time.Instant.now().toEpochMilli()
            )
        }
    }

    fun deleteReminder(id: String) {
        _reminders.removeAll { it.id == id }
    }

    fun searchReminders(query: String): List<Reminder> {
        if (query.isBlank()) return reminders.toList()
        val lowQuery = query.lowercase()
        return reminders.filter { 
            it.title.lowercase().contains(lowQuery) || 
            it.category.lowercase().contains(lowQuery) 
        }
    }
}
