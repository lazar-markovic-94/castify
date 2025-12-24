package com.castify.domain.rating

import java.time.Instant
import java.util.*

/**
 * Aggregate statistics for a talent profile.
 *
 * These statistics are calculated based on:
 * - Ratings received from clients
 * - Collaboration history
 * - Profile completeness
 * - Response and completion times
 *
 * Statistics are used to:
 * - Determine talent tier (Premium, Gold, Silver, Bronze)
 * - Display reputation and performance metrics
 * - Rank talents in search results
 */
data class TalentStatistics(
    val talentProfileId: UUID,

    // Rating metrics
    val averageRating: Double,
    val totalRatings: Int,
    val averageProfessionalismRating: Double,
    val averageQualityRating: Double,
    val averageCommunicationRating: Double,
    val averageTimelinessRating: Double,

    // Collaboration metrics
    val totalCollaborations: Int,
    val completedCollaborations: Int,
    val canceledCollaborations: Int,
    val successRate: Double,  // Percentage of completed vs total collaborations

    // Financial metrics
    val totalEarnings: Double,

    // Engagement metrics
    val averageResponseTimeHours: Double?,
    val profileCompleteness: Int,  // 0-100 percentage

    // Recommendation
    val wouldWorkAgainPercentage: Double,  // Percentage of clients who would work again

    // Tier
    val tier: TalentTier,

    // Metadata
    val lastUpdated: Instant
) {
    /**
     * Calculate profile completeness score (0-100)
     */
    fun calculateProfileCompleteness(
        hasBio: Boolean,
        hasLocation: Boolean,
        hasProfileImage: Boolean,
        languagesCount: Int,
        tagsCount: Int,
        capabilitiesCount: Int
    ): Int {
        var score = 0

        // Basic info (50 points)
        if (hasBio) score += 15
        if (hasLocation) score += 15
        if (hasProfileImage) score += 20

        // Skills and capabilities (50 points)
        if (languagesCount > 0) score += 15
        if (languagesCount >= 2) score += 5  // Bonus for multiple languages
        if (tagsCount >= 3) score += 15
        if (tagsCount >= 5) score += 5  // Bonus for comprehensive tags
        if (capabilitiesCount > 0) score += 10

        return score.coerceIn(0, 100)
    }

    /**
     * Recalculate tier based on current statistics
     */
    fun recalculateTier(): TalentTier {
        return TalentTier.fromStatistics(
            averageRating = averageRating,
            completedCollaborations = completedCollaborations,
            successRate = successRate,
            profileCompleteness = profileCompleteness
        )
    }

    companion object {
        /**
         * Create initial statistics for a new talent profile
         */
        fun createInitial(
            talentProfileId: UUID,
            profileCompleteness: Int
        ): TalentStatistics {
            return TalentStatistics(
                talentProfileId = talentProfileId,
                averageRating = 0.0,
                totalRatings = 0,
                averageProfessionalismRating = 0.0,
                averageQualityRating = 0.0,
                averageCommunicationRating = 0.0,
                averageTimelinessRating = 0.0,
                totalCollaborations = 0,
                completedCollaborations = 0,
                canceledCollaborations = 0,
                successRate = 0.0,
                totalEarnings = 0.0,
                averageResponseTimeHours = null,
                profileCompleteness = profileCompleteness,
                wouldWorkAgainPercentage = 0.0,
                tier = TalentTier.BRONZE,
                lastUpdated = Instant.now()
            )
        }

        /**
         * Update statistics with a new rating
         */
        fun updateWithNewRating(
            current: TalentStatistics,
            newRating: TalentRating
        ): TalentStatistics {
            val newTotalRatings = current.totalRatings + 1

            // Calculate new averages using incremental mean formula
            val newAverageRating = calculateNewAverage(
                current.averageRating,
                newRating.overallRating.toDouble(),
                current.totalRatings
            )
            val newAvgProfessionalism = calculateNewAverage(
                current.averageProfessionalismRating,
                newRating.professionalismRating.toDouble(),
                current.totalRatings
            )
            val newAvgQuality = calculateNewAverage(
                current.averageQualityRating,
                newRating.qualityRating.toDouble(),
                current.totalRatings
            )
            val newAvgCommunication = calculateNewAverage(
                current.averageCommunicationRating,
                newRating.communicationRating.toDouble(),
                current.totalRatings
            )
            val newAvgTimeliness = calculateNewAverage(
                current.averageTimelinessRating,
                newRating.timelinessRating.toDouble(),
                current.totalRatings
            )

            // Update would work again percentage
            val wouldWorkAgainCount = (current.wouldWorkAgainPercentage / 100.0 * current.totalRatings) +
                    if (newRating.wouldWorkAgain) 1 else 0
            val newWouldWorkAgainPercentage = (wouldWorkAgainCount / newTotalRatings) * 100.0

            val updated = current.copy(
                averageRating = newAverageRating,
                totalRatings = newTotalRatings,
                averageProfessionalismRating = newAvgProfessionalism,
                averageQualityRating = newAvgQuality,
                averageCommunicationRating = newAvgCommunication,
                averageTimelinessRating = newAvgTimeliness,
                wouldWorkAgainPercentage = newWouldWorkAgainPercentage,
                lastUpdated = Instant.now()
            )

            // Recalculate tier
            return updated.copy(tier = updated.recalculateTier())
        }

        /**
         * Update statistics when a collaboration is completed
         */
        fun updateWithCompletedCollaboration(
            current: TalentStatistics,
            earnings: Double
        ): TalentStatistics {
            val newCompleted = current.completedCollaborations + 1
            val newTotal = current.totalCollaborations + 1
            val newSuccessRate = (newCompleted.toDouble() / newTotal) * 100.0

            val updated = current.copy(
                completedCollaborations = newCompleted,
                totalCollaborations = newTotal,
                successRate = newSuccessRate,
                totalEarnings = current.totalEarnings + earnings,
                lastUpdated = Instant.now()
            )

            // Recalculate tier
            return updated.copy(tier = updated.recalculateTier())
        }

        /**
         * Update statistics when a collaboration is canceled
         */
        fun updateWithCanceledCollaboration(current: TalentStatistics): TalentStatistics {
            val newCanceled = current.canceledCollaborations + 1
            val newTotal = current.totalCollaborations + 1
            val newSuccessRate = if (newTotal > 0) {
                (current.completedCollaborations.toDouble() / newTotal) * 100.0
            } else {
                0.0
            }

            val updated = current.copy(
                canceledCollaborations = newCanceled,
                totalCollaborations = newTotal,
                successRate = newSuccessRate,
                lastUpdated = Instant.now()
            )

            // Recalculate tier
            return updated.copy(tier = updated.recalculateTier())
        }

        private fun calculateNewAverage(
            currentAverage: Double,
            newValue: Double,
            currentCount: Int
        ): Double {
            if (currentCount == 0) return newValue
            return ((currentAverage * currentCount) + newValue) / (currentCount + 1)
        }
    }
}
