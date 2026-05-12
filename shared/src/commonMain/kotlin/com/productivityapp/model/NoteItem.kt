package com.productivityapp.model

import androidx.compose.ui.graphics.Color

data class NoteItem(
    val id: String,
    val title: String,
    val summary: String,
    val blocks: List<NoteBlock>,
    val color: Color = Color(0xFF818CF8)
)
