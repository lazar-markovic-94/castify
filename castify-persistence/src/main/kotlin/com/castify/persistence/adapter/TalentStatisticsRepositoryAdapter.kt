package com.castify.persistence.adapter

import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.rating.TalentTier
import com.castify.persistence.entity.TalentStatisticsEntity
import com.castify.persistence.jpa.TalentStatisticsJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.*

/**
 * Adapter implementation of TalentStatisticsRepository port.
 * Implements the domain repository interface using Spring Data JPA.
 */
@Component
class TalentStatisticsRepositoryAdapter(
    private val jpaRepository: TalentStatisticsJpaRepository
) : TalentStatisticsRepository {

    override fun save(statistics: TalentStatistics): TalentStatistics {
        val entity = TalentStatisticsEntity.fromDomain(statistics)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByTalentProfileId(talentProfileId: UUID): TalentStatistics? {
        return jpaRepository.findById(talentProfileId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByTalentProfileIds(talentProfileIds: List<UUID>): List<TalentStatistics> {
        return jpaRepository.findByTalentProfileIdIn(talentProfileIds)
            .map { it.toDomain() }
    }

    override fun findByTier(tier: TalentTier): List<TalentStatistics> {
        return jpaRepository.findByPerformanceTier(tier)
            .map { it.toDomain() }
    }

    override fun findByMinimumAverageRating(minimumRating: Double): List<TalentStatistics> {
        return jpaRepository.findByMinimumAverageRating(minimumRating)
            .map { it.toDomain() }
    }

    override fun findTopRated(limit: Int): List<TalentStatistics> {
        val pageable = PageRequest.of(0, limit)
        return jpaRepository.findAllByOrderByAverageRatingDesc(pageable)
            .map { it.toDomain() }
    }

    override fun findMostSuccessful(limit: Int): List<TalentStatistics> {
        val pageable = PageRequest.of(0, limit)
        return jpaRepository.findAllByOrderByCompletedCollaborationsDesc(pageable)
            .map { it.toDomain() }
    }

    override fun deleteByTalentProfileId(talentProfileId: UUID) {
        jpaRepository.deleteById(talentProfileId)
    }

    override fun existsByTalentProfileId(talentProfileId: UUID): Boolean {
        return jpaRepository.existsByTalentProfileId(talentProfileId)
    }
}
