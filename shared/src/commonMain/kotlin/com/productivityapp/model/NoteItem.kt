package com.productivityapp.model

import androidx.compose.ui.graphics.Color

data class NoteItem(
    val id: String,
    val title: String,
    val summary: String, // Legacy support - kept in order
    val blocks: List<NoteBlock>, // Legacy support - kept in order
    val color: Color = Color(0xFF818CF8), // Legacy support - kept in order
    val content: String = "", // New field
    val category: String = "General",
    val isPinned: Boolean = false,
    val isLocked: Boolean = false, // Secure vault integration
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
