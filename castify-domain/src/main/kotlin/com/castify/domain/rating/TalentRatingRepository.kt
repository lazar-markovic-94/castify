package com.castify.domain.rating

import java.util.*

/**
 * Repository port for managing talent ratings.
 * This interface defines the contract for rating persistence operations.
 */
interface TalentRatingRepository {
    /**
     * Save a new rating or update an existing one
     */
    fun save(rating: TalentRating): TalentRating

    /**
     * Find a rating by its ID
     */
    fun findById(ratingId: UUID): TalentRating?

    /**
     * Find all ratings for a specific talent profile
     */
    fun findByTalentProfileId(talentProfileId: UUID): List<TalentRating>

    /**
     * Find all ratings given by a specific client
     */
    fun findByClientUserId(clientUserId: UUID): List<TalentRating>

    /**
     * Find rating by talent and collaboration
     * Used to prevent duplicate ratings for the same collaboration
     */
    fun findByTalentProfileIdAndCollaborationId(
        talentProfileId: UUID,
        collaborationId: UUID
    ): TalentRating?

    /**
     * Find rating by talent and client for a specific collaboration
     */
    fun findByTalentProfileIdAndClientUserIdAndCollaborationId(
        talentProfileId: UUID,
        clientUserId: UUID,
        collaborationId: UUID
    ): TalentRating?

    /**
     * Find paginated ratings for a talent profile
     */
    fun findByTalentProfileIdPaginated(
        talentProfileId: UUID,
        page: Int,
        size: Int
    ): List<TalentRating>

    /**
     * Count total ratings for a talent profile
     */
    fun countByTalentProfileId(talentProfileId: UUID): Long

    /**
     * Delete a rating by ID
     * Note: Should be rarely used, as ratings are typically immutable
     */
    fun deleteById(ratingId: UUID)
}
