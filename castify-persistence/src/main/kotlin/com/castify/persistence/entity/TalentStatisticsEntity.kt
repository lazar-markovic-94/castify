package com.castify.persistence.entity

import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentTier
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "talent_statistics")
data class TalentStatisticsEntity(
    @Id
    @Column(name = "talent_profile_id")
    val talentProfileId: UUID,

    @Column(name = "average_rating", nullable = false)
    val averageRating: Double = 0.0,

    @Column(name = "total_ratings", nullable = false)
    val totalRatings: Int = 0,

    @Column(name = "average_professionalism_rating", nullable = false)
    val averageProfessionalismRating: Double = 0.0,

    @Column(name = "average_quality_rating", nullable = false)
    val averageQualityRating: Double = 0.0,

    @Column(name = "average_communication_rating", nullable = false)
    val averageCommunicationRating: Double = 0.0,

    @Column(name = "average_timeliness_rating", nullable = false)
    val averageTimelinessRating: Double = 0.0,

    @Column(name = "total_collaborations", nullable = false)
    val totalCollaborations: Int = 0,

    @Column(name = "completed_collaborations", nullable = false)
    val completedCollaborations: Int = 0,

    @Column(name = "canceled_collaborations", nullable = false)
    val canceledCollaborations: Int = 0,

    @Column(name = "success_rate", nullable = false)
    val successRate: Double = 0.0,

    @Column(name = "total_earnings", nullable = false)
    val totalEarnings: Double = 0.0,

    @Column(name = "average_response_time_hours")
    val averageResponseTimeHours: Double? = null,

    @Column(name = "profile_completeness", nullable = false)
    val profileCompleteness: Int = 0,

    @Column(name = "would_work_again_percentage", nullable = false)
    val wouldWorkAgainPercentage: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    val tier: TalentTier = TalentTier.BRONZE,

    @Column(name = "last_updated", nullable = false)
    val lastUpdated: Instant = Instant.now()
) {
    /**
     * Convert JPA entity to domain model
     */
    fun toDomain(): TalentStatistics {
        return TalentStatistics(
            talentProfileId = talentProfileId,
            averageRating = averageRating,
            totalRatings = totalRatings,
            averageProfessionalismRating = averageProfessionalismRating,
            averageQualityRating = averageQualityRating,
            averageCommunicationRating = averageCommunicationRating,
            averageTimelinessRating = averageTimelinessRating,
            totalCollaborations = totalCollaborations,
            completedCollaborations = completedCollaborations,
            canceledCollaborations = canceledCollaborations,
            successRate = successRate,
            totalEarnings = totalEarnings,
            averageResponseTimeHours = averageResponseTimeHours,
            profileCompleteness = profileCompleteness,
            wouldWorkAgainPercentage = wouldWorkAgainPercentage,
            tier = tier,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        /**
         * Convert domain model to JPA entity
         */
        fun fromDomain(statistics: TalentStatistics): TalentStatisticsEntity {
            return TalentStatisticsEntity(
                talentProfileId = statistics.talentProfileId,
                averageRating = statistics.averageRating,
                totalRatings = statistics.totalRatings,
                averageProfessionalismRating = statistics.averageProfessionalismRating,
                averageQualityRating = statistics.averageQualityRating,
                averageCommunicationRating = statistics.averageCommunicationRating,
                averageTimelinessRating = statistics.averageTimelinessRating,
                totalCollaborations = statistics.totalCollaborations,
                completedCollaborations = statistics.completedCollaborations,
                canceledCollaborations = statistics.canceledCollaborations,
                successRate = statistics.successRate,
                totalEarnings = statistics.totalEarnings,
                averageResponseTimeHours = statistics.averageResponseTimeHours,
                profileCompleteness = statistics.profileCompleteness,
                wouldWorkAgainPercentage = statistics.wouldWorkAgainPercentage,
                tier = statistics.tier,
                lastUpdated = statistics.lastUpdated
            )
        }
    }
}
