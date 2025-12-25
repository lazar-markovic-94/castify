package com.castify.usecase.collaboration

import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.usecase.dto.CollaborationResponse
import com.castify.usecase.rating.TalentStatisticsService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating collaboration status.
 * Handles status transitions like start work, complete, and cancel.
 */
@Service
@Transactional
class UpdateCollaborationStatusUseCase(
    private val collaborationRepository: CollaborationRepository,
    private val statisticsService: TalentStatisticsService
) {
    private val logger = LoggerFactory.getLogger(UpdateCollaborationStatusUseCase::class.java)

    /**
     * Start work on an accepted collaboration.
     *
     * @param collaborationId The ID of the collaboration to start
     * @return The updated collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun startWork(collaborationId: UUID): CollaborationResponse {
        return try {
            logger.info("Starting work on collaboration: $collaborationId")

            val collaboration = collaborationRepository.findById(collaborationId)
                ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

            val updated = collaboration.startWork()
            val savedCollaboration = collaborationRepository.save(updated)

            logger.info("Successfully started work on collaboration: $collaborationId")
            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Collaboration not found: $collaborationId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot start work on collaboration in current status: $collaborationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error starting work on collaboration: $collaborationId", e)
            throw RuntimeException("Failed to start work: ${e.message}", e)
        }
    }

    /**
     * Complete an in-progress collaboration.
     *
     * @param collaborationId The ID of the collaboration to complete
     * @return The updated collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun complete(collaborationId: UUID): CollaborationResponse {
        return try {
            logger.info("Completing collaboration: $collaborationId")

            val collaboration = collaborationRepository.findById(collaborationId)
                ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

            val completed = collaboration.complete()
            val savedCollaboration = collaborationRepository.save(completed)

            // Update statistics in application layer (replaces database trigger)
            statisticsService.recalculateCollaborationStatistics(savedCollaboration.talentProfileId)

            logger.info("Successfully completed collaboration: $collaborationId")
            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Collaboration not found: $collaborationId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot complete collaboration in current status: $collaborationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error completing collaboration: $collaborationId", e)
            throw RuntimeException("Failed to complete collaboration: ${e.message}", e)
        }
    }

    /**
     * Cancel a collaboration.
     *
     * @param collaborationId The ID of the collaboration to cancel
     * @return The updated collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun cancel(collaborationId: UUID): CollaborationResponse {
        return try {
            logger.info("Canceling collaboration: $collaborationId")

            val collaboration = collaborationRepository.findById(collaborationId)
                ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

            val canceled = collaboration.cancel()
            val savedCollaboration = collaborationRepository.save(canceled)

            // Update statistics in application layer (replaces database trigger)
            statisticsService.recalculateCollaborationStatistics(savedCollaboration.talentProfileId)

            logger.info("Successfully canceled collaboration: $collaborationId")
            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Collaboration not found: $collaborationId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot cancel collaboration in current status: $collaborationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error canceling collaboration: $collaborationId", e)
            throw RuntimeException("Failed to cancel collaboration: ${e.message}", e)
        }
    }
}
