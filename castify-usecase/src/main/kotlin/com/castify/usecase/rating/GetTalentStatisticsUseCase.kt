package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.StatisticsResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for getting talent statistics.
 */
@Service
class GetTalentStatisticsUseCase(
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(GetTalentStatisticsUseCase::class.java)

    /**
     * Retrieves statistics for a talent.
     *
     * @param talentProfileId The talent profile ID
     * @return The statistics response
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun execute(talentProfileId: UUID): StatisticsResponse {
        return try {
            logger.info("Fetching statistics for talent: $talentProfileId")

            // Verify talent exists
            talentRepository.findById(talentProfileId)
                ?: throw ResourceNotFoundException("Talent profile not found: $talentProfileId")

            val statistics = statisticsRepository.findByTalentProfileId(talentProfileId)
                ?: TalentStatistics.createInitial(talentProfileId, 0)

            StatisticsResponse.fromDomain(statistics)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: $talentProfileId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching statistics for talent: $talentProfileId", e)
            throw RuntimeException("Failed to fetch statistics: ${e.message}", e)
        }
    }
}
