package dev.soupslurpr.transcribro.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

enum class ChatState {
    IDLE,
    LISTENING,
    PROCESSING_SPEECH,
    WAITING_FOR_LLM,
    SPEAKING
}
