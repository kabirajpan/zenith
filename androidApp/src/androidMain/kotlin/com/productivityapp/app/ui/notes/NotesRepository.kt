package com.productivityapp.app.ui.notes

import androidx.compose.runtime.mutableStateListOf
import com.productivityapp.model.NoteItem
import com.productivityapp.model.NoteBlock

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
            notes.add(0, NoteItem(id = java.util.UUID.randomUUID().toString(), title = title, summary = summary, blocks = blocks.toList()))
        }
    }
}
