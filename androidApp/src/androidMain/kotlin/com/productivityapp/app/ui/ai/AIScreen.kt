package com.productivityapp.app.ui.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import com.productivityapp.app.ui.vault.VaultItem
import com.productivityapp.shared.ai.AIService
import com.productivityapp.app.ui.tasks.TasksRepository
import com.productivityapp.app.ui.vault.VaultRepository
import com.productivityapp.app.ui.reminders.RemindersRepository
import kotlinx.coroutines.launch

// ChatMessage moved to AIRepository.kt

@Composable
fun AIScreen() {
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    
    val session = AIRepository.currentSession.value
    val messages = session.messages
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zenith AI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(
                onClick = { AIRepository.createNewSession() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
            }

            if (isTyping) {
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.width(40.dp).height(2.dp),
                    color = Color(0xFF818CF8),
                    backgroundColor = Color.Transparent
                )
            }
        }
        
        // Chat History
        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF818CF8).copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Zenith Intelligence", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Secure. Local. Intelligent.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    ZenithChatBubble(message)
                }
            }
        }
        
        // Input Area (Flush)
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .defaultMinSize(minHeight = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White, 
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isTyping,
                        maxLines = 5,
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text("Ask Zenith...", color = Color.Gray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isTyping) {
                            val query = inputText
                            
                            // If this is the first message, update session title
                            if (messages.size <= 1) {
                                AIRepository.updateSessionTitle(session, query)
                            }
                            
                            messages.add(ChatMessage(text = query, isUser = true))
                            inputText = ""
                            isTyping = true
                            
                            scope.launch {
                                val systemPrompt = buildSystemPrompt()
                                val response = AIService.getCompletion(query, systemPrompt)
                                isTyping = false
                                
                                if (response != null) {
                                    processResponse(response, messages)
                                } else {
                                    messages.add(ChatMessage(text = "I encountered an error. Please check your connection.", isUser = false))
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    enabled = !isTyping && inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, 
                        contentDescription = "Send", 
                        tint = if (isTyping) Color.Gray else Color(0xFF818CF8), 
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ZenithChatBubble(message: ChatMessage) {
    var isRevealed by remember { mutableStateOf(false) }
    var isActionProcessed by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isUser) Color(0xFF818CF8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (message.isUser) Color(0xFF818CF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                MarkdownText(message.text)
                
                // Zero-Knowledge Handshake Block (Secure Widget)
                if (message.secureItem != null) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                                    Text(message.secureItem.site, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(message.secureItem.username, color = Color.Gray, fontSize = 11.sp)
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
                                            clipboardManager.setText(AnnotatedString(message.secureItem.password))
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = message.secureItem.password,
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
                
                // Proposed Task Action Block
                if (message.proposedAction != null && !isActionProcessed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ProposedTaskWidget(
                        action = message.proposedAction,
                        onConfirm = { updatedAction ->
                            when (updatedAction.type) {
                                ActionType.CREATE -> {
                                    TasksRepository.addTask(
                                        title = updatedAction.title ?: "New Task",
                                        category = updatedAction.category ?: "Work",
                                        priority = updatedAction.priority ?: "Medium",
                                        energyLevel = updatedAction.energyLevel ?: "Medium"
                                    )
                                }
                                ActionType.CREATE_REMINDER -> {
                                    RemindersRepository.addReminder(
                                        title = updatedAction.title ?: "New Reminder",
                                        date = "Today",
                                        time = "Soon",
                                        category = updatedAction.category ?: "Personal",
                                        priority = updatedAction.priority ?: "Medium"
                                    )
                                }
                                ActionType.TOGGLE -> {
                                    updatedAction.taskId?.let { TasksRepository.toggleTask(it) }
                                }
                                ActionType.DELETE -> {
                                    updatedAction.taskId?.let { TasksRepository.deleteTask(it) }
                                }
                            }
                            
                            scope.launch {
                                kotlinx.coroutines.delay(1500)
                                isActionProcessed = true
                                // Add a simple AI acknowledgement
                                val completionText = when(updatedAction.type) {
                                    ActionType.CREATE -> "Task '${updatedAction.title}' created successfully!"
                                    ActionType.CREATE_REMINDER -> "Reminder '${updatedAction.title}' set successfully!"
                                    ActionType.TOGGLE -> "Task updated!"
                                    ActionType.DELETE -> "Task deleted!"
                                }
                                AIRepository.currentSession.value.messages.add(
                                    ChatMessage(text = completionText, isUser = false)
                                )
                            }
                        },
                        onDismiss = { isActionProcessed = true }
                    )
                }
            }
        }
    }
}

@Composable
fun ProposedTaskWidget(
    action: ProposedAction, 
    onConfirm: (ProposedAction) -> Unit, 
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(action.category ?: "Work") }
    var selectedPriority by remember { mutableStateOf(action.priority ?: "Medium") }
    var selectedEnergy by remember { mutableStateOf(action.energyLevel ?: "Medium") }
    var isConfirmed by remember { mutableStateOf(false) }

    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isConfirmed) Color(0xFF22C55E).copy(alpha = 0.3f) else Color(0xFF818CF8).copy(alpha = 0.2f)),
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        if (isConfirmed) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Action Confirmed", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF818CF8).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(action.type) {
                                ActionType.CREATE -> Icons.Default.Add
                                ActionType.CREATE_REMINDER -> Icons.Default.Notifications
                                ActionType.TOGGLE -> Icons.Default.CheckCircle
                                ActionType.DELETE -> Icons.Default.Delete
                            },
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when(action.type) {
                                ActionType.CREATE -> "New Task"
                                ActionType.CREATE_REMINDER -> "New Reminder"
                                ActionType.TOGGLE -> "Toggle Task"
                                ActionType.DELETE -> "Delete Task"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = action.title ?: "Task Action",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (action.type == ActionType.CREATE || action.type == ActionType.CREATE_REMINDER) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priority", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = listOf("High", "Medium", "Low"),
                                initialSelection = selectedPriority,
                                onItemSelected = { selectedPriority = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Category", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            VerticalWheelPicker(
                                options = listOf("Work", "Personal", "Finance", "Social", "Health", "Travel"),
                                initialSelection = selectedCategory,
                                onItemSelected = { selectedCategory = it }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Dismiss", color = Color.Gray, fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = { 
                            onConfirm(action.copy(
                                category = selectedCategory,
                                priority = selectedPriority,
                                energyLevel = selectedEnergy
                            )) 
                            isConfirmed = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF818CF8).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Confirm", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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

@Composable
fun MarkdownText(text: String) {
    val blocks = remember(text) { parseMarkdown(text) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = block.content,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                is MarkdownBlock.Code -> {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = block.content,
                            color = Color(0xFF818CF8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        )
                    }
                }
                is MarkdownBlock.Bullet -> {
                    Row {
                        Text("•", color = Color(0xFF818CF8), modifier = Modifier.padding(horizontal = 8.dp))
                        Text(
                            text = block.content,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val content: androidx.compose.ui.text.AnnotatedString) : MarkdownBlock()
    data class Code(val content: String) : MarkdownBlock()
    data class Bullet(val content: androidx.compose.ui.text.AnnotatedString) : MarkdownBlock()
}

fun parseMarkdown(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.split("\n")
    var inCodeBlock = false
    var currentCode = StringBuilder()
    
    lines.forEach { line ->
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.Code(currentCode.toString().trim()))
                currentCode = StringBuilder()
                inCodeBlock = false
            } else {
                inCodeBlock = true
            }
        } else if (inCodeBlock) {
            currentCode.append(line).append("\n")
        } else if (line.trim().startsWith("* ") || line.trim().startsWith("- ")) {
            blocks.add(MarkdownBlock.Bullet(parseInlineStyles(line.trim().substring(2))))
        } else if (line.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(parseInlineStyles(line)))
        }
    }
    
    if (inCodeBlock) {
        blocks.add(MarkdownBlock.Code(currentCode.toString().trim()))
    }
    
    return if (blocks.isEmpty() && text.isNotBlank()) listOf(MarkdownBlock.Paragraph(parseInlineStyles(text))) else blocks
}

