package com.castify.domain.collaboration

import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Represents a collaboration/project between a client and a talent
 */
data class Collaboration(
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
        fun create(
            clientUserId: UUID,
            talentProfileId: UUID,
            title: String,
            description: String,
            budget: BigDecimal?,
            startDate: Instant?,
            endDate: Instant?,
            notes: String?
        ): Collaboration {
            val now = Instant.now()
            return Collaboration(
                collaborationId = UUID.randomUUID(),
                clientUserId = clientUserId,
                talentProfileId = talentProfileId,
                title = title,
                description = description,
                budget = budget,
                startDate = startDate,
                endDate = endDate,
                status = CollaborationStatus.PROPOSED,
                proposedAt = now,
                respondedAt = null,
                completedAt = null,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun accept(): Collaboration {
        require(status == CollaborationStatus.PROPOSED) { "Can only accept proposed collaborations" }
        return copy(
            status = CollaborationStatus.ACCEPTED,
            respondedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun decline(): Collaboration {
        require(status == CollaborationStatus.PROPOSED) { "Can only decline proposed collaborations" }
        return copy(
            status = CollaborationStatus.DECLINED,
            respondedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun startWork(): Collaboration {
        require(status == CollaborationStatus.ACCEPTED) { "Can only start work on accepted collaborations" }
        return copy(
            status = CollaborationStatus.IN_PROGRESS,
            updatedAt = Instant.now()
        )
    }

    fun complete(): Collaboration {
        require(status == CollaborationStatus.IN_PROGRESS) { "Can only complete in-progress collaborations" }
        return copy(
            status = CollaborationStatus.COMPLETED,
            completedAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    fun cancel(): Collaboration {
        require(status != CollaborationStatus.COMPLETED && status != CollaborationStatus.DECLINED) {
            "Cannot cancel completed or declined collaborations"
        }
        return copy(
            status = CollaborationStatus.CANCELED,
            updatedAt = Instant.now()
        )
    }

    fun updateDetails(
        title: String?,
        description: String?,
        budget: BigDecimal?,
        startDate: Instant?,
        endDate: Instant?,
        notes: String?
    ): Collaboration {
        return copy(
            title = title ?: this.title,
            description = description ?: this.description,
            budget = budget ?: this.budget,
            startDate = startDate ?: this.startDate,
            endDate = endDate ?: this.endDate,
            notes = notes ?: this.notes,
            updatedAt = Instant.now()
        )
    }

    fun isActive(): Boolean {
        return status in listOf(
            CollaborationStatus.PROPOSED,
            CollaborationStatus.ACCEPTED,
            CollaborationStatus.IN_PROGRESS
        )
    }

    fun isCompleted(): Boolean {
        return status == CollaborationStatus.COMPLETED
    }

    fun canBeRated(): Boolean {
        return status == CollaborationStatus.COMPLETED
    }
}
