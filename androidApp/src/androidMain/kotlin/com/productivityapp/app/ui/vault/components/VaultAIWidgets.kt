package com.productivityapp.app.ui.vault.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productivityapp.app.ui.ai.ChatMessage
import com.productivityapp.app.ui.vault.VaultItem

@Composable
fun VaultSecureRevealWidget(
    message: ChatMessage,
    onProcessed: () -> Unit
) {
    val secureItem = message.secureItem ?: return
    var isRevealed by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.2f)),
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF22C55E).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isRevealed) Icons.Default.LockOpen else Icons.Default.Lock, 
                        contentDescription = null, 
                        tint = Color(0xFF22C55E), 
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(secureItem.site, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(secureItem.username, color = Color.Gray, fontSize = 11.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isRevealed) {
                Button(
                    onClick = { isRevealed = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF22C55E).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    elevation = null,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Tap to Reveal", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .clickable { 
                            clipboardManager.setText(AnnotatedString(secureItem.password))
                            onProcessed()
                        }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = secureItem.password,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Icon(
                            Icons.Default.ContentCopy, 
                            contentDescription = "Copy", 
                            tint = Color(0xFF22C55E).copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
