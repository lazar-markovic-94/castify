package com.castify.usecase.collaboration

import com.castify.domain.collaboration.Collaboration
import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.domain.user.UserRepository
import com.castify.usecase.dto.CollaborationResponse
import com.castify.usecase.dto.CreateCollaborationRequestDto
import com.castify.usecase.rating.TalentStatisticsService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating a new collaboration proposal.
 * Handles proposal creation between clients and talents.
 */
@Service
@Transactional
class CreateCollaborationUseCase(
    private val collaborationRepository: CollaborationRepository,
    private val talentRepository: TalentRepository,
    private val userRepository: UserRepository,
    private val statisticsService: TalentStatisticsService
) {
    private val logger = LoggerFactory.getLogger(CreateCollaborationUseCase::class.java)

    /**
     * Creates a new collaboration proposal from a client to a talent.
     *
     * @param clientUserId The ID of the client creating the proposal
     * @param request The collaboration creation request
     * @return The created collaboration response
     * @throws ResourceNotFoundException if the client or talent is not found
     */
    fun execute(clientUserId: UUID, request: CreateCollaborationRequestDto): CollaborationResponse {
        return try {
            logger.info("Creating collaboration proposal from client: $clientUserId to talent: ${request.talentProfileId}")

            // Validate client exists
            userRepository.findById(clientUserId)
                ?: throw ResourceNotFoundException("Client user not found: $clientUserId")

            // Validate talent exists
            talentRepository.findById(request.talentProfileId)
                ?: throw ResourceNotFoundException("Talent profile not found: ${request.talentProfileId}")

            // Create collaboration
            val collaboration = Collaboration.create(
                clientUserId = clientUserId,
                talentProfileId = request.talentProfileId,
                title = request.title,
                description = request.description,
                servicesRequired = request.servicesRequired,
                deadline = request.deadline,
                budget = request.budget,
                startDate = request.startDate,
                endDate = request.endDate,
                notes = request.notes
            )

            val savedCollaboration = collaborationRepository.save(collaboration)
            logger.info("Successfully created collaboration: ${savedCollaboration.collaborationId}")

            // Update statistics in application layer (replaces database trigger)
            statisticsService.recalculateCollaborationStatistics(savedCollaboration.talentProfileId)

            CollaborationResponse.fromDomain(savedCollaboration)

        } catch (e: ResourceNotFoundException) {
            logger.error("Resource not found while creating collaboration", e)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error creating collaboration", e)
            throw RuntimeException("Failed to create collaboration: ${e.message}", e)
        }
    }
}
