package com.castify.usecase.upload

/**
 * Custom exception for file upload errors.
 */
class FileUploadException(
    val error: String,
    val details: String
) : RuntimeException("$error: $details")
