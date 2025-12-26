package com.castify.persistence.entity

import com.castify.domain.collaboration.Collaboration
import com.castify.domain.collaboration.CollaborationStatus
import com.castify.domain.collaboration.ServiceType
import com.castify.persistence.converter.ServiceTypeArrayConverter
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "collaboration")
class CollaborationEntity(
    @Id
    @Column(name = "collaboration_id")
    val collaborationId: UUID = UUID.randomUUID(),

    @Column(name = "client_user_id", nullable = false)
    var clientUserId: UUID,

    @Column(name = "talent_profile_id", nullable = false)
    var talentProfileId: UUID,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String,

    @Column(name = "services_required", columnDefinition = "TEXT[]", nullable = false)
    @Convert(converter = ServiceTypeArrayConverter::class)
    var servicesRequired: List<ServiceType>,

    @Column(name = "deadline")
    var deadline: Instant? = null,

    @Column(name = "budget", precision = 10, scale = 2)
    var budget: BigDecimal? = null,

    @Column(name = "start_date")
    var startDate: Instant? = null,

    @Column(name = "end_date")
    var endDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CollaborationStatus,

    @Column(name = "proposed_at", nullable = false)
    var proposedAt: Instant,

    @Column(name = "responded_at")
    var respondedAt: Instant? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @Column(name = "created_by")
    var createdBy: UUID? = null,

    @Column(name = "updated_by")
    var updatedBy: UUID? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
) {
    /**
     * JPA entities must have equals/hashCode based only on ID to work correctly
     * with lazy loading, proxies, and collections
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollaborationEntity) return false
        return collaborationId == other.collaborationId
    }

    override fun hashCode(): Int = collaborationId.hashCode()

    override fun toString(): String =
        "CollaborationEntity(collaborationId=$collaborationId, title='$title', status=$status)"
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
            servicesRequired = servicesRequired,
            deadline = deadline,
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
                servicesRequired = collaboration.servicesRequired,
                deadline = collaboration.deadline,
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
