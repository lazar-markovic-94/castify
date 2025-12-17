package com.castify.usecase.dto

import com.castify.domain.collaboration.Collaboration
import com.castify.domain.collaboration.CollaborationStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.*

// ==================== Request DTOs ====================

/**
 * Request DTO for creating a new collaboration proposal
 */
data class CreateCollaborationRequestDto(
    val talentProfileId: UUID,
    val title: String,
    val description: String,
    val budget: BigDecimal?,
    val startDate: Instant?,
    val endDate: Instant?,
    val notes: String?
)

/**
 * Request DTO for updating collaboration details
 */
data class UpdateCollaborationRequestDto(
    val title: String?,
    val description: String?,
    val budget: BigDecimal?,
    val startDate: Instant?,
    val endDate: Instant?,
    val notes: String?
)

// ==================== Response DTOs ====================

/**
 * Response DTO for a single collaboration
 */
data class CollaborationResponse(
    val collaborationId: UUID,
    val clientUserId: UUID,
    val talentProfileId: UUID,
    val title: String,
    val description: String,
    val budget: BigDecimal?,
    val startDate: Instant?,
    val endDate: Instant?,
    val status: CollaborationStatus,
    val proposedAt: Instant,
    val respondedAt: Instant?,
    val completedAt: Instant?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(collaboration: Collaboration): CollaborationResponse {
            return CollaborationResponse(
                collaborationId = collaboration.collaborationId,
                clientUserId = collaboration.clientUserId,
                talentProfileId = collaboration.talentProfileId,
                title = collaboration.title,
                description = collaboration.description,
                budget = collaboration.budget,
                startDate = collaboration.startDate,
                endDate = collaboration.endDate,
                status = collaboration.status,
                proposedAt = collaboration.proposedAt,
                respondedAt = collaboration.respondedAt,
                completedAt = collaboration.completedAt,
                notes = collaboration.notes,
                createdAt = collaboration.createdAt,
                updatedAt = collaboration.updatedAt
            )
        }
    }
}

/**
 * Response DTO for a list of collaborations
 */
data class CollaborationsResponse(
    val collaborations: List<CollaborationResponse>,
    val total: Int
)
