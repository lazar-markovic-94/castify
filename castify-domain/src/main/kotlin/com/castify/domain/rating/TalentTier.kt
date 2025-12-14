package com.castify.domain.rating

/**
 * Represents the tier classification of a talent based on their performance statistics.
 *
 * Tier assignment is based on a combination of factors:
 * - Average rating
 * - Number of completed collaborations
 * - Success rate
 * - Profile completeness
 */
enum class TalentTier {
    /**
     * Premium tier - Top performing talents
     * Requirements:
     * - Average rating >= 4.7
     * - Completed collaborations >= 50
     * - Success rate >= 95%
     * - Profile completeness >= 90%
     */
    PREMIUM,

    /**
     * Gold tier - High performing talents
     * Requirements:
     * - Average rating >= 4.3
     * - Completed collaborations >= 20
     * - Success rate >= 85%
     * - Profile completeness >= 80%
     */
    GOLD,

    /**
     * Silver tier - Good performing talents
     * Requirements:
     * - Average rating >= 3.8
     * - Completed collaborations >= 5
     * - Success rate >= 75%
     * - Profile completeness >= 70%
     */
    SILVER,

    /**
     * Bronze tier - Entry level or new talents
     * Default tier for all talents
     */
    BRONZE;

    companion object {
        /**
         * Calculate the appropriate tier based on talent statistics
         */
        fun fromStatistics(
            averageRating: Double,
            completedCollaborations: Int,
            successRate: Double,
            profileCompleteness: Int
        ): TalentTier {
            return when {
                averageRating >= 4.7 &&
                completedCollaborations >= 50 &&
                successRate >= 95.0 &&
                profileCompleteness >= 90 -> PREMIUM

                averageRating >= 4.3 &&
                completedCollaborations >= 20 &&
                successRate >= 85.0 &&
                profileCompleteness >= 80 -> GOLD

                averageRating >= 3.8 &&
                completedCollaborations >= 5 &&
                successRate >= 75.0 &&
                profileCompleteness >= 70 -> SILVER

                else -> BRONZE
            }
        }
    }
}