fun parseInlineStyles(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            background = Color.White.copy(alpha = 0.1f),
                            color = Color(0xFF818CF8)
                        ))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("`")
                        i += 1
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

fun buildSystemPrompt(): String {
    val taskCtx = TasksRepository.tasks.joinToString("\n") { 
        "- ${it.title} | Status: ${if (it.isCompleted) "Done" else "Pending"} | Category: ${it.category} | Priority: ${it.priority} [ID: ${it.id}]" 
    }
    val vaultCtx = VaultRepository.vaultItems.joinToString("\n") { "- ${it.site}: ${it.description} (${it.category})" }
    
    return """
        You are Zenith Intelligence, a premium productivity assistant.
        
        ACTION TAGS:
        - To suggest creating a task: [CREATE_TASK: title]
        - To suggest creating a reminder: [CREATE_REMINDER: title]
        - To suggest toggling a task completion: [TOGGLE_TASK: task_id]
        - To suggest deleting a task: [DELETE_TASK: task_id]
        - To suggest revealing a password: [REVEAL: SiteName]
        
        IMPORTANT: NEVER show ID numbers (e.g. UUIDs) to the user. Use them only inside action tags.
        Keep task suggestions simple. The user can refine details like priority and category in the chat widget.
        
        CURRENT CONTEXT:
        TASKS:
        $taskCtx
        
        SECURE VAULT (METADATA ONLY):
        $vaultCtx
    """.trimIndent()
}

