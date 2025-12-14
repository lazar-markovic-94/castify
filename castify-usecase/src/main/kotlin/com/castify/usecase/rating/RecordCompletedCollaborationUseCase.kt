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
 * Use case for recording a completed collaboration.
 */
@Service
@Transactional
class RecordCompletedCollaborationUseCase(
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(RecordCompletedCollaborationUseCase::class.java)

    /**
     * Records a completed collaboration and updates statistics.
     *
     * @param talentProfileId The talent profile ID
     * @param earnings The earnings from this collaboration
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun execute(talentProfileId: UUID, earnings: Double) {
        try {
            logger.info("Recording completed collaboration for talent: $talentProfileId with earnings: $earnings")

            // Verify talent exists
            talentRepository.findById(talentProfileId)
                ?: throw ResourceNotFoundException("Talent profile not found: $talentProfileId")

            val statistics = statisticsRepository.findByTalentProfileId(talentProfileId)
                ?: TalentStatistics.createInitial(talentProfileId, 0)

            val updatedStats = TalentStatistics.updateWithCompletedCollaboration(statistics, earnings)
            statisticsRepository.save(updatedStats)

            logger.info("Successfully recorded completed collaboration for talent: $talentProfileId")

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: $talentProfileId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error recording completed collaboration for talent: $talentProfileId", e)
            throw RuntimeException("Failed to record completed collaboration: ${e.message}", e)
        }
    }
}
