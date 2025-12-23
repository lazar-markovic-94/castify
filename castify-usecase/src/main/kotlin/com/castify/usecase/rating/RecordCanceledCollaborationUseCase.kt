package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for recording a canceled collaboration.
 */
@Service
@Transactional
class RecordCanceledCollaborationUseCase(
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(RecordCanceledCollaborationUseCase::class.java)

    /**
     * Records a canceled collaboration and updates statistics.
     *
     * @param talentProfileId The talent profile ID
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun execute(talentProfileId: UUID) {
        try {
            logger.info("Recording canceled collaboration for talent: $talentProfileId")

            // Verify talent exists
            if (!talentRepository.existsById(talentProfileId)) {
                throw ResourceNotFoundException("Talent profile not found: $talentProfileId")
            }

            val statistics = statisticsRepository.findByTalentProfileId(talentProfileId)
                ?: TalentStatistics.createInitial(talentProfileId, 0)

            val updatedStats = TalentStatistics.updateWithCanceledCollaboration(statistics)
            statisticsRepository.save(updatedStats)

            logger.info("Successfully recorded canceled collaboration for talent: $talentProfileId")

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: $talentProfileId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error recording canceled collaboration for talent: $talentProfileId", e)
            throw RuntimeException("Failed to record canceled collaboration: ${e.message}", e)
        }
    }
}
