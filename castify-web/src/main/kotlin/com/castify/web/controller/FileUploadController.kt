package com.castify.web.controller

import com.castify.domain.media.ContentType
import com.castify.usecase.upload.FileUploadException
import com.castify.usecase.upload.UploadProfileImageUseCase
import com.castify.usecase.upload.UploadIntroductionVideoUseCase
import com.castify.usecase.upload.UploadPortfolioMediaUseCase
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@RestController
@RequestMapping("/api/upload")
@Tag(name = "File Upload", description = "File upload endpoints")
class FileUploadController(
    @Value("\${file.upload.dir:uploads}")
    private val uploadDir: String,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val uploadIntroductionVideoUseCase: UploadIntroductionVideoUseCase,
    private val uploadPortfolioMediaUseCase: UploadPortfolioMediaUseCase
) {

    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    @PostMapping("/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload a profile image")
    fun uploadProfileImage(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        logger.info("=".repeat(80))
        logger.info("PROFILE IMAGE UPLOAD REQUEST STARTED")
        logger.info("=".repeat(80))
        try {
            val userId = SecurityUtils.getCurrentUserId()
            logger.info("Profile image upload request received for user: $userId - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")

            val fileUrl = uploadProfileImageUseCase.execute(userId, file)

            logger.info("Profile image upload successful. Returning file URL: $fileUrl")
            return ResponseEntity.ok(mapOf("url" to fileUrl))

        } catch (e: FileUploadException) {
            logger.warn("Profile image upload failed: ${e.error} - ${e.details}")
            return ResponseEntity.badRequest().body(mapOf(
                "error" to e.error,
                "details" to e.details
            ))
        } catch (e: Exception) {
            logger.error("=".repeat(80))
            logger.error("UNEXPECTED ERROR DURING PROFILE IMAGE UPLOAD", e)
            logger.error("Exception type: ${e.javaClass.name}")
            logger.error("Exception message: ${e.message}")
            logger.error("Stack trace:", e)
            logger.error("=".repeat(80))
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "error" to "Unexpected error during upload",
                "details" to (e.message ?: "No error message available"),
                "exception" to e.javaClass.simpleName,
                "stackTrace" to e.stackTrace.take(10).joinToString("\n") { "  at $it" }
            ))
        } finally {
            logger.info("=".repeat(80))
            logger.info("PROFILE IMAGE UPLOAD REQUEST ENDED")
            logger.info("=".repeat(80))
        }
    }

    @PostMapping("/introduction-video", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload an introduction video")
    fun uploadIntroductionVideo(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        logger.info("=".repeat(80))
        logger.info("INTRODUCTION VIDEO UPLOAD REQUEST STARTED")
        logger.info("=".repeat(80))
        try {
            val userId = SecurityUtils.getCurrentUserId()
            logger.info("Introduction video upload request received for user: $userId - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")

            val fileUrl = uploadIntroductionVideoUseCase.execute(userId, file)

            logger.info("Introduction video upload successful. Returning file URL: $fileUrl")
            return ResponseEntity.ok(mapOf("url" to fileUrl))

        } catch (e: FileUploadException) {
            logger.warn("Introduction video upload failed: ${e.error} - ${e.details}")
            return ResponseEntity.badRequest().body(mapOf(
                "error" to e.error,
                "details" to e.details
            ))
        } catch (e: Exception) {
            logger.error("Error uploading introduction video", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "error" to "Unexpected error during video upload",
                "details" to (e.message ?: "No error message available")
            ))
        } finally {
            logger.info("=".repeat(80))
            logger.info("INTRODUCTION VIDEO UPLOAD REQUEST ENDED")
            logger.info("=".repeat(80))
        }
    }

    @PostMapping("/portfolio-media", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload portfolio media (image, video, or audio)")
    fun uploadPortfolioMedia(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false, defaultValue = "PORTFOLIO_ITEM") contentType: ContentType,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<Any> {
        logger.info("=".repeat(80))
        logger.info("PORTFOLIO MEDIA UPLOAD REQUEST STARTED")
        logger.info("=".repeat(80))
        try {
            val userId = SecurityUtils.getCurrentUserId()
            logger.info("Portfolio media upload request received for user: $userId - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")

            val mediaAsset = uploadPortfolioMediaUseCase.execute(userId, file, contentType, title, description)

            logger.info("Portfolio media upload successful. Media asset ID: ${mediaAsset.mediaAssetId}")
            return ResponseEntity.ok(mediaAsset)

        } catch (e: FileUploadException) {
            logger.warn("Portfolio media upload failed: ${e.error} - ${e.details}")
            return ResponseEntity.badRequest().body(mapOf(
                "error" to e.error,
                "details" to e.details
            ))
        } catch (e: Exception) {
            logger.error("Error uploading portfolio media", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "error" to "Unexpected error during portfolio media upload",
                "details" to (e.message ?: "No error message available")
            ))
        } finally {
            logger.info("=".repeat(80))
            logger.info("PORTFOLIO MEDIA UPLOAD REQUEST ENDED")
            logger.info("=".repeat(80))
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, String>>? {
        // Ignore client disconnection errors (broken pipe, connection reset, etc.)
        // These are harmless and occur when the browser cancels/closes the connection
        val message = e.message ?: ""
        val exceptionName = e.javaClass.simpleName

        if (exceptionName.contains("ClientAbort") ||
            exceptionName.contains("AsyncRequestNotUsable") ||
            message.contains("Broken pipe") ||
            message.contains("Connection reset")) {
            logger.debug("Client disconnected during file transfer (this is normal): $exceptionName")
            return null // Don't send response, client already disconnected
        }

        logger.error("=".repeat(80))
        logger.error("CONTROLLER EXCEPTION HANDLER TRIGGERED", e)
        logger.error("Exception type: ${e.javaClass.name}")
        logger.error("Exception message: ${e.message}")
        logger.error("=".repeat(80))
        e.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "error" to "Controller exception",
            "details" to (e.message ?: "No details"),
            "type" to e.javaClass.simpleName
        ))
    }

    @GetMapping("/images/{filename}")
    @Operation(summary = "Serve uploaded image (for profile images)")
    fun serveImage(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return serveFile(filename, "images", "application/octet-stream")
    }

    @GetMapping("/videos/{filename}")
    @Operation(summary = "Serve uploaded video (for introduction videos)")
    fun serveVideo(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return serveFile(filename, "videos", "video/mp4")
    }

    @GetMapping("/portfolio/images/{filename}")
    @Operation(summary = "Serve portfolio image")
    fun servePortfolioImage(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return serveFile(filename, "portfolio/images", "application/octet-stream")
    }

    @GetMapping("/portfolio/videos/{filename}")
    @Operation(summary = "Serve portfolio video")
    fun servePortfolioVideo(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return serveFile(filename, "portfolio/videos", "video/mp4")
    }

    @GetMapping("/portfolio/audio/{filename}")
    @Operation(summary = "Serve portfolio audio")
    fun servePortfolioAudio(@PathVariable filename: String): ResponseEntity<ByteArray> {
        return serveFile(filename, "portfolio/audio", "audio/mpeg")
    }

    private fun serveFile(filename: String, subdirectory: String, defaultContentType: String): ResponseEntity<ByteArray> {
        try {
            val filePath = Paths.get(uploadDir, subdirectory).resolve(filename)

            if (!Files.exists(filePath)) {
                logger.warn("File not found: $filePath")
                return ResponseEntity.notFound().build()
            }

            val bytes = Files.readAllBytes(filePath)
            val contentType = Files.probeContentType(filePath) ?: defaultContentType

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes)

        } catch (e: Exception) {
            logger.error("Error serving file: $filename from $subdirectory", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
