package com.castify.persistence.entity

import com.castify.domain.rating.TalentRating
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "talent_rating")
data class TalentRatingEntity(
    @Id
    @Column(name = "rating_id")
    val ratingId: UUID = UUID.randomUUID(),

    @Column(name = "talent_profile_id", nullable = false)
    val talentProfileId: UUID,

    @Column(name = "client_user_id", nullable = false)
    val clientUserId: UUID,

    @Column(name = "collaboration_id")
    val collaborationId: UUID? = null,

    @Column(name = "overall_rating", nullable = false)
    val overallRating: Int,

    @Column(name = "professionalism_rating", nullable = false)
    val professionalismRating: Int,

    @Column(name = "quality_rating", nullable = false)
    val qualityRating: Int,

    @Column(name = "communication_rating", nullable = false)
    val communicationRating: Int,

    @Column(name = "timeliness_rating", nullable = false)
    val timelinessRating: Int,

    @Column(name = "review_text", columnDefinition = "TEXT")
    val reviewText: String? = null,

    @Column(name = "would_work_again", nullable = false)
    val wouldWorkAgain: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean = false
) {
    /**
     * Convert JPA entity to domain model
     */
    fun toDomain(): TalentRating {
        return TalentRating(
            ratingId = ratingId,
            talentProfileId = talentProfileId,
            clientUserId = clientUserId,
            collaborationId = collaborationId,
            overallRating = overallRating,
            professionalismRating = professionalismRating,
            qualityRating = qualityRating,
            communicationRating = communicationRating,
            timelinessRating = timelinessRating,
            reviewText = reviewText,
            wouldWorkAgain = wouldWorkAgain,
            createdAt = createdAt,
            isVerified = isVerified
        )
    }

    companion object {
        /**
         * Convert domain model to JPA entity
         */
        fun fromDomain(rating: TalentRating): TalentRatingEntity {
            return TalentRatingEntity(
                ratingId = rating.ratingId,
                talentProfileId = rating.talentProfileId,
                clientUserId = rating.clientUserId,
                collaborationId = rating.collaborationId,
                overallRating = rating.overallRating,
                professionalismRating = rating.professionalismRating,
                qualityRating = rating.qualityRating,
                communicationRating = rating.communicationRating,
                timelinessRating = rating.timelinessRating,
                reviewText = rating.reviewText,
                wouldWorkAgain = rating.wouldWorkAgain,
                createdAt = rating.createdAt,
                isVerified = rating.isVerified
            )
        }
    }
}
