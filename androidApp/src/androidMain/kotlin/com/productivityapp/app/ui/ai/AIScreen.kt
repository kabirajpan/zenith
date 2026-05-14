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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import com.productivityapp.app.ui.vault.VaultItem
import com.productivityapp.app.ui.ai.widgets.UniversalActionCard
import com.productivityapp.app.ui.ai.widgets.UniversalWidgetAssembler
import com.productivityapp.app.ui.ai.widgets.HeaderSection
import com.productivityapp.app.ui.ai.widgets.SectionDivider
import com.productivityapp.shared.ai.AIService
import com.productivityapp.app.ui.tasks.TasksRepository
import com.productivityapp.app.ui.vault.VaultRepository
import com.productivityapp.app.ui.reminders.RemindersRepository
import com.productivityapp.app.ui.alarm.AlarmRepository
import com.productivityapp.app.ui.notes.NotesRepository
import com.productivityapp.app.ui.common.*
import com.productivityapp.app.ui.vault.components.VaultSecureRevealWidget
import kotlinx.serialization.json.*
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
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
                                // Single Phase: Zenith Intelligence Injection
                                val systemPrompt = buildZenithSystemPrompt()
                                val finalResponse = AIService.getCompletion(query, systemPrompt)
                                
                                isTyping = false
                                
                                if (finalResponse != null) {
                                    processResponse(finalResponse, messages)
                                } else {
                                    messages.add(ChatMessage(text = "I couldn't reach Zenith Intelligence. Please check your connection.", isUser = false))
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

fun buildContextSummary(): String {
    val tasks = TasksRepository.tasks.take(10).joinToString("\n") { 
        "- ${it.title} [Status: ${it.status}, Priority: ${it.priority}, ID: ${it.id}]" 
    }
    val reminders = RemindersRepository.reminders.take(5).joinToString("\n") { 
        "- ${it.title} at ${it.time} on ${it.date} [ID: ${it.id}]" 
    }
    val vault = VaultRepository.vaultItems.joinToString("\n") { 
        "- ${it.title} (Type: ${it.type.name})" 
    }
    val alarms = AlarmRepository.alarms.joinToString("\n") {
        "- ${it.time} [Label: ${it.label}, Days: ${it.repeatDays.joinToString(",")}, Enabled: ${it.isEnabled}, ID: ${it.id}]"
    }
    
    return """
        USER DATA SNAPSHOT:
        --- TASKS ---
        ${tasks.ifBlank { "No active tasks." }}
        
        --- REMINDERS ---
        ${reminders.ifBlank { "No active reminders." }}
        
        --- ALARMS ---
        ${alarms.ifBlank { "No alarms set." }}
        
        --- VAULT TITLES ---
        ${vault.ifBlank { "Vault is empty." }}
    """.trimIndent()
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
                
                // Proposed Action Block (Tokenized)
                if (message.proposedAction != null && !message.isProcessed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val onConfirm: (ProposedAction) -> Unit = { updatedAction ->
                        message.isProcessed = true
                        when (updatedAction.type) {
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
                                when (updatedAction.module) {
                                    "Tasks" -> updatedAction.targetId?.let { TasksRepository.toggleTask(it) }
                                    "Alarms" -> updatedAction.targetId?.let { AlarmRepository.toggleAlarm(it) }
                                }
                            }
                            ActionType.DELETE -> {
                                when (updatedAction.module) {
                                    "Tasks" -> updatedAction.targetId?.let { TasksRepository.deleteTask(it) }
                                    "Reminders" -> updatedAction.targetId?.let { RemindersRepository.deleteReminder(it) }
                                    "Alarms" -> updatedAction.targetId?.let { AlarmRepository.deleteAlarm(it) }
                                }
                            }
                            ActionType.CREATE -> {
                                if (updatedAction.module == "Alarms") {
                                    AlarmRepository.addAlarm(
                                        time = updatedAction.time ?: "07:00 AM",
                                        label = updatedAction.title?.takeIf { it != updatedAction.time } ?: "Alarm",
                                        repeatDays = updatedAction.repeatDays ?: emptyList()
                                    )
                                } else {
                                    // Handle Tasks (Existing logic)
                                    val cat = com.productivityapp.model.TaskCategory.entries.find { it.name.equals(updatedAction.category, ignoreCase = true) } ?: com.productivityapp.model.TaskCategory.WORK
                                    val prio = com.productivityapp.model.TaskPriority.entries.find { it.name.equals(updatedAction.priority, ignoreCase = true) } ?: com.productivityapp.model.TaskPriority.MEDIUM
                                    TasksRepository.addTask(
                                        title = updatedAction.title ?: "New Task",
                                        category = cat,
                                        priority = prio
                                    )
                                }
                            }
                            ActionType.UPDATE, ActionType.EDIT, ActionType.RENAME -> {
                                when (updatedAction.module) {
                                    "Alarms" -> {
                                        updatedAction.targetId?.let { id ->
                                            AlarmRepository.updateAlarm(
                                                id = id,
                                                time = updatedAction.time ?: "07:00 AM",
                                                label = updatedAction.title ?: "Alarm",
                                                repeatDays = updatedAction.repeatDays ?: emptyList(),
                                                isVibrate = true,
                                                escalationType = "Standard",
                                                sound = "Default"
                                            )
                                        }
                                    }
                                    "Notes" -> {
                                        NotesRepository.saveOrUpdateNote(
                                            id = updatedAction.targetId,
                                            title = updatedAction.title ?: "Updated Note",
                                            summary = "Updated via AI Assistant",
                                            blocks = emptyList()
                                        )
                                    }
                                }
                            }
                            ActionType.ADD_NOTE_BLOCK -> {
                                if (updatedAction.blockType == "table") {
                                    val rows = updatedAction.blockContent?.split("||")?.map { it.split("|").map { it.trim() } } ?: emptyList()
                                    NotesRepository.saveOrUpdateNote(
                                        id = null,
                                        title = updatedAction.title ?: "AI Suggested Note",
                                        summary = "Table generated by AI",
                                        blocks = listOf(com.productivityapp.model.NoteBlock.Table(rows))
                                    )
                                }
                            }
                            else -> {}
                        }
                        
                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            val completionText = "Confirmed! I've updated your ${updatedAction.module ?: "item"}."
                            AIRepository.currentSession.value.messages.add(
                                ChatMessage(text = completionText, isUser = false)
                            )
                        }
                    }
                    
                    val onDismiss = { message.isProcessed = true }

                    // Universal High-Performance Widget System
                    when {
                        message.secureItem != null -> {
                            UniversalActionCard {
                                HeaderSection(icon = Icons.Default.Lock, title = "Vault", action = "REVEAL SECURE")
                                SectionDivider()
                                VaultSecureRevealWidget(message = message, onProcessed = { onConfirm(message.proposedAction) })
                            }
                        }
                        message.proposedAction.type != ActionType.NONE -> {
                            UniversalWidgetAssembler(
                                action = message.proposedAction,
                                isProcessed = message.isProcessed,
                                onConfirm = { onConfirm(message.proposedAction) },
                                onDismiss = onDismiss
                            )
                        }
                    }
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

fun buildZenithSystemPrompt(): String {
    return """
        You are Zenith, a premium productivity assistant. You are secure, local, and intelligent.

        ## YOUR CAPABILITIES
        You manage Tasks, Alarms, Reminders, Vault Credentials, and Notes.
        
        ## DATA PRIVACY
        - You see metadata for Vault items (Titles) but NEVER passwords.
        - To show a password to the user, you trigger a REVEAL widget.

        ## RESPONSE PROTOCOL
        1. For chat/questions: Respond in clean Markdown plain text.
        2. For actions: Respond with text AND a WIDGET command on a NEW LINE at the very end.

        ## WIDGET FORMAT (JSON based)
        WIDGET::MODULE::ACTION::{"key": "value"}

        Supported Commands:
        - WIDGET::TASK::CREATE::{"title": "...", "priority": "HIGH/MEDIUM/LOW", "category": "WORK/PERSONAL"}
        - WIDGET::VAULT::REVEAL::{"title": "Exact Title from Vault"}
        - WIDGET::ALARM::CREATE::{"label": "...", "time": "07:30 AM", "days": ["Mo", "Su"]}
        - WIDGET::ALARM::UPDATE::{"id": "...", "time": "08:00 AM", "label": "..."}
        - WIDGET::ALARM::DELETE::{"id": "..."}
        - WIDGET::ALARM::TOGGLE::{"id": "..."}
        - WIDGET::REMINDER::CREATE::{"title": "...", "date": "Today/Tomorrow", "time": "09:00 AM"}
        - WIDGET::TASK::DELETE::{"id": "..."}
        - WIDGET::TASK::TOGGLE::{"id": "..."}

        RULES:
        1. For Alarms, use WIDGET::ALARM::TOGGLE to enable or disable an existing alarm.
        2. For Alarms, use WIDGET::ALARM::UPDATE to change the time or label of an existing alarm.
        3. Always look for the ID in the USER DATA SNAPSHOT before performing UPDATE, DELETE, or TOGGLE.

        ## EXAMPLES
        User: "hi"
        Response: "Hello! I'm Zenith. How can I assist you with your productivity today?"

        User: "what's my Netflix password?"
        Response: "I'll pull that up for you securely.
        WIDGET::VAULT::REVEAL::{"title": "Netflix"}"

        User: "weekly budget review is done"
        Response: "Excellent work! I've marked that as completed for you.
        WIDGET::TASK::TOGGLE::{"id": "ID_FROM_CONTEXT"}"

        ## USER'S CURRENT DATA
        ${buildContextSummary()}

        ## RULES (STRICT)
        1. ACTIONS REQUIRING WIDGETS: Creating, Deleting, or Changing Status (done/pending).
        2. NEVER fake an update in your text response. If you say "I've updated it", you MUST include the WIDGET command.
        3. ALWAYS use the exact ID from the 'USER DATA SNAPSHOT' for TOGGLE, DELETE, or UPDATE actions.
        4. NEVER output [SYSTEM STATUS] or [USER_STATUS] blocks.
        5. Keep responses concise and professional.
    """.trimIndent()
}

fun processResponse(response: String, messages: MutableList<ChatMessage>) {
    val widgetPattern = Regex("""WIDGET::(\w+)::(\w+)::(\{.*\})""", RegexOption.DOT_MATCHES_ALL)
    val match = widgetPattern.find(response)
    
    var cleanText = response
    var proposedAction: ProposedAction? = null
    var secureItem: VaultItem? = null

    if (match != null) {
        cleanText = response.replace(match.value, "").trim()
        val module = match.groupValues[1]
        val action = match.groupValues[2]
        val jsonStr = match.groupValues[3]
        
        try {
            val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(jsonStr) as? JsonObject
            
            fun getJsonString(key: String): String? {
                return json?.get(key)?.jsonPrimitive?.contentOrNull
            }

            if (module == "VAULT" && action == "REVEAL") {
                val title = getJsonString("title")
                secureItem = VaultRepository.vaultItems.find { it.title.equals(title, ignoreCase = true) }
            } else {
                proposedAction = ProposedAction(
                    type = when(action) {
                        "CREATE" -> if (module == "REMINDER") ActionType.CREATE_REMINDER else ActionType.CREATE
                        "DELETE" -> ActionType.DELETE
                        "TOGGLE" -> ActionType.TOGGLE
                        "UPDATE" -> ActionType.UPDATE
                        else -> ActionType.NONE
                    },
                    module = when(module) {
                        "TASK" -> "Tasks"
                        "REMINDER" -> "Reminders"
                        "ALARM" -> "Alarms"
                        else -> module
                    },
                    title = getJsonString("title") ?: getJsonString("label"),
                    time = getJsonString("time"),
                    category = getJsonString("category"),
                    targetId = getJsonString("id"),
                    priority = getJsonString("priority"),
                    repeatDays = json?.get("days")?.let { arr ->
                        (arr as? kotlinx.serialization.json.JsonArray)?.mapNotNull { (it as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull }
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    messages.add(ChatMessage(
        text = cleanText,
        isUser = false,
        proposedAction = proposedAction ?: ProposedAction(ActionType.NONE, "Unknown"),
        secureItem = secureItem
    ))
}
