package com.castify.persistence.jpa

import com.castify.persistence.entity.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConversationJpaRepository : JpaRepository<ConversationEntity, UUID> {
    @Query(
        """
        SELECT c FROM ConversationEntity c
        WHERE c.talentUserId = :userId OR c.clientUserId = :userId
        ORDER BY c.lastMessageAt DESC
        """
    )
    fun findByUserId(@Param("userId") userId: UUID): List<ConversationEntity>

    @Query(
        """
        SELECT c FROM ConversationEntity c
        WHERE (c.talentUserId = :talentUserId AND c.clientUserId = :clientUserId)
           OR (c.talentUserId = :clientUserId AND c.clientUserId = :talentUserId)
        """
    )
    fun findConversationBetweenUsers(
        @Param("talentUserId") talentUserId: UUID,
        @Param("clientUserId") clientUserId: UUID
    ): ConversationEntity?
}
