package com.castify.usecase.upload

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
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
 * Use case for uploading a talent's profile image.
 * Handles file validation, storage, and updates the talent profile with the image URL.
 */
@Service
@Transactional
class UploadProfileImageUseCase(
    @Value("\${file.upload.dir:uploads}")
    private val uploadDir: String,
    private val talentRepository: TalentRepository,
    private val asyncStatisticsUpdateService: AsyncStatisticsUpdateService
) {
    private val logger = LoggerFactory.getLogger(UploadProfileImageUseCase::class.java)

    /**
     * Uploads a profile image for a talent.
     *
     * @param userId The user ID of the talent
     * @param file The image file to upload
     * @return The relative URL of the uploaded image
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws FileUploadException if upload validation or save fails
     */
    fun execute(userId: UUID, file: MultipartFile): String {
        logger.info("Uploading profile image for user: $userId - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")

        // Validate talent exists
        val talentProfile = talentRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

        // Validate file
        validateFile(file)

        // Validate file type
        val contentType = file.contentType
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("Invalid content type for profile image: $contentType")
            throw FileUploadException(
                "Only image files are allowed",
                "Content type '$contentType' is not supported. Only image/* types are allowed."
            )
        }

        // Validate file size (max 5MB)
        val maxSize = 5 * 1024 * 1024 // 5MB
        if (file.size > maxSize) {
            logger.warn("Profile image file too large: ${file.size} bytes")
            throw FileUploadException(
                "File size exceeds 5MB limit",
                "File size is ${file.size / 1024 / 1024}MB, maximum allowed is 5MB"
            )
        }

        // Save file to storage
        val fileUrl = saveFile(file, "images")

        // Update talent profile with image URL
        val updatedProfile = try {
            val profile = talentProfile.copy(profileImageUrl = fileUrl)
            val saved = talentRepository.save(profile)
            logger.info("Successfully updated profile image URL for talent: ${talentProfile.talentProfileId}")
            saved
        } catch (e: Exception) {
            logger.error("Failed to update talent profile with image URL", e)
            throw FileUploadException(
                "Failed to update profile",
                "Image was uploaded but profile update failed: ${e.message}"
            )
        }

        // Update profile completeness statistics in a separate transaction
        // This is called on a separate service to ensure proper Spring AOP proxy interception
        asyncStatisticsUpdateService.updateProfileCompleteness(updatedProfile.talentProfileId)

        return fileUrl
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

    private fun saveFile(file: MultipartFile, subdirectory: String): String {
        try {
            // Create upload directory with subdirectory
            val uploadPath = Paths.get(uploadDir, subdirectory).toAbsolutePath()
            logger.info("Upload path resolved to: $uploadPath")

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath)
                logger.info("Upload directory created at: $uploadPath")
            }

            // Generate unique filename
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast(".", "jpg")
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
}
