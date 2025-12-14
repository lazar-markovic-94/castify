package com.castify.persistence.entity

import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaType
import com.castify.domain.media.PortfolioItem
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "media_asset")
class MediaAssetEntity(
    @Id
    @Column(name = "media_asset_id", columnDefinition = "UUID")
    var mediaAssetId: UUID = UUID.randomUUID(),

    @Column(name = "uploaded_by_user_id", nullable = false, columnDefinition = "UUID")
    var uploadedByUserId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    var mediaType: MediaType,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    var contentType: ContentType,

    @Column(name = "file_url", nullable = false)
    var fileUrl: String,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "duration_seconds")
    var durationSeconds: Int? = null,

    @Column(name = "file_size_bytes", nullable = false)
    var fileSizeBytes: Long,

    @Column(name = "uploaded_at", nullable = false)
    var uploadedAt: Instant = Instant.now()
) {
    fun toDomain(): MediaAsset = MediaAsset(
        mediaAssetId = mediaAssetId,
        uploadedByUserId = uploadedByUserId,
        mediaType = mediaType,
        contentType = contentType,
        fileUrl = fileUrl,
        thumbnailUrl = thumbnailUrl,
        title = title,
        description = description,
        durationSeconds = durationSeconds,
        fileSizeBytes = fileSizeBytes,
        uploadedAt = uploadedAt
    )

    companion object {
        fun fromDomain(mediaAsset: MediaAsset): MediaAssetEntity = MediaAssetEntity(
            mediaAssetId = mediaAsset.mediaAssetId,
            uploadedByUserId = mediaAsset.uploadedByUserId,
            mediaType = mediaAsset.mediaType,
            contentType = mediaAsset.contentType,
            fileUrl = mediaAsset.fileUrl,
            thumbnailUrl = mediaAsset.thumbnailUrl,
            title = mediaAsset.title,
            description = mediaAsset.description,
            durationSeconds = mediaAsset.durationSeconds,
            fileSizeBytes = mediaAsset.fileSizeBytes,
            uploadedAt = mediaAsset.uploadedAt
        )
    }
}

@Entity
@Table(name = "talent_portfolio_item")
class PortfolioItemEntity(
    @Id
    @Column(name = "portfolio_item_id", columnDefinition = "UUID")
    var portfolioItemId: UUID = UUID.randomUUID(),

    @Column(name = "talent_profile_id", nullable = false, columnDefinition = "UUID")
    var talentProfileId: UUID,

    @Column(name = "media_asset_id", nullable = false, columnDefinition = "UUID")
    var mediaAssetId: UUID,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name = "added_at", nullable = false)
    var addedAt: Instant = Instant.now()
) {
    fun toDomain(): PortfolioItem = PortfolioItem(
        portfolioItemId = portfolioItemId,
        talentProfileId = talentProfileId,
        mediaAssetId = mediaAssetId,
        displayOrder = displayOrder,
        addedAt = addedAt
    )

    companion object {
        fun fromDomain(portfolioItem: PortfolioItem): PortfolioItemEntity = PortfolioItemEntity(
            portfolioItemId = portfolioItem.portfolioItemId,
            talentProfileId = portfolioItem.talentProfileId,
            mediaAssetId = portfolioItem.mediaAssetId,
            displayOrder = portfolioItem.displayOrder,
            addedAt = portfolioItem.addedAt
        )
    }
}
