package com.castify.domain.rating

import com.castify.domain.exception.BadRequestException
import java.time.Instant
import java.util.*

/**
 * Represents a rating given to a talent by a client after a collaboration.
 *
 * Ratings consist of multiple dimensions:
 * - Overall rating (1-5 stars)
 * - Category-specific ratings (professionalism, quality, communication, timeliness)
 * - Optional written review
 *
 * Business rules:
 * - Only verified clients can rate talents
 * - A client can only rate a talent once per collaboration
 * - Ratings cannot be modified after submission
 * - All rating values must be between 1 and 5
 */
data class TalentRating(
    val ratingId: UUID,
    val talentProfileId: UUID,
    val clientUserId: UUID,
    val collaborationId: UUID?,  // Optional reference to specific collaboration/project

    // Rating dimensions (1-5 scale)
    val overallRating: Int,
    val professionalismRating: Int,
    val qualityRating: Int,
    val communicationRating: Int,
    val timelinessRating: Int,

    // Written review
    val reviewText: String?,

    // Would the client work with this talent again?
    val wouldWorkAgain: Boolean,

    // Metadata
    val createdAt: Instant,
    val isVerified: Boolean = false  // True if collaboration was confirmed
) {
    init {
        validateRating(overallRating, "Overall rating")
        validateRating(professionalismRating, "Professionalism rating")
        validateRating(qualityRating, "Quality rating")
        validateRating(communicationRating, "Communication rating")
        validateRating(timelinessRating, "Timeliness rating")

        reviewText?.let {
            if (it.length > 2000) {
                throw BadRequestException("Review text cannot exceed 2000 characters")
            }
        }
    }

    /**
     * Calculate the average of all category ratings
     */
    fun calculateAverageRating(): Double {
        return (overallRating + professionalismRating + qualityRating +
                communicationRating + timelinessRating) / 5.0
    }

    private fun validateRating(value: Int, fieldName: String) {
        if (value !in 1..5) {
            throw BadRequestException("$fieldName must be between 1 and 5")
        }
    }

    companion object {
        /**
         * Create a new rating
         */
        fun create(
            talentProfileId: UUID,
            clientUserId: UUID,
            collaborationId: UUID?,
            overallRating: Int,
            professionalismRating: Int,
            qualityRating: Int,
            communicationRating: Int,
            timelinessRating: Int,
            reviewText: String?,
            wouldWorkAgain: Boolean,
            isVerified: Boolean = false
        ): TalentRating {
            return TalentRating(
                ratingId = UUID.randomUUID(),
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
                createdAt = Instant.now(),
                isVerified = isVerified
            )
        }
    }
}
