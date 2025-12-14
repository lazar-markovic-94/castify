package com.castify.usecase.rating

import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import com.castify.usecase.dto.GetRatingsRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class GetTalentRatingsUseCaseTest {

    private lateinit var ratingRepository: TalentRatingRepository
    private lateinit var getTalentRatingsUseCase: GetTalentRatingsUseCase

    @BeforeEach
    fun setUp() {
        ratingRepository = mockk()
        getTalentRatingsUseCase = GetTalentRatingsUseCase(ratingRepository)
    }

    @Test
    fun `should successfully get ratings with pagination`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val request = GetRatingsRequest(
            talentProfileId = talentProfileId,
            page = 0,
            size = 10
        )

        val ratings = listOf(
            TalentRating(
                ratingId = UUID.randomUUID(),
                talentProfileId = talentProfileId,
                clientUserId = UUID.randomUUID(),
                collaborationId = null,
                overallRating = 5,
                professionalismRating = 5,
                qualityRating = 5,
                communicationRating = 5,
                timelinessRating = 5,
                reviewText = "Excellent!",
                wouldWorkAgain = true,
                createdAt = Instant.now(),
                isVerified = true
            )
        )

        every { ratingRepository.findByTalentProfileIdPaginated(talentProfileId, 0, 10) } returns ratings
        every { ratingRepository.countByTalentProfileId(talentProfileId) } returns 1

        // When
        val result = getTalentRatingsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.ratings.size)
        assertEquals(0, result.page)
        assertEquals(10, result.size)
        assertEquals(1, result.totalRatings)

        verify(exactly = 1) { ratingRepository.findByTalentProfileIdPaginated(talentProfileId, 0, 10) }
        verify(exactly = 1) { ratingRepository.countByTalentProfileId(talentProfileId) }
    }

    @Test
    fun `should handle empty ratings list`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val request = GetRatingsRequest(
            talentProfileId = talentProfileId,
            page = 0,
            size = 10
        )

        every { ratingRepository.findByTalentProfileIdPaginated(talentProfileId, 0, 10) } returns emptyList()
        every { ratingRepository.countByTalentProfileId(talentProfileId) } returns 0

        // When
        val result = getTalentRatingsUseCase.execute(request)

        // Then
        assertNotNull(result)
        assertTrue(result.ratings.isEmpty())
        assertEquals(0, result.totalRatings)
    }

    @Test
    fun `should calculate correct total pages`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val request = GetRatingsRequest(
            talentProfileId = talentProfileId,
            page = 0,
            size = 10
        )

        every { ratingRepository.findByTalentProfileIdPaginated(talentProfileId, 0, 10) } returns emptyList()
        every { ratingRepository.countByTalentProfileId(talentProfileId) } returns 25

        // When
        val result = getTalentRatingsUseCase.execute(request)

        // Then
        assertEquals(25, result.totalRatings)
        assertEquals(3, result.totalPages) // 25 / 10 = 3 pages
    }
}
