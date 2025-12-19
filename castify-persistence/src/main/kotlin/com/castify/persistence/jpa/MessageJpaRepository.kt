package com.castify.persistence.jpa

import com.castify.persistence.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageJpaRepository : JpaRepository<MessageEntity, UUID> {
    fun findByConversationIdOrderBySentAtAsc(conversationId: UUID): List<MessageEntity>
}
