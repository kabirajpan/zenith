package com.productivityapp.model

import kotlinx.serialization.Serializable

@Serializable
enum class SuggestionType {
    TASK, HABIT, SCREENTIME, GENERAL
}

@Serializable
data class AiSuggestion(
    val id: String = "",
    val type: SuggestionType = SuggestionType.GENERAL,
    val message: String,
    val relatedId: String? = null, // task/habit/event id
    val isRead: Boolean = false,
    val createdAt: Long = 0
)
