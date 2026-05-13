package com.productivityapp.app.ui.ai

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.productivityapp.app.ui.vault.VaultItem

enum class ActionType {
    CREATE, TOGGLE, DELETE, CREATE_REMINDER
}

data class ProposedAction(
    val type: ActionType,
    val title: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val energyLevel: String? = null,
    val taskId: String? = null
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val secureItem: VaultItem? = null,
    val proposedAction: ProposedAction? = null
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
)

object AIRepository {
    val sessions = mutableStateListOf<ChatSession>().apply {
        // Initial empty session
        add(ChatSession(title = "New Conversation"))
    }

    val currentSession = mutableStateOf(sessions[0])

    fun createNewSession() {
        val newSession = ChatSession(title = "New Conversation")
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
