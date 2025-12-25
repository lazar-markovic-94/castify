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
        // IMPORTANT: For updates, we must fetch the existing entity first to preserve the version
        // This ensures optimistic locking works correctly
        val existingEntity = jpaRepository.findById(statistics.talentProfileId).orElse(null)

        val entityToSave = if (existingEntity != null) {
            // Update existing entity to preserve version for optimistic locking
            existingEntity.apply {
                averageRating = statistics.averageRating
                totalRatings = statistics.totalRatings
                averageProfessionalismRating = statistics.averageProfessionalismRating
                averageQualityRating = statistics.averageQualityRating
                averageCommunicationRating = statistics.averageCommunicationRating
                averageTimelinessRating = statistics.averageTimelinessRating
                totalCollaborations = statistics.totalCollaborations
                completedCollaborations = statistics.completedCollaborations
                canceledCollaborations = statistics.canceledCollaborations
                successRate = statistics.successRate
                totalEarnings = java.math.BigDecimal.valueOf(statistics.totalEarnings)
                averageResponseTimeHours = statistics.averageResponseTimeHours
                profileCompleteness = statistics.profileCompleteness
                wouldWorkAgainPercentage = statistics.wouldWorkAgainPercentage
                performanceTier = statistics.tier
                lastUpdated = statistics.lastUpdated
            }
        } else {
            // Create new entity for first save
            TalentStatisticsEntity.fromDomain(statistics)
        }

        val saved = jpaRepository.save(entityToSave)
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
}
