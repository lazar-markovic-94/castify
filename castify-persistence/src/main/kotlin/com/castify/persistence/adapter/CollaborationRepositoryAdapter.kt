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
        val entity = CollaborationEntity.fromDomain(collaboration)
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

    override fun existsByClientAndTalent(clientUserId: UUID, talentProfileId: UUID): Boolean {
        return jpaRepository.existsByClientUserIdAndTalentProfileId(clientUserId, talentProfileId)
    }

    override fun countByTalentProfileIdAndStatus(
        talentProfileId: UUID,
        status: CollaborationStatus
    ): Int {
        return jpaRepository.countByTalentProfileIdAndStatus(talentProfileId, status)
    }

    override fun delete(collaborationId: UUID) {
        jpaRepository.deleteById(collaborationId)
    }
}
