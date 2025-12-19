package com.castify.usecase.media

import com.castify.domain.media.MediaRepository
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.MediaAssetResponse
import com.castify.usecase.dto.PortfolioItemWithMediaResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class GetPortfolioItemsUseCase(
    private val mediaRepository: MediaRepository,
    private val talentRepository: TalentRepository
) {
    fun execute(talentProfileId: UUID): List<PortfolioItemWithMediaResponse> {
        // Verify talent profile exists
        talentRepository.findById(talentProfileId)
            ?: throw IllegalArgumentException("Talent profile not found")

        val portfolioItems = mediaRepository.findPortfolioItemsByTalent(talentProfileId)

        // Get all media assets for the portfolio items
        return portfolioItems.mapNotNull { portfolioItem ->
            val mediaAsset = mediaRepository.findMediaAssetById(portfolioItem.mediaAssetId)
            mediaAsset?.let {
                PortfolioItemWithMediaResponse(
                    portfolioItemId = portfolioItem.portfolioItemId,
                    talentProfileId = portfolioItem.talentProfileId,
                    mediaAsset = MediaAssetResponse.fromDomain(it),
                    displayOrder = portfolioItem.displayOrder,
                    addedAt = portfolioItem.addedAt
                )
            }
        }.sortedBy { it.displayOrder }
    }
}
