package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class DayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

@Serializable
data class Alarm(
    val id: String = "", // Will be assigned on creation
    val label: String = "",
    val time: String, // e.g., "07:00 AM"
    val repeatDays: List<DayOfWeek> = emptyList(),
    val isEnabled: Boolean = true,
    val isVibrate: Boolean = true,
    val sound: String = "Default",
    val escalationType: String = "Standard", // Gentle, Standard, Urgent
    val isHighPriority: Boolean = false,
    val taskId: String? = null, // Linked task (optional)
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    companion object {
        fun stringToDayOfWeek(day: String): DayOfWeek? {
            return when (day) {
                "Mo", "MON" -> DayOfWeek.MON
                "Tu", "TUE" -> DayOfWeek.TUE
                "We", "WED" -> DayOfWeek.WED
                "Th", "THU" -> DayOfWeek.THU
                "Fr", "FRI" -> DayOfWeek.FRI
                "Sa", "SAT" -> DayOfWeek.SAT
                "Su", "SUN" -> DayOfWeek.SUN
                else -> null
            }
        }

        fun dayOfWeekToString(day: DayOfWeek): String {
            return when (day) {
                DayOfWeek.MON -> "Mo"
                DayOfWeek.TUE -> "Tu"
                DayOfWeek.WED -> "We"
                DayOfWeek.THU -> "Th"
                DayOfWeek.FRI -> "Fr"
                DayOfWeek.SAT -> "Sa"
                DayOfWeek.SUN -> "Su"
            }
        }
    }

    fun getDaysAsStringList(): List<String> = repeatDays.map { dayOfWeekToString(it) }
}
