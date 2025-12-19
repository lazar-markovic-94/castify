package com.castify.usecase.media

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.PortfolioItem
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.AddToPortfolioRequest
import com.castify.usecase.dto.PortfolioItemResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for adding a media asset to a talent's portfolio.
 */
@Service
@Transactional
class AddToPortfolioUseCase(
    private val mediaRepository: MediaRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(AddToPortfolioUseCase::class.java)

    /**
     * Adds a media asset to the talent's portfolio.
     *
     * @param userId The talent user ID
     * @param request The add to portfolio request
     * @return Portfolio item response
     * @throws ResourceNotFoundException if talent profile or media asset is not found
     * @throws UnauthorizedException if user tries to add someone else's media asset
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(userId: UUID, request: AddToPortfolioRequest): PortfolioItemResponse {
        return try {
            logger.info("Adding media asset to portfolio for user: $userId")

            val talentProfile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val mediaAssetId = requireNotNull(request.mediaAssetId) { "Media asset ID is required" }

            val mediaAsset = mediaRepository.findMediaAssetById(mediaAssetId)
                ?: throw ResourceNotFoundException("Media asset not found: $mediaAssetId")

            if (mediaAsset.uploadedByUserId != userId) {
                throw UnauthorizedException("You can only add your own media assets to your portfolio")
            }

            val portfolioItem = PortfolioItem(
                portfolioItemId = UUID.randomUUID(),
                talentProfileId = talentProfile.talentProfileId,
                mediaAssetId = mediaAssetId,
                displayOrder = request.displayOrder
            )

            val savedItem = mediaRepository.savePortfolioItem(portfolioItem)
            logger.info("Successfully added media asset to portfolio: ${savedItem.portfolioItemId}")

            PortfolioItemResponse.fromDomain(savedItem)

        } catch (e: ResourceNotFoundException) {
            logger.error("Resource not found when adding to portfolio for user: $userId", e)
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized attempt to add media asset to portfolio by user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid portfolio data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error adding media asset to portfolio for user: $userId", e)
            throw RuntimeException("Failed to add media asset to portfolio: ${e.message}", e)
        }
    }
}
