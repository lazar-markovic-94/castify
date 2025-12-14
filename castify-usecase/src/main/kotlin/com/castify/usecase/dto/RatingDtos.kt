package com.castify.usecase.dto

import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentTier
import java.time.Instant
import java.util.*

// ==================== Request DTOs ====================

/**
 * Request DTO for creating a new talent rating
 */
data class CreateRatingRequest(
    val talentProfileId: UUID,
    val collaborationId: UUID?,
    val overallRating: Int,
    val professionalismRating: Int,
    val qualityRating: Int,
    val communicationRating: Int,
    val timelinessRating: Int,
    val reviewText: String?,
    val wouldWorkAgain: Boolean
)

/**
 * Request DTO for fetching paginated ratings
 */
data class GetRatingsRequest(
    val talentProfileId: UUID,
    val page: Int = 0,
    val size: Int = 10
)

// ==================== Response DTOs ====================

/**
 * Response DTO for a single rating
 */
data class RatingResponse(
    val ratingId: UUID,
    val talentProfileId: UUID,
    val clientUserId: UUID,
    val collaborationId: UUID?,
    val overallRating: Int,
    val professionalismRating: Int,
    val qualityRating: Int,
    val communicationRating: Int,
    val timelinessRating: Int,
    val averageRating: Double,
    val reviewText: String?,
    val wouldWorkAgain: Boolean,
    val createdAt: Instant,
    val isVerified: Boolean
) {
    companion object {
        fun fromDomain(rating: TalentRating): RatingResponse {
            return RatingResponse(
                ratingId = rating.ratingId,
                talentProfileId = rating.talentProfileId,
                clientUserId = rating.clientUserId,
                collaborationId = rating.collaborationId,
                overallRating = rating.overallRating,
                professionalismRating = rating.professionalismRating,
                qualityRating = rating.qualityRating,
                communicationRating = rating.communicationRating,
                timelinessRating = rating.timelinessRating,
                averageRating = rating.calculateAverageRating(),
                reviewText = rating.reviewText,
                wouldWorkAgain = rating.wouldWorkAgain,
                createdAt = rating.createdAt,
                isVerified = rating.isVerified
            )
        }
    }
}

/**
 * Response DTO for paginated ratings
 */
data class RatingsPageResponse(
    val ratings: List<RatingResponse>,
    val totalRatings: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

/**
 * Response DTO for talent statistics
 */
data class StatisticsResponse(
    val talentProfileId: UUID,
    val averageRating: Double,
    val totalRatings: Int,
    val ratingBreakdown: RatingBreakdownResponse,
    val collaborationMetrics: CollaborationMetricsResponse,
    val financialMetrics: FinancialMetricsResponse,
    val engagementMetrics: EngagementMetricsResponse,
    val tier: TalentTier,
    val lastUpdated: Instant
) {
    companion object {
        fun fromDomain(statistics: TalentStatistics): StatisticsResponse {
            return StatisticsResponse(
                talentProfileId = statistics.talentProfileId,
                averageRating = statistics.averageRating,
                totalRatings = statistics.totalRatings,
                ratingBreakdown = RatingBreakdownResponse(
                    averageProfessionalism = statistics.averageProfessionalismRating,
                    averageQuality = statistics.averageQualityRating,
                    averageCommunication = statistics.averageCommunicationRating,
                    averageTimeliness = statistics.averageTimelinessRating
                ),
                collaborationMetrics = CollaborationMetricsResponse(
                    totalCollaborations = statistics.totalCollaborations,
                    completedCollaborations = statistics.completedCollaborations,
                    canceledCollaborations = statistics.canceledCollaborations,
                    successRate = statistics.successRate,
                    wouldWorkAgainPercentage = statistics.wouldWorkAgainPercentage
                ),
                financialMetrics = FinancialMetricsResponse(
                    totalEarnings = statistics.totalEarnings
                ),
                engagementMetrics = EngagementMetricsResponse(
                    averageResponseTimeHours = statistics.averageResponseTimeHours,
                    profileCompleteness = statistics.profileCompleteness
                ),
                tier = statistics.tier,
                lastUpdated = statistics.lastUpdated
            )
        }
    }
}

/**
 * Breakdown of rating categories
 */
data class RatingBreakdownResponse(
    val averageProfessionalism: Double,
    val averageQuality: Double,
    val averageCommunication: Double,
    val averageTimeliness: Double
)

/**
 * Collaboration metrics
 */
data class CollaborationMetricsResponse(
    val totalCollaborations: Int,
    val completedCollaborations: Int,
    val canceledCollaborations: Int,
    val successRate: Double,
    val wouldWorkAgainPercentage: Double
)

/**
 * Financial metrics
 */
data class FinancialMetricsResponse(
    val totalEarnings: Double
)

/**
 * Engagement metrics
 */
data class EngagementMetricsResponse(
    val averageResponseTimeHours: Double?,
    val profileCompleteness: Int
)

/**
 * Response DTO for leaderboard
 */
data class LeaderboardResponse(
    val topRated: List<LeaderboardEntryResponse>,
    val mostSuccessful: List<LeaderboardEntryResponse>
)

/**
 * Single leaderboard entry
 */
data class LeaderboardEntryResponse(
    val talentProfileId: UUID,
    val averageRating: Double,
    val completedCollaborations: Int,
    val tier: TalentTier
)
