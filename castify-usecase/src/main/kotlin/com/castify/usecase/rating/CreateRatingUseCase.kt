package com.castify.usecase.rating

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
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
    private val talentRepository: TalentRepository,
    private val statisticsService: TalentStatisticsService
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

            // Update statistics in application layer (replaces database trigger)
            statisticsService.updateWithNewRating(savedRating)
            logger.info("Updated statistics for talent: ${request.talentProfileId}")

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
}
