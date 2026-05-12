package com.productivityapp.app.ui.notes.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteAudioBlock(
    duration: String, 
    isRecording: Boolean, 
    progress: Float, 
    amplitudes: List<Float>,
    onPlay: () -> Unit, 
    onStop: () -> Unit, 
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (progress == 0f) snap() else tween(durationMillis = 32, easing = LinearEasing)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .border(1.dp, if (isRecording) Color.Red.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                    .clickable { onStop() },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(14.dp).background(Color.Red, RoundedCornerShape(2.dp)))
            }
        } else {
            Surface(
                color = Color(0xFF818CF8),
                shape = CircleShape,
                modifier = Modifier.size(40.dp).clickable { onPlay() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (progress > 0f && progress < 0.99f) Icons.Default.Pause else Icons.Default.PlayArrow, 
                        contentDescription = null, 
                        tint = Color.White, 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            if (isRecording) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RecordingWaveform(amplitudes, modifier = Modifier.weight(1f).height(30.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(duration, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(Color(0xFF818CF8), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Voice Memo", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(duration, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        
        if (!isRecording) {
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
                if (showMenu) {
                    androidx.compose.ui.window.Popup(
                        onDismissRequest = { showMenu = false },
                        alignment = Alignment.TopEnd,
                        offset = androidx.compose.ui.unit.IntOffset(-12, 60)
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
                                    "Delete Audio", 
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
    }
}
