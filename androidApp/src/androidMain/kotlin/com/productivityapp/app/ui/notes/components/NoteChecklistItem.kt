package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.productivityapp.model.NoteBlock

@Composable
fun NoteChecklistItem(
    block: NoteBlock.Checklist,
    isFocused: Boolean,
    onFocus: () -> Unit,
    onContentChange: (String) -> Unit, 
    onCheckedChange: (Boolean) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    val isChecked = block.isChecked
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Custom Square Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isChecked) Color(0xFF818CF8) else Color.Transparent)
                .border(2.dp, if (isChecked) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .clickable { onCheckedChange(!isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
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
                textAlign = when (block.textAlign) {
                    1 -> TextAlign.Center
                    2 -> TextAlign.Right
                    else -> TextAlign.Left
                },
                textDecoration = if (isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onKeyEvent { 
                    if (it.key == Key.Backspace && block.content.isEmpty()) { onBackspace(); true } else false
                }
        )
    }
}
