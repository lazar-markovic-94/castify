package com.castify.persistence.entity

import com.castify.domain.collaboration.Collaboration
import com.castify.domain.collaboration.CollaborationStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "collaboration")
data class CollaborationEntity(
    @Id
    @Column(name = "collaboration_id")
    val collaborationId: UUID = UUID.randomUUID(),

    @Column(name = "client_user_id", nullable = false)
    val clientUserId: UUID,

    @Column(name = "talent_profile_id", nullable = false)
    val talentProfileId: UUID,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    val description: String,

    @Column(name = "budget", precision = 10, scale = 2)
    val budget: BigDecimal? = null,

    @Column(name = "start_date")
    val startDate: Instant? = null,

    @Column(name = "end_date")
    val endDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: CollaborationStatus,

    @Column(name = "proposed_at", nullable = false)
    val proposedAt: Instant,

    @Column(name = "responded_at")
    val respondedAt: Instant? = null,

    @Column(name = "completed_at")
    val completedAt: Instant? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Convert JPA entity to domain model
     */
    fun toDomain(): Collaboration {
        return Collaboration(
            collaborationId = collaborationId,
            clientUserId = clientUserId,
            talentProfileId = talentProfileId,
            title = title,
            description = description,
            budget = budget,
            startDate = startDate,
            endDate = endDate,
            status = status,
            proposedAt = proposedAt,
            respondedAt = respondedAt,
            completedAt = completedAt,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Convert domain model to JPA entity
         */
        fun fromDomain(collaboration: Collaboration): CollaborationEntity {
            return CollaborationEntity(
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
