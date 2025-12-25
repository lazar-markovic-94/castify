package com.castify.domain.media

import java.util.*

interface MediaRepository {
    fun saveMediaAsset(mediaAsset: MediaAsset): MediaAsset
    fun findMediaAssetById(mediaAssetId: UUID): MediaAsset?

    fun savePortfolioItem(portfolioItem: PortfolioItem): PortfolioItem
    fun findPortfolioItemsByTalent(talentProfileId: UUID): List<PortfolioItem>
    fun deletePortfolioItem(portfolioItemId: UUID)
}
