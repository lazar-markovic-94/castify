package com.castify.persistence.jpa

import com.castify.domain.collaboration.CollaborationStatus
import com.castify.persistence.entity.CollaborationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CollaborationJpaRepository : JpaRepository<CollaborationEntity, UUID> {

    /**
     * Find all collaborations for a specific client user
     */
    fun findByClientUserId(clientUserId: UUID): List<CollaborationEntity>

    /**
     * Find all collaborations for a specific talent profile
     */
    fun findByTalentProfileId(talentProfileId: UUID): List<CollaborationEntity>

    /**
     * Find collaborations by client user ID and status
     */
    fun findByClientUserIdAndStatus(
        clientUserId: UUID,
        status: CollaborationStatus
    ): List<CollaborationEntity>

    /**
     * Find collaborations by talent profile ID and status
     */
    fun findByTalentProfileIdAndStatus(
        talentProfileId: UUID,
        status: CollaborationStatus
    ): List<CollaborationEntity>

    /**
     * Check if collaboration exists between client and talent
     */
    fun existsByClientUserIdAndTalentProfileId(
        clientUserId: UUID,
        talentProfileId: UUID
    ): Boolean

    /**
     * Count collaborations by talent profile ID and status
     */
    fun countByTalentProfileIdAndStatus(
        talentProfileId: UUID,
        status: CollaborationStatus
    ): Int
}
