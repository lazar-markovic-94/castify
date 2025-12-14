package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRatingRepository
import com.castify.usecase.dto.RatingResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for getting a single rating by ID.
 */
@Service
class GetRatingByIdUseCase(
    private val ratingRepository: TalentRatingRepository
) {
    private val logger = LoggerFactory.getLogger(GetRatingByIdUseCase::class.java)

    /**
     * Retrieves a rating by its ID.
     *
     * @param ratingId The rating ID
     * @return The rating response
     * @throws ResourceNotFoundException if the rating is not found
     */
    fun execute(ratingId: UUID): RatingResponse {
        return try {
            logger.info("Fetching rating: $ratingId")

            val rating = ratingRepository.findById(ratingId)
                ?: throw ResourceNotFoundException("Rating not found: $ratingId")

            RatingResponse.fromDomain(rating)

        } catch (e: ResourceNotFoundException) {
            logger.error("Rating not found: $ratingId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching rating: $ratingId", e)
            throw RuntimeException("Failed to fetch rating: ${e.message}", e)
        }
    }
}
