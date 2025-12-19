package com.castify.usecase.media

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.media.MediaRepository
import com.castify.domain.talent.TalentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for removing a media asset from a talent's portfolio.
 */
@Service
@Transactional
class RemoveFromPortfolioUseCase(
    private val mediaRepository: MediaRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(RemoveFromPortfolioUseCase::class.java)

    /**
     * Removes a media asset from the talent's portfolio.
     *
     * @param userId The talent user ID
     * @param portfolioItemId The portfolio item ID to remove
     * @throws ResourceNotFoundException if talent profile or portfolio item is not found
     */
    fun execute(userId: UUID, portfolioItemId: UUID) {
        return try {
            logger.info("Removing portfolio item: $portfolioItemId for user: $userId")

            val talentProfile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            // Verify ownership before deleting
            val portfolioItems = mediaRepository.findPortfolioItemsByTalent(talentProfile.talentProfileId)
            val itemToDelete = portfolioItems.find { it.portfolioItemId == portfolioItemId }
                ?: throw ResourceNotFoundException("Portfolio item not found: $portfolioItemId")

            mediaRepository.deletePortfolioItem(portfolioItemId)
            logger.info("Successfully removed portfolio item: $portfolioItemId")

        } catch (e: ResourceNotFoundException) {
            logger.error("Resource not found when removing portfolio item: $portfolioItemId for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error removing portfolio item: $portfolioItemId for user: $userId", e)
            throw RuntimeException("Failed to remove portfolio item: ${e.message}", e)
        }
    }
}
