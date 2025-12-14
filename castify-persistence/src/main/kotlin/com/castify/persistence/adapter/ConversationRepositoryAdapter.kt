package com.castify.persistence.adapter

import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import com.castify.domain.messaging.Message
import com.castify.persistence.entity.ConversationEntity
import com.castify.persistence.entity.MessageEntity
import com.castify.persistence.jpa.ConversationJpaRepository
import com.castify.persistence.jpa.MessageJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class ConversationRepositoryAdapter(
    private val conversationJpaRepository: ConversationJpaRepository,
    private val messageJpaRepository: MessageJpaRepository
) : ConversationRepository {

    override fun saveConversation(conversation: Conversation): Conversation {
        val entity = ConversationEntity.fromDomain(conversation)
        val saved = conversationJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findConversationById(conversationId: UUID): Conversation? {
        return conversationJpaRepository.findById(conversationId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findConversationsByUser(userId: UUID): List<Conversation> {
        return conversationJpaRepository.findByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findConversationBetweenUsers(talentUserId: UUID, clientUserId: UUID): Conversation? {
        return conversationJpaRepository.findConversationBetweenUsers(talentUserId, clientUserId)?.toDomain()
    }

    override fun saveMessage(message: Message): Message {
        val entity = MessageEntity.fromDomain(message)
        val saved = messageJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findMessageById(messageId: UUID): Message? {
        return messageJpaRepository.findById(messageId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findMessagesByConversation(conversationId: UUID): List<Message> {
        return messageJpaRepository.findByConversationIdOrderBySentAtAsc(conversationId)
            .map { it.toDomain() }
    }
}
