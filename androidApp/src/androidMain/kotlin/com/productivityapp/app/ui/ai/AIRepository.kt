package com.productivityapp.app.ui.ai

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.productivityapp.app.ui.vault.VaultItem

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val secureItem: VaultItem? = null
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
)

object AIRepository {
    private val defaultMessage = ChatMessage(
        text = "Hello! I'm your Zenith Intelligence. How can I help you today?",
        isUser = false
    )

    val sessions = mutableStateListOf<ChatSession>().apply {
        // Initial session
        add(ChatSession(title = "New Conversation", messages = mutableStateListOf(defaultMessage)))
    }

    val currentSession = mutableStateOf(sessions[0])

    fun createNewSession() {
        val newSession = ChatSession(
            title = "New Conversation", 
            messages = mutableStateListOf(defaultMessage)
        )
        sessions.add(0, newSession)
        currentSession.value = newSession
    }

    fun switchSession(session: ChatSession) {
        currentSession.value = session
    }
    
    fun updateSessionTitle(session: ChatSession, firstMessage: String) {
        val title = if (firstMessage.length > 20) firstMessage.take(20) + "..." else firstMessage
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index != -1) {
            sessions[index] = sessions[index].copy(title = title)
            if (currentSession.value.id == session.id) {
                currentSession.value = sessions[index]
            }
        }
    }
}