fun processResponse(response: String, messages: MutableList<ChatMessage>) {
    val revealPattern = Regex("\\[REVEAL: (.*?)\\]")
    val createPattern = Regex("\\[CREATE_TASK: (.*?)\\]")
    val togglePattern = Regex("\\[TOGGLE_TASK: (.*?)\\]")
    val deletePattern = Regex("\\[DELETE_TASK: (.*?)\\]")
    
    val revealMatch = revealPattern.find(response)
    val createMatch = createPattern.find(response)
    val reminderPattern = Regex("\\[CREATE_REMINDER: (.*?)\\]")
    val reminderMatch = reminderPattern.find(response)
    val toggleMatch = togglePattern.find(response)
    val deleteMatch = deletePattern.find(response)
    
    when {
        revealMatch != null -> {
            val siteName = revealMatch.groupValues[1].trim()
            val cleanText = response.replace(revealMatch.value, "reveal it below")
            val vaultItem = VaultRepository.vaultItems.find { 
                it.site.contains(siteName, ignoreCase = true) || siteName.contains(it.site, ignoreCase = true)
            }
            messages.add(ChatMessage(text = cleanText, isUser = false, secureItem = vaultItem))
        }
        createMatch != null -> {
            val cleanText = response.replace(createMatch.value, "").trim()
            messages.add(ChatMessage(
                text = cleanText,
                isUser = false,
                proposedAction = ProposedAction(
                    type = ActionType.CREATE,
                    title = createMatch.groupValues[1].trim()
                )
            ))
        }
        reminderMatch != null -> {
            val cleanText = response.replace(reminderMatch.value, "").trim()
            messages.add(ChatMessage(
                text = cleanText,
                isUser = false,
                proposedAction = ProposedAction(
                    type = ActionType.CREATE_REMINDER,
                    title = reminderMatch.groupValues[1].trim()
                )
            ))
        }
        toggleMatch != null -> {
            val cleanText = response.replace(toggleMatch.value, "").trim()
            messages.add(ChatMessage(
                text = cleanText,
                isUser = false,
                proposedAction = ProposedAction(
                    type = ActionType.TOGGLE,
                    taskId = toggleMatch.groupValues[1].trim()
                )
            ))
        }
        deleteMatch != null -> {
            val cleanText = response.replace(deleteMatch.value, "").trim()
            messages.add(ChatMessage(
                text = cleanText,
                isUser = false,
                proposedAction = ProposedAction(
                    type = ActionType.DELETE,
                    taskId = deleteMatch.groupValues[1].trim()
                )
            ))
        }
        else -> {
            messages.add(ChatMessage(text = response, isUser = false))
        }
    }
}
