package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteLinkBlock(
    url: String, 
    isFocused: Boolean,
    onFocus: () -> Unit,
    onUrlChange: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    
    var isActuallyFocused by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActuallyFocused) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = url,
            onValueChange = { 
                if (it.endsWith("\n")) { /* ignore or close */ }
                else onUrlChange(it.replace("\n", ""))
            },
            textStyle = TextStyle(
                color = if (isActuallyFocused) Color.White else Color(0xFF818CF8), 
                fontSize = 16.sp, 
                textDecoration = if (!isActuallyFocused && url.isNotEmpty()) TextDecoration.Underline else null
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { 
                    isActuallyFocused = it.isFocused
                    if (it.isFocused) onFocus()
                }
                .onKeyEvent {
                    if (it.key == androidx.compose.ui.input.key.Key.Backspace && url.isEmpty()) {
                        onBackspace()
                        true
                    } else false
                }
                .pointerInput(url) {
                    detectTapGestures(
                        onTap = { 
                            if (!isActuallyFocused && url.isNotEmpty()) {
                                try { uriHandler.openUri(if (!url.startsWith("http")) "https://$url" else url) } catch (e: Exception) {}
                            } else {
                                focusRequester.requestFocus()
                            }
                        }
                    )
                },
            decorationBox = { innerTextField ->
                if (url.isEmpty()) Text("Paste or type link...", color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp)
                innerTextField()
            }
        )
    }
}
