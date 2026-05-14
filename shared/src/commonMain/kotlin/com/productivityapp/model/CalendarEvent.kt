package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = "",
    val title: String,
    val description: String = "",
    val startDateTime: Long = 0,
    val endDateTime: Long = 0,
    val location: String = "",
    val isAllDay: Boolean = false,
    val isGoogleSynced: Boolean = false,
    val googleEventId: String? = null,
    val reminderId: String? = null, // Linked reminder
    val color: String = "#818CF8", // Default indigo
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
