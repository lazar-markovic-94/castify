package com.castify.persistence.adapter

import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import com.castify.persistence.entity.TalentRatingEntity
import com.castify.persistence.jpa.TalentRatingJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.*

/**
 * Adapter implementation of TalentRatingRepository port.
 * Implements the domain repository interface using Spring Data JPA.
 */
@Component
class TalentRatingRepositoryAdapter(
    private val jpaRepository: TalentRatingJpaRepository
) : TalentRatingRepository {

    override fun save(rating: TalentRating): TalentRating {
        val entity = TalentRatingEntity.fromDomain(rating)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(ratingId: UUID): TalentRating? {
        return jpaRepository.findById(ratingId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByTalentProfileId(talentProfileId: UUID): List<TalentRating> {
        return jpaRepository.findByTalentProfileId(talentProfileId)
            .map { it.toDomain() }
    }

    override fun findByClientUserId(clientUserId: UUID): List<TalentRating> {
        return jpaRepository.findByClientUserId(clientUserId)
            .map { it.toDomain() }
    }

    override fun findByTalentProfileIdAndCollaborationId(
        talentProfileId: UUID,
        collaborationId: UUID
    ): TalentRating? {
        return jpaRepository.findByTalentProfileIdAndCollaborationId(talentProfileId, collaborationId)
            ?.toDomain()
    }

    override fun findByTalentProfileIdAndClientUserIdAndCollaborationId(
        talentProfileId: UUID,
        clientUserId: UUID,
        collaborationId: UUID
    ): TalentRating? {
        return jpaRepository.findByTalentProfileIdAndClientUserIdAndCollaborationId(
            talentProfileId,
            clientUserId,
            collaborationId
        )?.toDomain()
    }

    override fun findByTalentProfileIdPaginated(
        talentProfileId: UUID,
        page: Int,
        size: Int
    ): List<TalentRating> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return jpaRepository.findByTalentProfileId(talentProfileId, pageable)
            .map { it.toDomain() }
    }

    override fun countByTalentProfileId(talentProfileId: UUID): Long {
        return jpaRepository.countByTalentProfileId(talentProfileId)
    }

    override fun deleteById(ratingId: UUID) {
        jpaRepository.deleteById(ratingId)
    }
}
