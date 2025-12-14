package com.castify.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@RestController
@RequestMapping("/api/upload")
@Tag(name = "File Upload", description = "File upload endpoints")
class FileUploadController(
    @Value("\${file.upload.dir:uploads}")
    private val uploadDir: String
) {

    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    @PostMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload an image file")
    fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        logger.info("=".repeat(80))
        logger.info("FILE UPLOAD REQUEST STARTED")
        logger.info("=".repeat(80))
        try {
            logger.info("Upload request received - filename: ${file.originalFilename}, size: ${file.size}, contentType: ${file.contentType}")
            logger.info("Upload directory configured as: $uploadDir")
            logger.info("Working directory: ${System.getProperty("user.dir")}")
            logger.info("uploadDir value: '$uploadDir' (length: ${uploadDir.length})")

            // Validate file
            if (file.isEmpty) {
                logger.warn("Upload failed: File is empty")
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "File is empty",
                    "details" to "The uploaded file contains no data"
                ))
            }

            // Validate file type
            val contentType = file.contentType
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("Upload failed: Invalid content type - $contentType")
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "Only image files are allowed",
                    "details" to "Content type '$contentType' is not supported. Only image/* types are allowed."
                ))
            }

            // Validate file size (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                logger.warn("Upload failed: File too large - ${file.size} bytes")
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "File size exceeds 5MB limit",
                    "details" to "File size is ${file.size / 1024 / 1024}MB, maximum allowed is 5MB"
                ))
            }

            // Create upload directory if it doesn't exist
            val uploadPath = try {
                Paths.get(uploadDir).toAbsolutePath()
            } catch (e: Exception) {
                logger.error("Failed to resolve upload path: $uploadDir", e)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Invalid upload directory configuration",
                    "details" to "Could not resolve path '$uploadDir': ${e.javaClass.simpleName} - ${e.message}",
                    "path" to uploadDir
                ))
            }

            logger.info("Upload path resolved to: $uploadPath")

            try {
                if (!Files.exists(uploadPath)) {
                    logger.info("Upload directory does not exist, creating...")
                    Files.createDirectories(uploadPath)
                    logger.info("Upload directory created successfully at: $uploadPath")
                } else {
                    logger.info("Upload directory already exists at: $uploadPath")
                }
            } catch (e: java.nio.file.AccessDeniedException) {
                logger.error("Access denied when creating upload directory: $uploadPath", e)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Permission denied",
                    "details" to "Cannot create or access upload directory. Check file system permissions.",
                    "path" to uploadPath.toString(),
                    "exception" to e.javaClass.simpleName
                ))
            } catch (e: java.io.IOException) {
                logger.error("IO error when creating upload directory: $uploadPath", e)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Failed to create upload directory",
                    "details" to "IO error: ${e.message}",
                    "path" to uploadPath.toString(),
                    "exception" to e.javaClass.simpleName
                ))
            }

            // Generate unique filename
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast(".", "jpg")
            val uniqueFilename = "${UUID.randomUUID()}.$extension"
            val filePath = uploadPath.resolve(uniqueFilename)

            logger.info("Saving file to: $filePath")

            // Save file
            try {
                file.inputStream.use { inputStream ->
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
                }
                logger.info("File saved successfully to: $filePath")
            } catch (e: java.nio.file.AccessDeniedException) {
                logger.error("Access denied when saving file: $filePath", e)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Permission denied",
                    "details" to "Cannot write file to upload directory. Check file system permissions.",
                    "path" to filePath.toString(),
                    "exception" to e.javaClass.simpleName
                ))
            } catch (e: java.io.IOException) {
                logger.error("IO error when saving file: $filePath", e)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                    "error" to "Failed to save file",
                    "details" to "IO error while writing file: ${e.message}",
                    "path" to filePath.toString(),
                    "exception" to e.javaClass.simpleName
                ))
            }

            // Return URL
            val fileUrl = "/api/upload/images/$uniqueFilename"
            logger.info("Upload successful. Returning file URL: $fileUrl")
            return ResponseEntity.ok(mapOf("url" to fileUrl))

        } catch (e: IllegalStateException) {
            logger.error("Illegal state during upload", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "error" to "Upload service not properly initialized",
                "details" to (e.message ?: "Unknown state error"),
                "exception" to e.javaClass.simpleName,
                "stackTrace" to e.stackTrace.take(5).joinToString("\n") { "  at $it" }
            ))
        } catch (e: Exception) {
            logger.error("=".repeat(80))
            logger.error("UNEXPECTED ERROR DURING FILE UPLOAD", e)
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
            logger.info("FILE UPLOAD REQUEST ENDED")
            logger.info("=".repeat(80))
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Map<String, String>> {
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
    @Operation(summary = "Serve uploaded image")
    fun serveImage(@PathVariable filename: String): ResponseEntity<ByteArray> {
        try {
            val filePath = Paths.get(uploadDir).resolve(filename)

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build()
            }

            val bytes = Files.readAllBytes(filePath)
            val contentType = Files.probeContentType(filePath) ?: "application/octet-stream"

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes)

        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
