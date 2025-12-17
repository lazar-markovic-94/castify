package com.castify.web.controller

import com.castify.domain.collaboration.CollaborationStatus
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.collaboration.*
import com.castify.usecase.dto.CollaborationResponse
import com.castify.usecase.dto.CollaborationsResponse
import com.castify.usecase.dto.CreateCollaborationRequestDto
import com.castify.web.security.SecurityUtils
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for collaboration operations.
 *
 * Endpoints:
 * - POST /api/collaborations - Create a new collaboration proposal (CLIENT only)
 * - GET /api/collaborations/{id} - Get a specific collaboration
 * - GET /api/collaborations/client/me - Get collaborations for current client
 * - GET /api/collaborations/talent/me - Get collaborations for current talent
 * - POST /api/collaborations/{id}/accept - Accept a collaboration proposal (TALENT only)
 * - POST /api/collaborations/{id}/decline - Decline a collaboration proposal (TALENT only)
 * - POST /api/collaborations/{id}/start - Start work on a collaboration
 * - POST /api/collaborations/{id}/complete - Complete a collaboration
 * - POST /api/collaborations/{id}/cancel - Cancel a collaboration
 */
@RestController
@RequestMapping("/api/collaborations")
class CollaborationController(
    private val createCollaborationUseCase: CreateCollaborationUseCase,
    private val getCollaborationsUseCase: GetCollaborationsUseCase,
    private val respondToCollaborationUseCase: RespondToCollaborationUseCase,
    private val updateCollaborationStatusUseCase: UpdateCollaborationStatusUseCase,
    private val talentRepository: TalentRepository
) {

    /**
     * Create a new collaboration proposal.
     * Only clients can create proposals.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    fun createCollaboration(
        @Valid @RequestBody request: CreateCollaborationRequestDto
    ): ResponseEntity<CollaborationResponse> {
        val clientUserId = SecurityUtils.getCurrentUserId()
        val collaboration = createCollaborationUseCase.execute(clientUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(collaboration)
    }

    /**
     * Get a specific collaboration by ID.
     */
    @GetMapping("/{collaborationId}")
    fun getCollaborationById(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = getCollaborationsUseCase.getById(collaborationId)
        return ResponseEntity.ok(collaboration)
    }

    /**
     * Get all collaborations for the current client user.
     * Can optionally filter by status.
     */
    @GetMapping("/client/me")
    @PreAuthorize("hasRole('CLIENT')")
    fun getClientCollaborations(
        @RequestParam(required = false) status: CollaborationStatus?
    ): ResponseEntity<CollaborationsResponse> {
        val clientUserId = SecurityUtils.getCurrentUserId()

        val collaborations = if (status != null) {
            getCollaborationsUseCase.getByClientUserIdAndStatus(clientUserId, status)
        } else {
            getCollaborationsUseCase.getByClientUserId(clientUserId)
        }

        return ResponseEntity.ok(collaborations)
    }

    /**
     * Get all collaborations for the current talent user.
     * Can optionally filter by status.
     */
    @GetMapping("/talent/me")
    @PreAuthorize("hasRole('TALENT')")
    fun getTalentCollaborations(
        @RequestParam(required = false) status: CollaborationStatus?
    ): ResponseEntity<CollaborationsResponse> {
        val userId = SecurityUtils.getCurrentUserId()

        // Get talent profile for this user
        val talentProfile = talentRepository.findByUserId(userId)
            ?: throw IllegalStateException("Talent profile not found for user: $userId")

        val collaborations = if (status != null) {
            getCollaborationsUseCase.getByTalentProfileIdAndStatus(talentProfile.talentProfileId, status)
        } else {
            getCollaborationsUseCase.getByTalentProfileId(talentProfile.talentProfileId)
        }

        return ResponseEntity.ok(collaborations)
    }

    /**
     * Accept a collaboration proposal.
     * Only talents can accept proposals sent to them.
     */
    @PostMapping("/{collaborationId}/accept")
    @PreAuthorize("hasRole('TALENT')")
    fun acceptCollaboration(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = respondToCollaborationUseCase.accept(collaborationId)
        return ResponseEntity.ok(collaboration)
    }

    /**
     * Decline a collaboration proposal.
     * Only talents can decline proposals sent to them.
     */
    @PostMapping("/{collaborationId}/decline")
    @PreAuthorize("hasRole('TALENT')")
    fun declineCollaboration(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = respondToCollaborationUseCase.decline(collaborationId)
        return ResponseEntity.ok(collaboration)
    }

    /**
     * Start work on an accepted collaboration.
     */
    @PostMapping("/{collaborationId}/start")
    fun startCollaboration(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = updateCollaborationStatusUseCase.startWork(collaborationId)
        return ResponseEntity.ok(collaboration)
    }

    /**
     * Complete an in-progress collaboration.
     */
    @PostMapping("/{collaborationId}/complete")
    fun completeCollaboration(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = updateCollaborationStatusUseCase.complete(collaborationId)
        return ResponseEntity.ok(collaboration)
    }

    /**
     * Cancel a collaboration.
     */
    @PostMapping("/{collaborationId}/cancel")
    fun cancelCollaboration(
        @PathVariable collaborationId: UUID
    ): ResponseEntity<CollaborationResponse> {
        val collaboration = updateCollaborationStatusUseCase.cancel(collaborationId)
        return ResponseEntity.ok(collaboration)
    }
}
