package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RecordingWaveform(amplitudes: List<Float>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = 3.dp.toPx()
        val gap = 2.dp.toPx()
        val maxBars = (width / (barWidth + gap)).toInt()
        
        val recentAmps = amplitudes.takeLast(maxBars)
        
        recentAmps.forEachIndexed { index, amp ->
            val barHeight = (amp * height).coerceAtLeast(4.dp.toPx())
            val x = width - (recentAmps.size - index) * (barWidth + gap)
            drawRoundRect(
                color = Color.Red.copy(alpha = 0.6f + (amp * 0.4f)),
                topLeft = androidx.compose.ui.geometry.Offset(x, (height - barHeight) / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
            )
        }
    }
}
