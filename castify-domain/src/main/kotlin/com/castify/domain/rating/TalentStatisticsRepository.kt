package com.castify.domain.rating

import java.util.*

/**
 * Repository port for managing talent statistics.
 * This interface defines the contract for statistics persistence operations.
 */
interface TalentStatisticsRepository {
    /**
     * Save or update talent statistics
     */
    fun save(statistics: TalentStatistics): TalentStatistics

    /**
     * Find statistics by talent profile ID
     */
    fun findByTalentProfileId(talentProfileId: UUID): TalentStatistics?

    /**
     * Find all statistics for multiple talent profiles
     * Useful for batch operations and search results
     */
    fun findByTalentProfileIds(talentProfileIds: List<UUID>): List<TalentStatistics>

    /**
     * Find top rated talents
     * Returns talents ordered by average rating descending
     */
    fun findTopRated(limit: Int): List<TalentStatistics>

    /**
     * Find most successful talents
     * Returns talents ordered by completed collaborations descending
     */
    fun findMostSuccessful(limit: Int): List<TalentStatistics>
}
