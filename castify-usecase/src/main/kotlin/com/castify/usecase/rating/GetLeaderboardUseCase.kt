package com.castify.usecase.rating

import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.usecase.dto.LeaderboardEntryResponse
import com.castify.usecase.dto.LeaderboardResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Use case for getting leaderboard data (top rated and most successful talents).
 */
@Service
class GetLeaderboardUseCase(
    private val statisticsRepository: TalentStatisticsRepository
) {
    private val logger = LoggerFactory.getLogger(GetLeaderboardUseCase::class.java)

    /**
     * Retrieves leaderboard data.
     *
     * @param limit The number of entries to return for each category
     * @return The leaderboard response
     */
    fun execute(limit: Int = 10): LeaderboardResponse {
        return try {
            logger.info("Fetching leaderboard data with limit: $limit")

            val topRated = statisticsRepository.findTopRated(limit)
            val mostSuccessful = statisticsRepository.findMostSuccessful(limit)

            logger.info("Retrieved ${topRated.size} top rated and ${mostSuccessful.size} most successful talents")

            LeaderboardResponse(
                topRated = topRated.map {
                    LeaderboardEntryResponse(
                        talentProfileId = it.talentProfileId,
                        averageRating = it.averageRating,
                        completedCollaborations = it.completedCollaborations,
                        tier = it.tier
                    )
                },
                mostSuccessful = mostSuccessful.map {
                    LeaderboardEntryResponse(
                        talentProfileId = it.talentProfileId,
                        averageRating = it.averageRating,
                        completedCollaborations = it.completedCollaborations,
                        tier = it.tier
                    )
                }
            )

        } catch (e: Exception) {
            logger.error("Error fetching leaderboard data", e)
            throw RuntimeException("Failed to fetch leaderboard: ${e.message}", e)
        }
    }
}
