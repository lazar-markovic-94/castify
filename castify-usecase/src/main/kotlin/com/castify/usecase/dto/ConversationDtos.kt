package com.castify.usecase.dto

import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.Message
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

data class CreateConversationRequest(
    val talentUserId: UUID?,
    val clientUserId: UUID?
)

data class ConversationResponse(
    val conversationId: UUID,
    val talentUserId: UUID,
    val clientUserId: UUID,
    val createdAt: Instant,
    val lastMessageAt: Instant
) {
    companion object {
        fun fromDomain(conversation: Conversation): ConversationResponse = ConversationResponse(
            conversationId = conversation.conversationId,
            talentUserId = conversation.talentUserId,
            clientUserId = conversation.clientUserId,
            createdAt = conversation.createdAt,
            lastMessageAt = conversation.lastMessageAt
        )
    }
}

data class SendMessageRequest(
    @field:NotBlank(message = "Message content is required")
    val content: String?
)

data class MessageResponse(
    val messageId: UUID,
    val conversationId: UUID,
    val senderUserId: UUID,
    val content: String,
    val sentAt: Instant,
    val isRead: Boolean,
    val readAt: Instant?
) {
    companion object {
        fun fromDomain(message: Message): MessageResponse = MessageResponse(
            messageId = message.messageId,
            conversationId = message.conversationId,
            senderUserId = message.senderUserId,
            content = message.content,
            sentAt = message.sentAt,
            isRead = message.isRead,
            readAt = message.readAt
        )
    }
}
