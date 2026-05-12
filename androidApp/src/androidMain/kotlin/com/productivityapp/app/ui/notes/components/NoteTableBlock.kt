package com.productivityapp.app.ui.notes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteTableBlock(
    data: List<List<String>>, 
    onDataChange: (List<List<String>>) -> Unit,
    onBackspace: () -> Unit
) {
    var expandedRowIndex by remember { mutableStateOf(-1) }
    var expandedColIndex by remember { mutableStateOf(-1) }
    var isTableFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isTableFocused = it.hasFocus }
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedVisibility(visible = isTableFocused) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (data.isNotEmpty()) {
                    data[0].forEachIndexed { colIndex, _ ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { expandedColIndex = colIndex }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                            }
                            
                            if (expandedColIndex == colIndex) {
                                androidx.compose.ui.window.Popup(
                                    onDismissRequest = { expandedColIndex = -1 },
                                    alignment = Alignment.TopCenter
                                ) {
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = 8.dp,
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        ColMenuContent(
                                            colIndex = colIndex,
                                            data = data,
                                            onDataChange = onDataChange,
                                            onDismiss = { expandedColIndex = -1 }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
            }
        }

        data.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                row.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = cell,
                            onValueChange = { newVal ->
                                val newData = data.mapIndexed { r, rList ->
                                    if (r == rowIndex) rList.mapIndexed { c, cVal -> if (c == colIndex) newVal else cVal }
                                    else rList
                                }
                                onDataChange(newData)
                            },
                            textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onKeyEvent {
                                    if (rowIndex == 0 && colIndex == 0 && it.key == Key.Backspace && cell.isEmpty()) {
                                        onBackspace()
                                        true
                                    } else false
                                }
                        )
                    }
                }
                
                if (isTableFocused) {
                    Box {
                        IconButton(onClick = { expandedRowIndex = rowIndex }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        if (expandedRowIndex == rowIndex) {
                            androidx.compose.ui.window.Popup(
                                onDismissRequest = { expandedRowIndex = -1 },
                                alignment = Alignment.TopEnd
                            ) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = 8.dp,
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    RowMenuContent(
                                        rowIndex = rowIndex,
                                        data = data,
                                        onDataChange = onDataChange,
                                        onDismiss = { expandedRowIndex = -1 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        AnimatedVisibility(visible = isTableFocused) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "+ Add Row", 
                    color = Color(0xFF818CF8).copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val newRow = List(if (data.isEmpty()) 2 else data[0].size) { "" }
                        onDataChange(data + listOf(newRow))
                    }
                )
                Text(
                    "+ Add Column", 
                    color = Color(0xFF818CF8).copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onDataChange(data.map { it + "" })
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ColMenuContent(colIndex: Int, data: List<List<String>>, onDataChange: (List<List<String>>) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(4.dp).verticalScroll(rememberScrollState())) {
        Text("Add Col Right", color = Color.White, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
            onDataChange(data.map { row -> row.toMutableList().apply { add(colIndex + 1, "") } })
            onDismiss()
        }.padding(8.dp))
        Text("Delete Column", color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
            if (data.isNotEmpty() && data[0].size > 1) {
                onDataChange(data.map { row -> row.toMutableList().apply { removeAt(colIndex) } })
            }
            onDismiss()
        }.padding(8.dp))
    }
}

@Composable
fun RowMenuContent(rowIndex: Int, data: List<List<String>>, onDataChange: (List<List<String>>) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(4.dp).verticalScroll(rememberScrollState())) {
        Text("Add Row Below", color = Color.White, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
            onDataChange(data.toMutableList().apply { add(rowIndex + 1, List(data[0].size) { "" }) })
            onDismiss()
        }.padding(8.dp))
        Text("Delete Row", color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clickable {
            if (data.size > 1) onDataChange(data.toMutableList().apply { removeAt(rowIndex) })
            onDismiss()
        }.padding(8.dp))
    }
}
