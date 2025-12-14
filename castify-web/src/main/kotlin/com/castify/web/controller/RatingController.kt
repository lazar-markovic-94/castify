package com.castify.web.controller

import com.castify.usecase.dto.*
import com.castify.usecase.rating.*
import com.castify.web.security.SecurityUtils
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for talent rating and statistics operations.
 *
 * Endpoints:
 * - POST /api/ratings - Create a new rating (CLIENT only)
 * - GET /api/ratings/{ratingId} - Get a specific rating
 * - GET /api/ratings/talent/{talentProfileId} - Get ratings for a talent
 * - GET /api/statistics/{talentProfileId} - Get statistics for a talent
 * - GET /api/leaderboard - Get leaderboard data
 */
@RestController
@RequestMapping("/api")
class RatingController(
    private val createRatingUseCase: CreateRatingUseCase,
    private val getTalentRatingsUseCase: GetTalentRatingsUseCase,
    private val getRatingByIdUseCase: GetRatingByIdUseCase,
    private val getTalentStatisticsUseCase: GetTalentStatisticsUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) {

    /**
     * Create a new rating for a talent
     * Only clients can create ratings
     */
    @PostMapping("/ratings")
    @PreAuthorize("hasRole('CLIENT')")
    fun createRating(
        @Valid @RequestBody request: CreateRatingRequest
    ): ResponseEntity<RatingResponse> {
        val clientUserId = SecurityUtils.getCurrentUserId()
        val rating = createRatingUseCase.execute(clientUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(rating)
    }

    /**
     * Get a specific rating by ID
     */
    @GetMapping("/ratings/{ratingId}")
    fun getRatingById(
        @PathVariable ratingId: UUID
    ): ResponseEntity<RatingResponse> {
        val rating = getRatingByIdUseCase.execute(ratingId)
        return ResponseEntity.ok(rating)
    }

    /**
     * Get paginated ratings for a talent profile
     */
    @GetMapping("/ratings/talent/{talentProfileId}")
    fun getTalentRatings(
        @PathVariable talentProfileId: UUID,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) size: Int
    ): ResponseEntity<RatingsPageResponse> {
        val request = GetRatingsRequest(
            talentProfileId = talentProfileId,
            page = page,
            size = size
        )
        val ratings = getTalentRatingsUseCase.execute(request)
        return ResponseEntity.ok(ratings)
    }

    /**
     * Get statistics for a talent profile
     */
    @GetMapping("/statistics/{talentProfileId}")
    fun getTalentStatistics(
        @PathVariable talentProfileId: UUID
    ): ResponseEntity<StatisticsResponse> {
        val statistics = getTalentStatisticsUseCase.execute(talentProfileId)
        return ResponseEntity.ok(statistics)
    }

    /**
     * Get leaderboard data (top rated and most successful talents)
     */
    @GetMapping("/leaderboard")
    fun getLeaderboard(
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) limit: Int
    ): ResponseEntity<LeaderboardResponse> {
        val leaderboard = getLeaderboardUseCase.execute(limit)
        return ResponseEntity.ok(leaderboard)
    }
}
