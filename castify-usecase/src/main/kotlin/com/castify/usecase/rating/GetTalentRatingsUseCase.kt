package com.castify.usecase.rating

import com.castify.domain.rating.TalentRatingRepository
import com.castify.usecase.dto.GetRatingsRequest
import com.castify.usecase.dto.RatingResponse
import com.castify.usecase.dto.RatingsPageResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for getting paginated ratings for a talent.
 */
@Service
class GetTalentRatingsUseCase(
    private val ratingRepository: TalentRatingRepository
) {
    private val logger = LoggerFactory.getLogger(GetTalentRatingsUseCase::class.java)

    /**
     * Retrieves paginated ratings for a specific talent.
     *
     * @param request The pagination request
     * @return Paginated ratings response
     */
    fun execute(request: GetRatingsRequest): RatingsPageResponse {
        return try {
            logger.info("Fetching ratings for talent: ${request.talentProfileId}, page: ${request.page}")

            val ratings = ratingRepository.findByTalentProfileIdPaginated(
                request.talentProfileId,
                request.page,
                request.size
            )

            val totalRatings = ratingRepository.countByTalentProfileId(request.talentProfileId)
            val totalPages = ((totalRatings + request.size - 1) / request.size).toInt()

            logger.info("Found $totalRatings ratings for talent: ${request.talentProfileId}")

            RatingsPageResponse(
                ratings = ratings.map { RatingResponse.fromDomain(it) },
                totalRatings = totalRatings,
                page = request.page,
                size = request.size,
                totalPages = totalPages
            )

        } catch (e: Exception) {
            logger.error("Error fetching ratings for talent: ${request.talentProfileId}", e)
            throw RuntimeException("Failed to fetch ratings: ${e.message}", e)
        }
    }
}
