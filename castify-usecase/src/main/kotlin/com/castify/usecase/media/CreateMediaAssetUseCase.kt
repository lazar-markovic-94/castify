package com.castify.usecase.media

import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.usecase.dto.CreateMediaAssetRequest
import com.castify.usecase.dto.MediaAssetResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating a new media asset.
 */
@Service
@Transactional
class CreateMediaAssetUseCase(
    private val mediaRepository: MediaRepository
) {
    private val logger = LoggerFactory.getLogger(CreateMediaAssetUseCase::class.java)

    /**
     * Creates a new media asset.
     *
     * @param userId The user ID uploading the media
     * @param request The media asset creation request
     * @return Media asset response
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(userId: UUID, request: CreateMediaAssetRequest): MediaAssetResponse {
        return try {
            logger.info("Creating media asset for user: $userId")

            val mediaType = requireNotNull(request.mediaType) { "Media type is required" }
            val contentType = requireNotNull(request.contentType) { "Content type is required" }
            val fileUrl = requireNotNull(request.fileUrl) { "File URL is required" }
            val fileSizeBytes = requireNotNull(request.fileSizeBytes) { "File size is required" }

            val mediaAsset = MediaAsset(
                mediaAssetId = UUID.randomUUID(),
                uploadedByUserId = userId,
                mediaType = mediaType,
                contentType = contentType,
                fileUrl = fileUrl,
                thumbnailUrl = request.thumbnailUrl,
                title = request.title,
                description = request.description,
                durationSeconds = request.durationSeconds,
                fileSizeBytes = fileSizeBytes
            )

            val savedAsset = mediaRepository.saveMediaAsset(mediaAsset)
            logger.info("Successfully created media asset: ${savedAsset.mediaAssetId}")

            MediaAssetResponse.fromDomain(savedAsset)

        } catch (e: IllegalArgumentException) {
            logger.error("Invalid media asset data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error creating media asset for user: $userId", e)
            throw RuntimeException("Failed to create media asset: ${e.message}", e)
        }
    }
}
