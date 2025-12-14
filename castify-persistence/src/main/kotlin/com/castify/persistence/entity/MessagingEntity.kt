package com.castify.persistence.entity

import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.Message
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "conversation")
class ConversationEntity(
    @Id
    @Column(name = "conversation_id", columnDefinition = "UUID")
    var conversationId: UUID = UUID.randomUUID(),

    @Column(name = "talent_user_id", nullable = false, columnDefinition = "UUID")
    var talentUserId: UUID,

    @Column(name = "client_user_id", nullable = false, columnDefinition = "UUID")
    var clientUserId: UUID,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "last_message_at", nullable = false)
    var lastMessageAt: Instant = Instant.now()
) {
    fun toDomain(): Conversation = Conversation(
        conversationId = conversationId,
        talentUserId = talentUserId,
        clientUserId = clientUserId,
        createdAt = createdAt,
        lastMessageAt = lastMessageAt
    )

    companion object {
        fun fromDomain(conversation: Conversation): ConversationEntity = ConversationEntity(
            conversationId = conversation.conversationId,
            talentUserId = conversation.talentUserId,
            clientUserId = conversation.clientUserId,
            createdAt = conversation.createdAt,
            lastMessageAt = conversation.lastMessageAt
        )
    }
}

@Entity
@Table(name = "message")
class MessageEntity(
    @Id
    @Column(name = "message_id", columnDefinition = "UUID")
    var messageId: UUID = UUID.randomUUID(),

    @Column(name = "conversation_id", nullable = false, columnDefinition = "UUID")
    var conversationId: UUID,

    @Column(name = "sender_user_id", nullable = false, columnDefinition = "UUID")
    var senderUserId: UUID,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(name = "sent_at", nullable = false)
    var sentAt: Instant = Instant.now(),

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "read_at")
    var readAt: Instant? = null
) {
    fun toDomain(): Message = Message(
        messageId = messageId,
        conversationId = conversationId,
        senderUserId = senderUserId,
        content = content,
        sentAt = sentAt,
        isRead = isRead,
        readAt = readAt
    )

    companion object {
        fun fromDomain(message: Message): MessageEntity = MessageEntity(
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
