package com.castify.persistence.jpa

import com.castify.persistence.entity.TalentRatingEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TalentRatingJpaRepository : JpaRepository<TalentRatingEntity, UUID> {

    /**
     * Find all ratings for a specific talent profile
     */
    fun findByTalentProfileId(talentProfileId: UUID): List<TalentRatingEntity>

    /**
     * Find all ratings given by a specific client
     */
    fun findByClientUserId(clientUserId: UUID): List<TalentRatingEntity>

    /**
     * Find rating by talent and collaboration
     */
    fun findByTalentProfileIdAndCollaborationId(
        talentProfileId: UUID,
        collaborationId: UUID
    ): TalentRatingEntity?

    /**
     * Find rating by talent, client, and collaboration
     */
    fun findByTalentProfileIdAndClientUserIdAndCollaborationId(
        talentProfileId: UUID,
        clientUserId: UUID,
        collaborationId: UUID
    ): TalentRatingEntity?

    /**
     * Find paginated ratings for a talent profile
     */
    fun findByTalentProfileId(
        talentProfileId: UUID,
        pageable: Pageable
    ): List<TalentRatingEntity>

    /**
     * Count total ratings for a talent profile
     */
    fun countByTalentProfileId(talentProfileId: UUID): Long

    /**
     * Find all ratings for a talent profile with a specific verification status
     */
    fun findByTalentProfileIdAndIsVerified(
        talentProfileId: UUID,
        isVerified: Boolean
    ): List<TalentRatingEntity>
}
