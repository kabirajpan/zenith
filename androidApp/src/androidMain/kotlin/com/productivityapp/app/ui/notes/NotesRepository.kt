package com.productivityapp.app.ui.notes

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color

data class NoteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val blocks: List<NoteBlock>,
    val color: Color = Color(0xFF818CF8)
)

object NotesRepository {
    val notes = mutableStateListOf<NoteItem>()
    
    fun saveOrUpdateNote(id: String?, title: String, summary: String, blocks: List<NoteBlock>) {
        if (title.isBlank() && blocks.all { it is NoteBlock.Text && it.content.isBlank() }) return
        
        val existingIndex = notes.indexOfFirst { it.id == id }
        if (existingIndex != -1) {
            notes[existingIndex] = notes[existingIndex].copy(
                title = title,
                summary = summary,
                blocks = blocks.toList()
            )
        } else {
            notes.add(0, NoteItem(title = title, summary = summary, blocks = blocks.toList()))
        }
    }
}
