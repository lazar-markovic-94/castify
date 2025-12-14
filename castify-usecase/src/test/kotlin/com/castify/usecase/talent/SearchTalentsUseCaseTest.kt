package com.castify.usecase.talent

import com.castify.domain.talent.*
import com.castify.usecase.dto.TalentSearchRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class SearchTalentsUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var profileEnricher: TalentProfileEnricher
    private lateinit var searchTalentsUseCase: SearchTalentsUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        profileEnricher = mockk(relaxed = true)
        searchTalentsUseCase = SearchTalentsUseCase(talentRepository, profileEnricher)
    }

    @Test
    fun `should successfully search talents with basic pagination`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10
        )

        val profiles = listOf(
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "John Actor",
                bio = "Actor",
                location = Location(mode = LocationMode.REMOTE),
                profileImageUrl = null
            ),
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "Jane Singer",
                bio = "Singer",
                location = Location(mode = LocationMode.REMOTE),
                profileImageUrl = null
            )
        )

        val pagedProfiles = PagedTalentResult(
            profiles = profiles,
            totalElements = 2
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(profiles) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(0, result.page)
        assertEquals(10, result.size)
        assertEquals(2, result.totalElements)

        verify(exactly = 1) { talentRepository.searchPaginated(any(), 0, 10) }
    }

    @Test
    fun `should search with capabilities filter`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10,
            canDoActing = true,
            canDoSinging = true
        )

        val profiles = listOf(
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "Multi-talented Artist",
                bio = "Actor and Singer",
                location = Location(mode = LocationMode.REMOTE),
                profileImageUrl = null,
                capabilities = TalentCapabilities(
                    canDoActing = true,
                    canDoSinging = true
                )
            )
        )

        val pagedProfiles = PagedTalentResult(
            profiles = profiles,
            totalElements = 1
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(profiles) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.totalElements)

        verify(exactly = 1) { talentRepository.searchPaginated(any(), 0, 10) }
    }

    @Test
    fun `should search with location filter`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10,
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            radiusKm = 50.0
        )

        val profiles = listOf(
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "Local Artist",
                bio = "Based in LA",
                location = Location(
                    mode = LocationMode.SPECIFIC,
                    latitude = BigDecimal("34.0522"),
                    longitude = BigDecimal("-118.2437"),
                    city = "Los Angeles",
                    country = "USA"
                ),
                profileImageUrl = null
            )
        )

        val pagedProfiles = PagedTalentResult(
            profiles = profiles,
            totalElements = 1
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(profiles) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.totalElements)

        verify(exactly = 1) { talentRepository.searchPaginated(any(), 0, 10) }
    }

    @Test
    fun `should search with tags filter`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10,
            tags = setOf("Actor", "Comedian")
        )

        val profiles = listOf(
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "Comic Actor",
                bio = "Actor and Comedian",
                location = Location(mode = LocationMode.REMOTE),
                profileImageUrl = null,
                tags = setOf("Actor", "Comedian")
            )
        )

        val pagedProfiles = PagedTalentResult(
            profiles = profiles,
            totalElements = 1
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(profiles) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.totalElements)
    }

    @Test
    fun `should handle empty search results`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10,
            tags = setOf("NonexistentTag")
        )

        val pagedProfiles = PagedTalentResult(
            profiles = emptyList(),
            totalElements = 0
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(emptyList()) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `should calculate correct total pages`() {
        // Given
        val request = TalentSearchRequest(
            page = 0,
            size = 10
        )

        val pagedProfiles = PagedTalentResult(
            profiles = emptyList(),
            totalElements = 25
        )

        every { talentRepository.searchPaginated(any(), 0, 10) } returns pagedProfiles
        every { profileEnricher.enrichProfiles(emptyList()) } returns emptyList()

        // When
        val result = searchTalentsUseCase.execute(request)

        // Then
        assertEquals(25, result.totalElements)
        assertEquals(3, result.totalPages) // 25 items / 10 per page = 3 pages
    }
}
