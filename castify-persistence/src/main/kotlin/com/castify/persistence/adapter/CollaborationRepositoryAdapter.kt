package com.castify.persistence.adapter

import com.castify.domain.collaboration.Collaboration
import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.collaboration.CollaborationStatus
import com.castify.persistence.entity.CollaborationEntity
import com.castify.persistence.jpa.CollaborationJpaRepository
import org.springframework.stereotype.Component
import java.util.*

/**
 * Adapter implementation of CollaborationRepository port.
 * Implements the domain repository interface using Spring Data JPA.
 */
@Component
class CollaborationRepositoryAdapter(
    private val jpaRepository: CollaborationJpaRepository
) : CollaborationRepository {

    override fun save(collaboration: Collaboration): Collaboration {
        // Find existing entity to preserve version for optimistic locking
        val entity = jpaRepository.findById(collaboration.collaborationId)
            .map { existing ->
                // Update existing entity to preserve version
                existing.apply {
                    clientUserId = collaboration.clientUserId
                    talentProfileId = collaboration.talentProfileId
                    title = collaboration.title
                    description = collaboration.description
                    servicesRequired = collaboration.servicesRequired
                    deadline = collaboration.deadline
                    budget = collaboration.budget
                    startDate = collaboration.startDate
                    endDate = collaboration.endDate
                    status = collaboration.status
                    proposedAt = collaboration.proposedAt
                    respondedAt = collaboration.respondedAt
                    completedAt = collaboration.completedAt
                    notes = collaboration.notes
                    updatedAt = collaboration.updatedAt
                }
            }
            .orElseGet {
                // Create new entity for new collaborations
                CollaborationEntity.fromDomain(collaboration)
            }

        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(collaborationId: UUID): Collaboration? {
        return jpaRepository.findById(collaborationId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByClientUserId(clientUserId: UUID): List<Collaboration> {
        return jpaRepository.findByClientUserId(clientUserId)
            .map { it.toDomain() }
    }

    override fun findByTalentProfileId(talentProfileId: UUID): List<Collaboration> {
        return jpaRepository.findByTalentProfileId(talentProfileId)
            .map { it.toDomain() }
    }

    override fun findByClientUserIdAndStatus(
        clientUserId: UUID,
        status: CollaborationStatus
    ): List<Collaboration> {
        return jpaRepository.findByClientUserIdAndStatus(clientUserId, status)
            .map { it.toDomain() }
    }

    override fun findByTalentProfileIdAndStatus(
        talentProfileId: UUID,
        status: CollaborationStatus
    ): List<Collaboration> {
        return jpaRepository.findByTalentProfileIdAndStatus(talentProfileId, status)
            .map { it.toDomain() }
    }

    override fun countByTalentProfileIdAndStatus(
        talentProfileId: UUID,
        status: CollaborationStatus
    ): Int {
        return jpaRepository.countByTalentProfileIdAndStatus(talentProfileId, status)
    }
}
