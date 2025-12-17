package com.castify.usecase.collaboration

import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.usecase.dto.CollaborationResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for responding to collaboration proposals.
 * Allows talents to accept or decline proposals from clients.
 */
@Service
@Transactional
class RespondToCollaborationUseCase(
    private val collaborationRepository: CollaborationRepository
) {
    private val logger = LoggerFactory.getLogger(RespondToCollaborationUseCase::class.java)

    /**
     * Accept a collaboration proposal.
     *
     * @param collaborationId The ID of the collaboration to accept
     * @return The updated collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun accept(collaborationId: UUID): CollaborationResponse {
        return try {
            logger.info("Accepting collaboration: $collaborationId")

            val collaboration = collaborationRepository.findById(collaborationId)
                ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

            val acceptedCollaboration = collaboration.accept()
            val savedCollaboration = collaborationRepository.save(acceptedCollaboration)

            logger.info("Successfully accepted collaboration: $collaborationId")
            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Collaboration not found: $collaborationId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot accept collaboration in current status: $collaborationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error accepting collaboration: $collaborationId", e)
            throw RuntimeException("Failed to accept collaboration: ${e.message}", e)
        }
    }

    /**
     * Decline a collaboration proposal.
     *
     * @param collaborationId The ID of the collaboration to decline
     * @return The updated collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun decline(collaborationId: UUID): CollaborationResponse {
        return try {
            logger.info("Declining collaboration: $collaborationId")

            val collaboration = collaborationRepository.findById(collaborationId)
                ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

            val declinedCollaboration = collaboration.decline()
            val savedCollaboration = collaborationRepository.save(declinedCollaboration)

            logger.info("Successfully declined collaboration: $collaborationId")
            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Collaboration not found: $collaborationId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot decline collaboration in current status: $collaborationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error declining collaboration: $collaborationId", e)
            throw RuntimeException("Failed to decline collaboration: ${e.message}", e)
        }
    }
}
