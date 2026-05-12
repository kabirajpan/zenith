package com.productivityapp.model

sealed class NoteBlock {
    data class Text(
        val content: String,
        val fontWeight: Int = 400, // 300 Light, 400 Regular, 700 Bold
        val fontSize: Int = 17,
        val textAlign: Int = 0, // 0 Left, 1 Center, 2 Right
        val color: Long = 0xFFFFFFFF
    ) : NoteBlock()
    
    data class Checklist(
        val content: String, 
        val isChecked: Boolean,
        val fontWeight: Int = 400,
        val fontSize: Int = 17,
        val textAlign: Int = 0,
        val color: Long = 0xFFFFFFFF
    ) : NoteBlock()
    
    data class Table(val data: List<List<String>>) : NoteBlock()
    data class Link(val url: String) : NoteBlock()
    data class Image(val uri: String) : NoteBlock()
    data class Audio(val duration: String, val isRecording: Boolean = false, val filePath: String? = null) : NoteBlock()
}
