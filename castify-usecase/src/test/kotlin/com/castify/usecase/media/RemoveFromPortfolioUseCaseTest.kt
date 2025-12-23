package com.castify.usecase.media

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.media.MediaRepository
import com.castify.domain.media.PortfolioItem
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.TestHelpers
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import java.util.*

class RemoveFromPortfolioUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var removeFromPortfolioUseCase: RemoveFromPortfolioUseCase

    @BeforeEach
    fun setUp() {
        mediaRepository = mockk()
        talentRepository = mockk()
        removeFromPortfolioUseCase = RemoveFromPortfolioUseCase(mediaRepository, talentRepository)
    }

    @Test
    fun `should successfully remove portfolio item`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val portfolioItemId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val portfolioItems = listOf(
            PortfolioItem(
                portfolioItemId = portfolioItemId,
                talentProfileId = talentProfileId,
                mediaAssetId = UUID.randomUUID(),
                displayOrder = 1
            )
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns portfolioItems
        every { mediaRepository.deletePortfolioItem(portfolioItemId) } returns Unit

        // When
        removeFromPortfolioUseCase.execute(userId, portfolioItemId)

        // Then
        verify(exactly = 1) { mediaRepository.deletePortfolioItem(portfolioItemId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when talent profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val portfolioItemId = UUID.randomUUID()

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            removeFromPortfolioUseCase.execute(userId, portfolioItemId)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 0) { mediaRepository.deletePortfolioItem(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when portfolio item not found`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val portfolioItemId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val portfolioItems = listOf(
            PortfolioItem(
                portfolioItemId = UUID.randomUUID(),
                talentProfileId = talentProfileId,
                mediaAssetId = UUID.randomUUID(),
                displayOrder = 1
            )
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns portfolioItems

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            removeFromPortfolioUseCase.execute(userId, portfolioItemId)
        }

        assertTrue(exception.message!!.contains("Portfolio item not found"))
        verify(exactly = 0) { mediaRepository.deletePortfolioItem(any()) }
    }

    @Test
    fun `should handle empty portfolio`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()
        val portfolioItemId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        every { talentRepository.findByUserId(userId) } returns talentProfile
        every { mediaRepository.findPortfolioItemsByTalent(talentProfileId) } returns emptyList()

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            removeFromPortfolioUseCase.execute(userId, portfolioItemId)
        }

        assertTrue(exception.message!!.contains("Portfolio item not found"))
        verify(exactly = 0) { mediaRepository.deletePortfolioItem(any()) }
    }
}
