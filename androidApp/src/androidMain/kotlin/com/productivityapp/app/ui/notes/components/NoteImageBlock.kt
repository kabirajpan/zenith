package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun NoteImageBlock(uri: String, onDelete: () -> Unit) {
    var isFullScreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    if (uri.isEmpty()) {
        Box(
            modifier = Modifier.size(120.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
    } else {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            AsyncImage(
                model = uri,
                contentDescription = "Note image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { isFullScreen = true },
                contentScale = ContentScale.Crop
            )
            
            // Overflow menu for Image
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
                if (showMenu) {
                    androidx.compose.ui.window.Popup(
                        onDismissRequest = { showMenu = false },
                        alignment = Alignment.TopEnd,
                        offset = androidx.compose.ui.unit.IntOffset(-12, 50)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 4.dp,
                            modifier = Modifier.width(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable { 
                                        showMenu = false
                                        onDelete() 
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Delete Image", 
                                    color = Color.Red.copy(alpha = 0.9f), 
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (isFullScreen) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { isFullScreen = false }) {
                Box(modifier = Modifier.fillMaxSize().clickable { isFullScreen = false }, contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Full image",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
