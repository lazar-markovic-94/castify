package com.castify.persistence.adapter

import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.PortfolioItem
import com.castify.persistence.entity.MediaAssetEntity
import com.castify.persistence.entity.PortfolioItemEntity
import com.castify.persistence.jpa.MediaAssetJpaRepository
import com.castify.persistence.jpa.PortfolioItemJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class MediaRepositoryAdapter(
    private val mediaAssetJpaRepository: MediaAssetJpaRepository,
    private val portfolioItemJpaRepository: PortfolioItemJpaRepository
) : MediaRepository {

    override fun saveMediaAsset(mediaAsset: MediaAsset): MediaAsset {
        val entity = MediaAssetEntity.fromDomain(mediaAsset)
        val saved = mediaAssetJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findMediaAssetById(mediaAssetId: UUID): MediaAsset? {
        return mediaAssetJpaRepository.findById(mediaAssetId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun savePortfolioItem(portfolioItem: PortfolioItem): PortfolioItem {
        val entity = PortfolioItemEntity.fromDomain(portfolioItem)
        val saved = portfolioItemJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findPortfolioItemsByTalent(talentProfileId: UUID): List<PortfolioItem> {
        return portfolioItemJpaRepository.findByTalentProfileId(talentProfileId)
            .map { it.toDomain() }
    }

    override fun deletePortfolioItem(portfolioItemId: UUID) {
        portfolioItemJpaRepository.deleteById(portfolioItemId)
    }
}
