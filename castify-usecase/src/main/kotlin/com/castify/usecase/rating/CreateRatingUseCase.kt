package com.castify.usecase.rating

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.CreateRatingRequest
import com.castify.usecase.dto.RatingResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating a new rating for a talent.
 * Handles rating creation and automatic statistics updates.
 */
@Service
@Transactional
class CreateRatingUseCase(
    private val ratingRepository: TalentRatingRepository,
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(CreateRatingUseCase::class.java)

    /**
     * Creates a new rating for a talent and updates their statistics.
     *
     * @param clientUserId The ID of the client creating the rating
     * @param request The rating creation request
     * @return The created rating response
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws BadRequestException if the client already rated this talent for this collaboration
     */
    fun execute(clientUserId: UUID, request: CreateRatingRequest): RatingResponse {
        return try {
            logger.info("Creating rating for talent: ${request.talentProfileId} by client: $clientUserId")

            // Verify talent profile exists
            if (!talentRepository.existsById(request.talentProfileId)) {
                throw ResourceNotFoundException("Talent profile not found: ${request.talentProfileId}")
            }

            // Check if client already rated this talent for this collaboration
            if (request.collaborationId != null) {
                val existingRating = ratingRepository.findByTalentProfileIdAndClientUserIdAndCollaborationId(
                    request.talentProfileId,
                    clientUserId,
                    request.collaborationId
                )
                if (existingRating != null) {
                    throw BadRequestException("You have already rated this talent for this collaboration")
                }
            }

            // Create the rating
            val rating = TalentRating.create(
                talentProfileId = request.talentProfileId,
                clientUserId = clientUserId,
                collaborationId = request.collaborationId,
                overallRating = request.overallRating,
                professionalismRating = request.professionalismRating,
                qualityRating = request.qualityRating,
                communicationRating = request.communicationRating,
                timelinessRating = request.timelinessRating,
                reviewText = request.reviewText,
                wouldWorkAgain = request.wouldWorkAgain,
                isVerified = request.collaborationId != null // Verified if linked to collaboration
            )

            // Save the rating
            val savedRating = ratingRepository.save(rating)
            logger.info("Successfully created rating: ${savedRating.ratingId}")

            // Update talent statistics
            updateStatisticsWithNewRating(request.talentProfileId, savedRating)

            RatingResponse.fromDomain(savedRating)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: ${request.talentProfileId}", e)
            throw e
        } catch (e: BadRequestException) {
            logger.error("Duplicate rating attempt for talent: ${request.talentProfileId} by client: $clientUserId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error creating rating for talent: ${request.talentProfileId}", e)
            throw RuntimeException("Failed to create rating: ${e.message}", e)
        }
    }

    private fun updateStatisticsWithNewRating(talentProfileId: UUID, rating: TalentRating) {
        try {
            val currentStats = statisticsRepository.findByTalentProfileId(talentProfileId)
                ?: TalentStatistics.createInitial(talentProfileId, 0)

            val updatedStats = TalentStatistics.updateWithNewRating(currentStats, rating)
            statisticsRepository.save(updatedStats)
            logger.info("Updated statistics for talent: $talentProfileId")
        } catch (e: Exception) {
            logger.error("Failed to update statistics for talent: $talentProfileId", e)
            // Don't fail the rating creation if statistics update fails
        }
    }
}
