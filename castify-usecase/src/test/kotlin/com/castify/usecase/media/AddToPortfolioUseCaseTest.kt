package com.castify.usecase.media

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.MediaType
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.TestHelpers
import com.castify.usecase.dto.AddToPortfolioRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class AddToPortfolioUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var addToPortfolioUseCase: AddToPortfolioUseCase

    @BeforeEach
    fun setUp() {
        mediaRepository = mockk()
        talentRepository = mockk()
        addToPortfolioUseCase = AddToPortfolioUseCase(mediaRepository, talentRepository)
    }

    @Test
    fun `should successfully add media asset to portfolio`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val mediaAssetId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val mediaAsset = MediaAsset(
            mediaAssetId = mediaAssetId,
            uploadedByUserId = userId,
            mediaType = MediaType.VIDEO,
            contentType = ContentType.DEMO_REEL,
            fileUrl = "https://example.com/video.mp4",
            thumbnailUrl = null,
            title = "Demo Reel",
            description = null,
            durationSeconds = 120,
            fileSizeBytes = 15000000L,
            uploadedAt = Instant.now()
        )

        val request = AddToPortfolioRequest(
            mediaAssetId = mediaAssetId,
            displayOrder = 1
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findMediaAssetById(mediaAssetId) } returns mediaAsset
        every { mediaRepository.savePortfolioItem(any()) } answers { firstArg() }

        // When
        val result = addToPortfolioUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(talentProfileId, result.talentProfileId)
        assertEquals(mediaAssetId, result.mediaAssetId)
        assertEquals(1, result.displayOrder)

        verify(exactly = 1) { mediaRepository.savePortfolioItem(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when talent profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val mediaAssetId = UUID.randomUUID()

        val request = AddToPortfolioRequest(
            mediaAssetId = mediaAssetId,
            displayOrder = 1
        )

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            addToPortfolioUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 0) { mediaRepository.savePortfolioItem(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when media asset not found`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val mediaAssetId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val request = AddToPortfolioRequest(
            mediaAssetId = mediaAssetId,
            displayOrder = 1
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findMediaAssetById(mediaAssetId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            addToPortfolioUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Media asset not found"))
        verify(exactly = 0) { mediaRepository.savePortfolioItem(any()) }
    }

    @Test
    fun `should throw UnauthorizedException when adding someone else's media asset`() {
        // Given
        val userId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val mediaAssetId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val mediaAsset = MediaAsset(
            mediaAssetId = mediaAssetId,
            uploadedByUserId = otherUserId,
            mediaType = MediaType.VIDEO,
            contentType = ContentType.DEMO_REEL,
            fileUrl = "https://example.com/video.mp4",
            thumbnailUrl = null,
            title = "Demo Reel",
            description = null,
            durationSeconds = 120,
            fileSizeBytes = 15000000L,
            uploadedAt = Instant.now()
        )

        val request = AddToPortfolioRequest(
            mediaAssetId = mediaAssetId,
            displayOrder = 1
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findMediaAssetById(mediaAssetId) } returns mediaAsset

        // When & Then
        val exception = assertThrows<UnauthorizedException> {
            addToPortfolioUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("You can only add your own media assets"))
        verify(exactly = 0) { mediaRepository.savePortfolioItem(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when mediaAssetId is null`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val request = AddToPortfolioRequest(
            mediaAssetId = null,
            displayOrder = 1
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile

        // When & Then
        assertThrows<IllegalArgumentException> {
            addToPortfolioUseCase.execute(userId, request)
        }

        verify(exactly = 0) { mediaRepository.savePortfolioItem(any()) }
    }
}
