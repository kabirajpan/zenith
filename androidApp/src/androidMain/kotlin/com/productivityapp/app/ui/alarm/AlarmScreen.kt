package com.productivityapp.app.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlarmScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Alarms",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyAlarms) { alarm ->
                AlarmCard(alarm)
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: AlarmItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = alarm.time, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = alarm.days, color = Color.Gray, fontSize = 12.sp)
        }
        
        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = { /* Toggle */ },
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF818CF8))
        )
    }
}

data class AlarmItem(
    val time: String,
    val days: String,
    val isEnabled: Boolean
)

val dummyAlarms = listOf(
    AlarmItem("07:00 AM", "Mon, Tue, Wed, Thu, Fri", true),
    AlarmItem("08:30 AM", "Sat, Sun", false),
    AlarmItem("10:00 PM", "Daily", true)
)
