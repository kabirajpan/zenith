package com.productivityapp.app.ui.reminders

import androidx.compose.runtime.mutableStateListOf

data class ReminderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nexusId: String? = null, // Linked Task/Habit ID
    val title: String,
    val description: String = "",
    val date: String,
    val time: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium",
    val category: String = "Personal",
    val snoozeCount: Int = 0,
    val urgencyLevel: Int = 1 // 1=Gentle, 2=Standard, 3=Urgent
)

object RemindersRepository {
    private val _reminders = mutableStateListOf<ReminderItem>()
    val reminders: List<ReminderItem> get() = _reminders

    fun addReminder(title: String, date: String, time: String, category: String, priority: String, nexusId: String? = null) {
        _reminders.add(ReminderItem(title = title, date = date, time = time, category = category, priority = priority, nexusId = nexusId))
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
