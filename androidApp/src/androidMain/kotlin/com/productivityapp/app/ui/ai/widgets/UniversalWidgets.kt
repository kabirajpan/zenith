package com.productivityapp.app.ui.ai.widgets

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productivityapp.app.ui.ai.ProposedAction
import com.productivityapp.app.ui.vault.VaultItem
import com.productivityapp.app.ui.vault.VaultType

@Composable
fun UniversalActionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.widthIn(max = 320.dp),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SectionDivider() {
    Divider(
        color = Color.White.copy(alpha = 0.05f),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// 1. HeaderWidget
@Composable
fun HeaderSection(icon: ImageVector, title: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF818CF8).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(action.uppercase(), color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

// 2. TextInputWidget
@Composable
fun InputSection(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(label, color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color.Gray.copy(alpha = 0.3f), fontSize = 13.sp)
            }
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF818CF8)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 3. DateTimeWidget
@Composable
fun DateTimeSection(date: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text("DATE", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(date, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text("TIME", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(time, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 4. PriorityWidget
@Composable
fun PrioritySection(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("PRIORITY", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                val isSelected = selected == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFFFBBF24).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(opt, color = if (isSelected) Color(0xFFFBBF24) else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. CategoryWidget
@Composable
fun CategorySection(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("CATEGORY", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                val isSelected = selected == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF818CF8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(opt, color = if (isSelected) Color(0xFF818CF8) else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. RepeatWidget
@Composable
fun RepeatSection(selectedDays: Set<String>, onToggle: (String) -> Unit) {
    val allDays = listOf("M", "T", "W", "T", "F", "S", "S")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("REPEAT ON", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            allDays.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.03f))
                        .clickable { onToggle(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 7. ToggleWidget
@Composable
fun ToggleSection(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF818CF8),
                checkedTrackColor = Color(0xFF818CF8).copy(alpha = 0.5f)
            )
        )
    }
}

// 8. StatusWidget
@Composable
fun StatusSection(status: String, onStatusChange: (String) -> Unit) {
    val statuses = listOf("TODO", "PROGRESS", "DONE")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("STATUS", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            statuses.forEach { s ->
                val isSelected = status == s
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF22C55E).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                        .clickable { onStatusChange(s) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s, color = if (isSelected) Color(0xFF22C55E) else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 9. RevealWidget (Vault Specific)
@Composable
fun RevealSection(item: VaultItem, isRevealed: Boolean, onReveal: () -> Unit) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!isRevealed) {
            Button(
                onClick = onReveal,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF22C55E).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                elevation = null
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tap to Reveal Secret", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { 
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(item.encryptedData))
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.encryptedData, 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// 10. ConfirmWidget
@Composable
fun ActionSection(isProcessed: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onDismiss,
            enabled = !isProcessed,
            modifier = Modifier.weight(0.4f),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            elevation = null
        ) {
            Text("Dismiss", color = Color.Gray, fontSize = 12.sp)
        }
        Button(
            onClick = onConfirm,
            enabled = !isProcessed,
            modifier = Modifier.weight(0.6f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF818CF8),
                disabledBackgroundColor = Color(0xFF818CF8).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (isProcessed) "Processed" else "Confirm", 
                color = if (isProcessed) Color.White.copy(alpha = 0.5f) else Color.White, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}
