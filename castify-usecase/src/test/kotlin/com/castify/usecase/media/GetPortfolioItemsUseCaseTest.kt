package com.castify.usecase.media

import com.castify.domain.media.ContentType
import com.castify.domain.media.MediaAsset
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.MediaType
import com.castify.domain.media.PortfolioItem
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class GetPortfolioItemsUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var getPortfolioItemsUseCase: GetPortfolioItemsUseCase

    @BeforeEach
    fun setUp() {
        mediaRepository = mockk()
        talentRepository = mockk()
        getPortfolioItemsUseCase = GetPortfolioItemsUseCase(mediaRepository, talentRepository)
    }

    @Test
    fun `should return portfolio items sorted by display order`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val mockTalentProfile: TalentProfile = mockk()

        val mediaAsset1 = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.IMAGE,
            contentType = ContentType.PORTFOLIO_ITEM,
            fileUrl = "https://example.com/image1.jpg",
            thumbnailUrl = "https://example.com/thumb1.jpg",
            title = "Headshot 1",
            description = "Professional headshot",
            durationSeconds = null,
            fileSizeBytes = 1024000
        )

        val mediaAsset2 = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.VIDEO,
            contentType = ContentType.DEMO_REEL,
            fileUrl = "https://example.com/video1.mp4",
            thumbnailUrl = "https://example.com/thumb2.jpg",
            title = "Demo Reel",
            description = "Acting demo reel",
            durationSeconds = 120,
            fileSizeBytes = 5024000
        )

        val portfolioItem1 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset1.mediaAssetId,
            displayOrder = 2,
            addedAt = Instant.now()
        )

        val portfolioItem2 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset2.mediaAssetId,
            displayOrder = 1,
            addedAt = Instant.now()
        )

        every { talentRepository.findById(talentProfileId) } returns mockTalentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns listOf(
            portfolioItem1,
            portfolioItem2
        )
        every { mediaRepository.findMediaAssetById(mediaAsset1.mediaAssetId) } returns mediaAsset1
        every { mediaRepository.findMediaAssetById(mediaAsset2.mediaAssetId) } returns mediaAsset2

        // When
        val result = getPortfolioItemsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        // Should be sorted by display order
        assertEquals(1, result[0].displayOrder)
        assertEquals(2, result[1].displayOrder)
        assertEquals(mediaAsset2.mediaAssetId, result[0].mediaAsset.mediaAssetId)
        assertEquals(mediaAsset1.mediaAssetId, result[1].mediaAsset.mediaAssetId)

        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
        verify(exactly = 1) { mediaRepository.findPortfolioItemsByTalent(talentProfileId) }
        verify(exactly = 1) { mediaRepository.findMediaAssetById(mediaAsset1.mediaAssetId) }
        verify(exactly = 1) { mediaRepository.findMediaAssetById(mediaAsset2.mediaAssetId) }
    }

    @Test
    fun `should return empty list when talent has no portfolio items`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val mockTalentProfile: TalentProfile = mockk()

        every { talentRepository.findById(talentProfileId) } returns mockTalentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns emptyList()

        // When
        val result = getPortfolioItemsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())

        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
        verify(exactly = 1) { mediaRepository.findPortfolioItemsByTalent(talentProfileId) }
    }

    @Test
    fun `should throw IllegalArgumentException when talent profile not found`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        every { talentRepository.findById(talentProfileId) } returns null

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            getPortfolioItemsUseCase.execute(talentProfileId)
        }

        assertEquals("Talent profile not found", exception.message)
        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
        verify(exactly = 0) { mediaRepository.findPortfolioItemsByTalent(any()) }
    }

    @Test
    fun `should filter out portfolio items with missing media assets`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val mockTalentProfile: TalentProfile = mockk()

        val mediaAsset1 = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.IMAGE,
            contentType = ContentType.PORTFOLIO_ITEM,
            fileUrl = "https://example.com/image1.jpg",
            fileSizeBytes = 1024000
        )

        val portfolioItem1 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset1.mediaAssetId,
            displayOrder = 1
        )

        val portfolioItem2 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = UUID.randomUUID(), // This media asset doesn't exist
            displayOrder = 2
        )

        every { talentRepository.findById(talentProfileId) } returns mockTalentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns listOf(
            portfolioItem1,
            portfolioItem2
        )
        every { mediaRepository.findMediaAssetById(mediaAsset1.mediaAssetId) } returns mediaAsset1
        every { mediaRepository.findMediaAssetById(portfolioItem2.mediaAssetId) } returns null

        // When
        val result = getPortfolioItemsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(mediaAsset1.mediaAssetId, result[0].mediaAsset.mediaAssetId)

        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
        verify(exactly = 1) { mediaRepository.findPortfolioItemsByTalent(talentProfileId) }
    }

    @Test
    fun `should include all media asset details in response`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val mockTalentProfile: TalentProfile = mockk()

        val mediaAsset = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.AUDIO,
            contentType = ContentType.VOICE_SAMPLE,
            fileUrl = "https://example.com/audio.mp3",
            thumbnailUrl = "https://example.com/audio-thumb.jpg",
            title = "Voice Sample",
            description = "Commercial voice sample",
            durationSeconds = 60,
            fileSizeBytes = 2048000
        )

        val portfolioItem = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset.mediaAssetId,
            displayOrder = 1
        )

        every { talentRepository.findById(talentProfileId) } returns mockTalentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns listOf(portfolioItem)
        every { mediaRepository.findMediaAssetById(mediaAsset.mediaAssetId) } returns mediaAsset

        // When
        val result = getPortfolioItemsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)

        val response = result[0]
        assertEquals(portfolioItem.portfolioItemId, response.portfolioItemId)
        assertEquals(talentProfileId, response.talentProfileId)
        assertEquals(1, response.displayOrder)

        val mediaResponse = response.mediaAsset
        assertEquals(mediaAsset.mediaAssetId, mediaResponse.mediaAssetId)
        assertEquals(MediaType.AUDIO, mediaResponse.mediaType)
        assertEquals(ContentType.VOICE_SAMPLE, mediaResponse.contentType)
        assertEquals("https://example.com/audio.mp3", mediaResponse.fileUrl)
        assertEquals("https://example.com/audio-thumb.jpg", mediaResponse.thumbnailUrl)
        assertEquals("Voice Sample", mediaResponse.title)
        assertEquals("Commercial voice sample", mediaResponse.description)
        assertEquals(60, mediaResponse.durationSeconds)
        assertEquals(2048000, mediaResponse.fileSizeBytes)
    }

    @Test
    fun `should handle portfolio items with same display order`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val mockTalentProfile: TalentProfile = mockk()

        val mediaAsset1 = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.IMAGE,
            contentType = ContentType.PORTFOLIO_ITEM,
            fileUrl = "https://example.com/image1.jpg",
            fileSizeBytes = 1024000
        )

        val mediaAsset2 = MediaAsset(
            mediaAssetId = UUID.randomUUID(),
            uploadedByUserId = UUID.randomUUID(),
            mediaType = MediaType.IMAGE,
            contentType = ContentType.PORTFOLIO_ITEM,
            fileUrl = "https://example.com/image2.jpg",
            fileSizeBytes = 1024000
        )

        val portfolioItem1 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset1.mediaAssetId,
            displayOrder = 1
        )

        val portfolioItem2 = PortfolioItem(
            portfolioItemId = UUID.randomUUID(),
            talentProfileId = talentProfileId,
            mediaAssetId = mediaAsset2.mediaAssetId,
            displayOrder = 1 // Same display order
        )

        every { talentRepository.findById(talentProfileId) } returns mockTalentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns listOf(
            portfolioItem1,
            portfolioItem2
        )
        every { mediaRepository.findMediaAssetById(mediaAsset1.mediaAssetId) } returns mediaAsset1
        every { mediaRepository.findMediaAssetById(mediaAsset2.mediaAssetId) } returns mediaAsset2

        // When
        val result = getPortfolioItemsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        // Both should have the same display order
        assertEquals(1, result[0].displayOrder)
        assertEquals(1, result[1].displayOrder)
    }
}
