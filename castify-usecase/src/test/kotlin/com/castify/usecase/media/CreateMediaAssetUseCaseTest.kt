package com.castify.usecase.media

import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.MediaType
import com.castify.usecase.dto.CreateMediaAssetRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class CreateMediaAssetUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var createMediaAssetUseCase: CreateMediaAssetUseCase

    @BeforeEach
    fun setUp() {
        mediaRepository = mockk()
        createMediaAssetUseCase = CreateMediaAssetUseCase(mediaRepository)
    }

    @Test
    fun `should successfully create video media asset`() {
        // Given
        val userId = UUID.randomUUID()

        val request = CreateMediaAssetRequest(
            mediaType = MediaType.VIDEO,
            contentType = ContentType.DEMO_REEL,
            fileUrl = "https://example.com/video.mp4",
            thumbnailUrl = "https://example.com/thumb.jpg",
            title = "My Demo Reel",
            description = "Acting demo reel 2024",
            durationSeconds = 120,
            fileSizeBytes = 15000000L
        )

        every { mediaRepository.saveMediaAsset(any()) } answers { firstArg() }

        // When
        val result = createMediaAssetUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(MediaType.VIDEO, result.mediaType)
        assertEquals("My Demo Reel", result.title)
        assertEquals(120, result.durationSeconds)

        verify(exactly = 1) { mediaRepository.saveMediaAsset(any()) }
    }

    @Test
    fun `should successfully create image media asset`() {
        // Given
        val userId = UUID.randomUUID()

        val request = CreateMediaAssetRequest(
            mediaType = MediaType.IMAGE,
            contentType = ContentType.HEADSHOT,
            fileUrl = "https://example.com/photo.jpg",
            thumbnailUrl = null,
            title = "Headshot",
            description = "Professional headshot",
            durationSeconds = null,
            fileSizeBytes = 2500000L
        )

        every { mediaRepository.saveMediaAsset(any()) } answers { firstArg() }

        // When
        val result = createMediaAssetUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(MediaType.IMAGE, result.mediaType)
        assertEquals("Headshot", result.title)
        assertNull(result.durationSeconds)

        verify(exactly = 1) { mediaRepository.saveMediaAsset(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when mediaType is null`() {
        // Given
        val userId = UUID.randomUUID()

        val request = CreateMediaAssetRequest(
            mediaType = null,
            contentType = ContentType.DEMO_REEL,
            fileUrl = "https://example.com/video.mp4",
            thumbnailUrl = null,
            title = null,
            description = null,
            durationSeconds = null,
            fileSizeBytes = 1000000L
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            createMediaAssetUseCase.execute(userId, request)
        }

        verify(exactly = 0) { mediaRepository.saveMediaAsset(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when fileUrl is null`() {
        // Given
        val userId = UUID.randomUUID()

        val request = CreateMediaAssetRequest(
            mediaType = MediaType.VIDEO,
            contentType = ContentType.DEMO_REEL,
            fileUrl = null,
            thumbnailUrl = null,
            title = null,
            description = null,
            durationSeconds = null,
            fileSizeBytes = 1000000L
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            createMediaAssetUseCase.execute(userId, request)
        }

        verify(exactly = 0) { mediaRepository.saveMediaAsset(any()) }
    }

    @Test
    fun `should successfully create audio media asset`() {
        // Given
        val userId = UUID.randomUUID()

        val request = CreateMediaAssetRequest(
            mediaType = MediaType.AUDIO,
            contentType = ContentType.VOICE_SAMPLE,
            fileUrl = "https://example.com/voice-sample.mp3",
            thumbnailUrl = null,
            title = "Voice Over Sample",
            description = "Commercial voice over",
            durationSeconds = 30,
            fileSizeBytes = 500000L
        )

        every { mediaRepository.saveMediaAsset(any()) } answers { firstArg() }

        // When
        val result = createMediaAssetUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(MediaType.AUDIO, result.mediaType)
        assertEquals(30, result.durationSeconds)
    }
}
