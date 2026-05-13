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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.productivityapp.app.ui.vault.VaultItem
import com.productivityapp.shared.ai.AIService
import com.productivityapp.app.ui.tasks.TasksRepository
import com.productivityapp.app.ui.vault.VaultRepository
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
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                ZenithChatBubble(message)
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
    val clipboardManager = LocalClipboardManager.current

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
    val taskCtx = TasksRepository.tasks.joinToString("\n") { "- ${it.title} (${it.category}, ${it.priority}, ${it.energyLevel} energy)" }
    val vaultCtx = VaultRepository.vaultItems.joinToString("\n") { "- ${it.site}: ${it.description} (${it.category})" }
    
    return """
        You are Zenith Intelligence, a premium productivity assistant.
        You are professional, concise, and helpful.
        
        SECURITY RULE: NEVER show passwords. If a user asks for a password or login details, identify the account and then use the special tag [REVEAL: SiteName] to trigger the local secure reveal UI.
        Example: "I found your Netflix account. Would you like to [REVEAL: Netflix]?"
        
        CURRENT CONTEXT:
        TASKS:
        $taskCtx
        
        SECURE VAULT (METADATA ONLY):
        $vaultCtx
        
        Answer based on this context. If something isn't here, say you don't have access to that information.
    """.trimIndent()
}

fun processResponse(response: String, messages: MutableList<ChatMessage>) {
    val revealPattern = Regex("\\[REVEAL: (.*?)\\]")
    val match = revealPattern.find(response)
    
    if (match != null) {
        val siteName = match.groupValues[1].trim()
        val cleanText = response.replace(revealPattern, "reveal it below")
        val vaultItem = VaultRepository.vaultItems.find { 
            it.site.equals(siteName, ignoreCase = true) || 
            it.site.contains(siteName, ignoreCase = true) || 
            siteName.contains(it.site, ignoreCase = true)
        }
        
        messages.add(ChatMessage(
            text = cleanText,
            isUser = false,
            secureItem = vaultItem
        ))
    } else {
        messages.add(ChatMessage(text = response, isUser = false))
    }
}
