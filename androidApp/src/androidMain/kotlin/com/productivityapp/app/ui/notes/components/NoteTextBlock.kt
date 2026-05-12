package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.productivityapp.model.NoteBlock

@Composable
fun NoteTextBlock(
    block: NoteBlock.Text,
    isFocused: Boolean,
    showPlaceholder: Boolean,
    onFocus: () -> Unit,
    onContentChange: (String) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    BasicTextField(
        value = block.content,
        onValueChange = {
            if (it.endsWith("\n")) onEnter()
            else onContentChange(it.replace("\n", ""))
        },
        textStyle = TextStyle(
            color = Color(block.color),
            fontSize = block.fontSize.sp,
            fontWeight = FontWeight(block.fontWeight),
            textAlign = when(block.textAlign) {
                1 -> TextAlign.Center
                2 -> TextAlign.Right
                else -> TextAlign.Left
            },
            lineHeight = (block.fontSize + 8).sp
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onKeyEvent { 
                if (it.key == Key.Backspace && block.content.isEmpty()) { onBackspace(); true } else false
            },
        decorationBox = { innerTextField ->
            if (showPlaceholder && block.content.isEmpty()) Text("Start typing...", color = Color.Gray, fontSize = block.fontSize.sp)
            innerTextField()
        }
    )
}
