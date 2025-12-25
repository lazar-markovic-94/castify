package com.castify.domain.messaging

import java.time.Instant
import java.util.*

data class Conversation(
    val conversationId: UUID,
    val talentUserId: UUID,
    val clientUserId: UUID,
    val createdAt: Instant = Instant.now(),
    val lastMessageAt: Instant = Instant.now()
) {
    fun updateLastMessageTime(): Conversation = copy(lastMessageAt = Instant.now())

    fun isParticipant(userId: UUID): Boolean = userId == talentUserId || userId == clientUserId
}

data class Message(
    val messageId: UUID,
    val conversationId: UUID,
    val senderUserId: UUID,
    val content: String,
    val sentAt: Instant = Instant.now(),
    val isRead: Boolean = false,
    val readAt: Instant? = null
) {
    init {
        require(content.isNotBlank()) { "Message content cannot be blank" }
    }

    fun markAsRead(): Message = copy(isRead = true, readAt = Instant.now())
}
