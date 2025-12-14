package com.castify.persistence.jpa

import com.castify.persistence.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}

@Repository
interface TalentProfileJpaRepository : JpaRepository<TalentProfileEntity, UUID> {
    fun findByUserId(userId: UUID): TalentProfileEntity?
}

@Repository
interface ClientOrgJpaRepository : JpaRepository<ClientOrgEntity, UUID> {
    fun findByUserId(userId: UUID): ClientOrgEntity?
}

@Repository
interface MediaAssetJpaRepository : JpaRepository<MediaAssetEntity, UUID> {
    fun findByUploadedByUserId(uploadedByUserId: UUID): List<MediaAssetEntity>
}

@Repository
interface PortfolioItemJpaRepository : JpaRepository<PortfolioItemEntity, UUID> {
    fun findByTalentProfileId(talentProfileId: UUID): List<PortfolioItemEntity>
}

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

@Repository
interface MessageJpaRepository : JpaRepository<MessageEntity, UUID> {
    fun findByConversationIdOrderBySentAtAsc(conversationId: UUID): List<MessageEntity>
}
