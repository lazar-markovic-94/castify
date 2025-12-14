package com.castify.usecase.dto

import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaType
import com.castify.domain.media.PortfolioItem
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.Instant
import java.util.*

data class CreateMediaAssetRequest(
    val mediaType: MediaType?,
    val contentType: ContentType?,
    @field:NotBlank(message = "File URL is required")
    val fileUrl: String?,
    val thumbnailUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val durationSeconds: Int? = null,
    @field:Positive(message = "File size must be positive")
    val fileSizeBytes: Long?
)

data class MediaAssetResponse(
    val mediaAssetId: UUID,
    val uploadedByUserId: UUID,
    val mediaType: MediaType,
    val contentType: ContentType,
    val fileUrl: String,
    val thumbnailUrl: String?,
    val title: String?,
    val description: String?,
    val durationSeconds: Int?,
    val fileSizeBytes: Long,
    val uploadedAt: Instant
) {
    companion object {
        fun fromDomain(mediaAsset: MediaAsset): MediaAssetResponse = MediaAssetResponse(
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

data class AddToPortfolioRequest(
    val mediaAssetId: UUID?,
    val displayOrder: Int = 0
)

data class PortfolioItemResponse(
    val portfolioItemId: UUID,
    val talentProfileId: UUID,
    val mediaAssetId: UUID,
    val displayOrder: Int,
    val addedAt: Instant
) {
    companion object {
        fun fromDomain(portfolioItem: PortfolioItem): PortfolioItemResponse = PortfolioItemResponse(
            portfolioItemId = portfolioItem.portfolioItemId,
            talentProfileId = portfolioItem.talentProfileId,
            mediaAssetId = portfolioItem.mediaAssetId,
            displayOrder = portfolioItem.displayOrder,
            addedAt = portfolioItem.addedAt
        )
    }
}
