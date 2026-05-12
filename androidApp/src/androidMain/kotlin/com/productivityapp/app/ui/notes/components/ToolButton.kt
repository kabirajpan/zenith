package com.productivityapp.app.ui.notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToolButton(icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.92f else 1f)
    val alpha by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.5f else 0.7f)

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .focusProperties { canFocus = false }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = Color.White.copy(alpha = alpha), 
            modifier = Modifier.size(22.dp)
        )
    }
}
