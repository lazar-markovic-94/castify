package com.castify.persistence.jpa

import com.castify.domain.rating.TalentTier
import com.castify.persistence.entity.TalentStatisticsEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TalentStatisticsJpaRepository : JpaRepository<TalentStatisticsEntity, UUID> {

    /**
     * Find statistics for multiple talent profiles
     */
    fun findByTalentProfileIdIn(talentProfileIds: List<UUID>): List<TalentStatisticsEntity>

    /**
     * Find talents by performance tier
     */
    fun findByPerformanceTier(performanceTier: TalentTier): List<TalentStatisticsEntity>

    /**
     * Find talents with minimum average rating
     */
    @Query("SELECT s FROM TalentStatisticsEntity s WHERE s.averageRating >= :minimumRating")
    fun findByMinimumAverageRating(@Param("minimumRating") minimumRating: Double): List<TalentStatisticsEntity>

    /**
     * Find top rated talents
     */
    fun findAllByOrderByAverageRatingDesc(pageable: Pageable): List<TalentStatisticsEntity>

    /**
     * Find most successful talents (by completed collaborations)
     */
    fun findAllByOrderByCompletedCollaborationsDesc(pageable: Pageable): List<TalentStatisticsEntity>

    /**
     * Check if statistics exist for a talent profile
     */
    fun existsByTalentProfileId(talentProfileId: UUID): Boolean
}
