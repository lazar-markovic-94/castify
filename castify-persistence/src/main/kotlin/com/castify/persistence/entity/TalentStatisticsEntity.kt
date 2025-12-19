package com.castify.persistence.entity

import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentTier
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "talent_statistics")
class TalentStatisticsEntity(
    @Id
    @Column(name = "talent_profile_id")
    val talentProfileId: UUID,

    @Column(name = "average_rating", nullable = false)
    var averageRating: Double = 0.0,

    @Column(name = "total_ratings", nullable = false)
    var totalRatings: Int = 0,

    @Column(name = "average_professionalism_rating", nullable = false)
    var averageProfessionalismRating: Double = 0.0,

    @Column(name = "average_quality_rating", nullable = false)
    var averageQualityRating: Double = 0.0,

    @Column(name = "average_communication_rating", nullable = false)
    var averageCommunicationRating: Double = 0.0,

    @Column(name = "average_timeliness_rating", nullable = false)
    var averageTimelinessRating: Double = 0.0,

    @Column(name = "total_collaborations", nullable = false)
    var totalCollaborations: Int = 0,

    @Column(name = "completed_collaborations", nullable = false)
    var completedCollaborations: Int = 0,

    @Column(name = "canceled_collaborations", nullable = false)
    var canceledCollaborations: Int = 0,

    @Column(name = "success_rate", nullable = false)
    var successRate: Double = 0.0,

    @Column(name = "total_earnings", nullable = false, precision = 12, scale = 2)
    var totalEarnings: BigDecimal = BigDecimal.ZERO,

    @Column(name = "average_response_time_hours")
    var averageResponseTimeHours: Double? = null,

    @Column(name = "profile_completeness", nullable = false)
    var profileCompleteness: Int = 0,

    @Column(name = "would_work_again_percentage", nullable = false)
    var wouldWorkAgainPercentage: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_tier", nullable = false, length = 20)
    var performanceTier: TalentTier = TalentTier.BRONZE,

    @Column(name = "last_updated", nullable = false)
    var lastUpdated: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
) {
    /**
     * JPA entities must have equals/hashCode based only on ID to work correctly
     * with lazy loading, proxies, and collections
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TalentStatisticsEntity) return false
        return talentProfileId == other.talentProfileId
    }

    override fun hashCode(): Int = talentProfileId.hashCode()

    override fun toString(): String =
        "TalentStatisticsEntity(talentProfileId=$talentProfileId, averageRating=$averageRating, tier=$performanceTier)"
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
            totalEarnings = totalEarnings.toDouble(),
            averageResponseTimeHours = averageResponseTimeHours,
            profileCompleteness = profileCompleteness,
            wouldWorkAgainPercentage = wouldWorkAgainPercentage,
            tier = performanceTier,
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
                totalEarnings = BigDecimal.valueOf(statistics.totalEarnings),
                averageResponseTimeHours = statistics.averageResponseTimeHours,
                profileCompleteness = statistics.profileCompleteness,
                wouldWorkAgainPercentage = statistics.wouldWorkAgainPercentage,
                performanceTier = statistics.tier,
                lastUpdated = statistics.lastUpdated
            )
        }
    }
}
