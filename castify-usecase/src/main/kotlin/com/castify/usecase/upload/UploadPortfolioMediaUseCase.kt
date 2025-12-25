package com.castify.usecase.upload

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.MediaType
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.MediaAssetResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Use case for uploading portfolio media (images, videos, audio).
 * Handles file validation, storage, and creates a MediaAsset entity.
 */
@Service
@Transactional
class UploadPortfolioMediaUseCase(
    @Value("\${file.upload.dir:uploads}")
    private val uploadDir: String,
    private val talentRepository: TalentRepository,
    private val mediaRepository: MediaRepository
) {
    private val logger = LoggerFactory.getLogger(UploadPortfolioMediaUseCase::class.java)

    /**
     * Uploads a portfolio media file (image, video, or audio).
     *
     * @param userId The user ID of the talent
     * @param file The media file to upload
     * @param contentType The content type (PORTFOLIO_ITEM, DEMO_REEL, VOICE_SAMPLE, etc.)
     * @param title Optional title for the media
     * @param description Optional description for the media
     * @return MediaAssetResponse containing the created media asset details
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws FileUploadException if upload validation or save fails
     */
    fun execute(
        userId: UUID,
        file: MultipartFile,
        contentType: ContentType = ContentType.PORTFOLIO_ITEM,
        title: String? = null,
        description: String? = null
    ): MediaAssetResponse {
        logger.info("Uploading portfolio media for user: $userId - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")

        // Validate talent exists
        val talentProfile = talentRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

        // Validate file
        validateFile(file)

        // Determine media type and validate
        val (mediaType, subdirectory) = determineMediaType(file)
        validateMediaFile(file, mediaType)

        // Save file to storage
        val fileUrl = saveFile(file, subdirectory)

        // Create media asset entity
        try {
            val mediaAsset = MediaAsset(
                mediaAssetId = UUID.randomUUID(),
                uploadedByUserId = userId,
                mediaType = mediaType,
                contentType = contentType,
                fileUrl = fileUrl,
                thumbnailUrl = null, // Can be generated separately
                title = title,
                description = description,
                durationSeconds = null, // Can be set separately for videos/audio
                fileSizeBytes = file.size
            )

            val savedAsset = mediaRepository.saveMediaAsset(mediaAsset)
            logger.info("Successfully created media asset: ${savedAsset.mediaAssetId} for talent: ${talentProfile.talentProfileId}")

            return MediaAssetResponse.fromDomain(savedAsset)

        } catch (e: Exception) {
            logger.error("Failed to create media asset for portfolio", e)
            throw FileUploadException(
                "Failed to create media asset",
                "File was uploaded but media asset creation failed: ${e.message}"
            )
        }
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            logger.warn("Upload failed: File is empty")
            throw FileUploadException(
                "File is empty",
                "The uploaded file contains no data"
            )
        }
    }

    private fun determineMediaType(file: MultipartFile): Pair<MediaType, String> {
        val mimeType = file.contentType ?: throw FileUploadException(
            "Invalid file",
            "Cannot determine file type (missing content type)"
        )

        return when {
            mimeType.startsWith("image/") -> MediaType.IMAGE to "portfolio/images"
            mimeType.startsWith("video/") -> MediaType.VIDEO to "portfolio/videos"
            mimeType.startsWith("audio/") -> MediaType.AUDIO to "portfolio/audio"
            else -> throw FileUploadException(
                "Unsupported media type",
                "Content type '$mimeType' is not supported. Only image/*, video/*, and audio/* are allowed."
            )
        }
    }

    private fun validateMediaFile(file: MultipartFile, mediaType: MediaType) {
        when (mediaType) {
            MediaType.IMAGE -> {
                // Max 10MB for portfolio images
                val maxSize = 10 * 1024 * 1024
                if (file.size > maxSize) {
                    throw FileUploadException(
                        "File size exceeds 10MB limit",
                        "Image file size is ${file.size / 1024 / 1024}MB, maximum allowed is 10MB"
                    )
                }
            }
            MediaType.VIDEO -> {
                // Max 100MB for portfolio videos (larger than intro videos)
                val maxSize = 100 * 1024 * 1024
                if (file.size > maxSize) {
                    throw FileUploadException(
                        "File size exceeds 100MB limit",
                        "Video file size is ${file.size / 1024 / 1024}MB, maximum allowed is 100MB"
                    )
                }
            }
            MediaType.AUDIO -> {
                // Max 50MB for audio files
                val maxSize = 50 * 1024 * 1024
                if (file.size > maxSize) {
                    throw FileUploadException(
                        "File size exceeds 50MB limit",
                        "Audio file size is ${file.size / 1024 / 1024}MB, maximum allowed is 50MB"
                    )
                }
            }
        }
    }

    private fun saveFile(file: MultipartFile, subdirectory: String): String {
        try {
            // Create upload directory if it doesn't exist
            val uploadPath = Paths.get(uploadDir, subdirectory).toAbsolutePath()
            logger.info("Upload path resolved to: $uploadPath")

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath)
                logger.info("Upload directory created at: $uploadPath")
            }

            // Generate unique filename
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast(".", getDefaultExtension(file.contentType))
            val uniqueFilename = "${UUID.randomUUID()}.$extension"
            val filePath = uploadPath.resolve(uniqueFilename)

            logger.info("Saving file to: $filePath")

            // Save file
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            logger.info("File saved successfully to: $filePath")

            // Return relative URL
            val fileUrl = "/api/upload/$subdirectory/$uniqueFilename"
            logger.info("Upload successful. Returning file URL: $fileUrl")
            return fileUrl

        } catch (e: java.nio.file.AccessDeniedException) {
            logger.error("Access denied when saving file", e)
            throw FileUploadException(
                "Permission denied",
                "Cannot write file to upload directory. Check file system permissions."
            )
        } catch (e: java.io.IOException) {
            logger.error("IO error when saving file", e)
            throw FileUploadException(
                "Failed to save file",
                "IO error while writing file: ${e.message}"
            )
        } catch (e: Exception) {
            logger.error("Unexpected error during file save", e)
            throw FileUploadException(
                "Unexpected error during upload",
                e.message ?: "No error message available"
            )
        }
    }

    private fun getDefaultExtension(contentType: String?): String {
        return when {
            contentType == null -> "bin"
            contentType.startsWith("image/") -> "jpg"
            contentType.startsWith("video/") -> "mp4"
            contentType.startsWith("audio/") -> "mp3"
            else -> "bin"
        }
    }
}
