package com.productivityapp.app.ui.common

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalWheelPicker(
    options: List<String>,
    initialSelection: String,
    onItemSelected: (String) -> Unit
) {
    val itemHeight = 32.dp
    val visibleItems = 3
    val initialIndex = options.indexOf(initialSelection).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    // Track selection
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            if (centerIndex in options.indices) {
                onItemSelected(options[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(itemHeight * visibleItems)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Selection Highlight (Glassy lines)
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(itemHeight))
            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(options.size) { index ->
                val option = options[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            val itemOffset = listState.layoutInfo.visibleItemsInfo
                                .find { it.index == index }
                                ?.let { it.offset + it.size / 2 } ?: 0
                            val viewportCenter = listState.layoutInfo.viewportEndOffset / 2
                            val distanceFromCenter = kotlin.math.abs(itemOffset - viewportCenter).toFloat()
                            val normalizedDistance = (distanceFromCenter / (itemHeight.toPx() * 1.5f)).coerceIn(0f, 1f)
                            
                            alpha = 1f - (normalizedDistance * 0.6f)
                            scaleX = 1f - (normalizedDistance * 0.2f)
                            scaleY = 1f - (normalizedDistance * 0.2f)
                            rotationX = normalizedDistance * 45f * (if (itemOffset < viewportCenter) 1f else -1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionSection(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF818CF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .clickable { onSelect(option) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(option, color = if (isSelected) Color(0xFF818CF8) else Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
