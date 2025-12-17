package com.castify.usecase.collaboration

import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.collaboration.CollaborationStatus
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.usecase.dto.CollaborationResponse
import com.castify.usecase.dto.CollaborationsResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for retrieving collaborations.
 * Supports queries by ID, client, talent, and status filters.
 */
@Service
class GetCollaborationsUseCase(
    private val collaborationRepository: CollaborationRepository
) {
    private val logger = LoggerFactory.getLogger(GetCollaborationsUseCase::class.java)

    /**
     * Get a collaboration by ID.
     *
     * @param collaborationId The ID of the collaboration
     * @return The collaboration response
     * @throws ResourceNotFoundException if the collaboration is not found
     */
    fun getById(collaborationId: UUID): CollaborationResponse {
        logger.info("Getting collaboration by ID: $collaborationId")

        val collaboration = collaborationRepository.findById(collaborationId)
            ?: throw ResourceNotFoundException("Collaboration not found: $collaborationId")

        return CollaborationResponse.fromDomain(collaboration)
    }

    /**
     * Get all collaborations for a client user.
     *
     * @param clientUserId The ID of the client user
     * @return List of collaboration responses
     */
    fun getByClientUserId(clientUserId: UUID): CollaborationsResponse {
        logger.info("Getting collaborations for client: $clientUserId")

        val collaborations = collaborationRepository.findByClientUserId(clientUserId)
            .sortedByDescending { it.createdAt }
            .map { CollaborationResponse.fromDomain(it) }

        return CollaborationsResponse(
            collaborations = collaborations,
            total = collaborations.size
        )
    }

    /**
     * Get all collaborations for a talent profile.
     *
     * @param talentProfileId The ID of the talent profile
     * @return List of collaboration responses
     */
    fun getByTalentProfileId(talentProfileId: UUID): CollaborationsResponse {
        logger.info("Getting collaborations for talent: $talentProfileId")

        val collaborations = collaborationRepository.findByTalentProfileId(talentProfileId)
            .sortedByDescending { it.createdAt }
            .map { CollaborationResponse.fromDomain(it) }

        return CollaborationsResponse(
            collaborations = collaborations,
            total = collaborations.size
        )
    }

    /**
     * Get collaborations for a client filtered by status.
     *
     * @param clientUserId The ID of the client user
     * @param status The collaboration status
     * @return List of collaboration responses
     */
    fun getByClientUserIdAndStatus(clientUserId: UUID, status: CollaborationStatus): CollaborationsResponse {
        logger.info("Getting collaborations for client: $clientUserId with status: $status")

        val collaborations = collaborationRepository.findByClientUserIdAndStatus(clientUserId, status)
            .sortedByDescending { it.createdAt }
            .map { CollaborationResponse.fromDomain(it) }

        return CollaborationsResponse(
            collaborations = collaborations,
            total = collaborations.size
        )
    }

    /**
     * Get collaborations for a talent filtered by status.
     *
     * @param talentProfileId The ID of the talent profile
     * @param status The collaboration status
     * @return List of collaboration responses
     */
    fun getByTalentProfileIdAndStatus(talentProfileId: UUID, status: CollaborationStatus): CollaborationsResponse {
        logger.info("Getting collaborations for talent: $talentProfileId with status: $status")

        val collaborations = collaborationRepository.findByTalentProfileIdAndStatus(talentProfileId, status)
            .sortedByDescending { it.createdAt }
            .map { CollaborationResponse.fromDomain(it) }

        return CollaborationsResponse(
            collaborations = collaborations,
            total = collaborations.size
        )
    }
}
