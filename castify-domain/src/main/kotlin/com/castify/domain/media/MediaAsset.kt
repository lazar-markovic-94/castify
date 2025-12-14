package com.castify.domain.media

import java.time.Instant
import java.util.*

data class MediaAsset(
    val mediaAssetId: UUID,
    val uploadedByUserId: UUID,
    val mediaType: MediaType,
    val contentType: ContentType,
    val fileUrl: String,
    val thumbnailUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val durationSeconds: Int? = null,
    val fileSizeBytes: Long,
    val uploadedAt: Instant = Instant.now()
) {
    init {
        require(fileUrl.isNotBlank()) { "File URL cannot be blank" }
        require(fileSizeBytes > 0) { "File size must be positive" }
        if (durationSeconds != null) {
            require(durationSeconds > 0) { "Duration must be positive" }
        }
    }

    fun updateTitle(newTitle: String?): MediaAsset = copy(title = newTitle)

    fun updateDescription(newDescription: String?): MediaAsset = copy(description = newDescription)

    fun isVideo(): Boolean = mediaType == MediaType.VIDEO

    fun isImage(): Boolean = mediaType == MediaType.IMAGE

    fun isAudio(): Boolean = mediaType == MediaType.AUDIO
}

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO
}

enum class ContentType {
    HEADSHOT,
    PORTFOLIO_ITEM,
    DEMO_REEL,
    VOICE_SAMPLE,
    OTHER
}

data class PortfolioItem(
    val portfolioItemId: UUID,
    val talentProfileId: UUID,
    val mediaAssetId: UUID,
    val displayOrder: Int = 0,
    val addedAt: Instant = Instant.now()
) {
    init {
        require(displayOrder >= 0) { "Display order must be non-negative" }
    }

    fun updateDisplayOrder(newOrder: Int): PortfolioItem {
        require(newOrder >= 0) { "Display order must be non-negative" }
        return copy(displayOrder = newOrder)
    }
}
