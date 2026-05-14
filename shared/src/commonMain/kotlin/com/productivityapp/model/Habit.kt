package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class HabitFrequency {
    DAILY, WEEKLY
}

@Serializable
data class Habit(
    val id: String = "",
    val title: String,
    val icon: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: List<Int> = emptyList(), // 1-7 for Mon-Sun
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val completedDates: List<Long> = emptyList(), // Timestamps of completion
    val reminderId: String? = null, // Linked reminder
    val isArchived: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
