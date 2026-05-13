package com.productivityapp.app.ui.reminders

import androidx.compose.runtime.mutableStateListOf

data class ReminderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String,
    val time: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium",
    val category: String = "Personal"
)

object RemindersRepository {
    private val _reminders = mutableStateListOf<ReminderItem>(
        ReminderItem(title = "Morning Standup", date = "Today", time = "09:00 AM", category = "Work", priority = "High"),
        ReminderItem(title = "Gym Session", date = "Today", time = "06:00 PM", category = "Health", priority = "Medium"),
        ReminderItem(title = "Buy Groceries", date = "Tomorrow", time = "10:00 AM", category = "Personal", priority = "Low"),
        ReminderItem(title = "Project Deadline", date = "May 20", time = "11:59 PM", category = "Work", priority = "High")
    )
    val reminders: List<ReminderItem> get() = _reminders

    fun addReminder(title: String, date: String, time: String, category: String, priority: String) {
        _reminders.add(ReminderItem(title = title, date = date, time = time, category = category, priority = priority))
    }

    fun toggleReminder(id: String) {
        val index = _reminders.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _reminders[index]
            _reminders[index] = item.copy(isCompleted = !item.isCompleted)
        }
    }

    fun deleteReminder(id: String) {
        _reminders.removeIf { it.id == id }
    }
}
