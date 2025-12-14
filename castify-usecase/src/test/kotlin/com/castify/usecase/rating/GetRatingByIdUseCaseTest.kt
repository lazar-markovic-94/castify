package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class GetRatingByIdUseCaseTest {

    private lateinit var ratingRepository: TalentRatingRepository
    private lateinit var getRatingByIdUseCase: GetRatingByIdUseCase

    @BeforeEach
    fun setUp() {
        ratingRepository = mockk()
        getRatingByIdUseCase = GetRatingByIdUseCase(ratingRepository)
    }

    @Test
    fun `should successfully get rating by id`() {
        // Given
        val ratingId = UUID.randomUUID()

        val rating = TalentRating(
            ratingId = ratingId,
            talentProfileId = UUID.randomUUID(),
            clientUserId = UUID.randomUUID(),
            collaborationId = null,
            overallRating = 5,
            professionalismRating = 5,
            qualityRating = 5,
            communicationRating = 5,
            timelinessRating = 5,
            reviewText = "Great work!",
            wouldWorkAgain = true,
            createdAt = Instant.now(),
            isVerified = true
        )

        every { ratingRepository.findById(ratingId) } returns rating

        // When
        val result = getRatingByIdUseCase.execute(ratingId)

        // Then
        assertNotNull(result)
        assertEquals(ratingId, result.ratingId)
        assertEquals(5, result.overallRating)
        assertEquals("Great work!", result.reviewText)

        verify(exactly = 1) { ratingRepository.findById(ratingId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when rating not found`() {
        // Given
        val ratingId = UUID.randomUUID()

        every { ratingRepository.findById(ratingId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getRatingByIdUseCase.execute(ratingId)
        }

        assertTrue(exception.message!!.contains("Rating not found"))
        verify(exactly = 1) { ratingRepository.findById(ratingId) }
    }
}
