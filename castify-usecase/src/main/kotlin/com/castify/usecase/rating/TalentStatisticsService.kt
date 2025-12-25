package com.castify.usecase.rating

import com.castify.domain.collaboration.CollaborationRepository
import com.castify.domain.collaboration.CollaborationStatus
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Centralized service for managing talent statistics updates.
 *
 * This service handles all statistics calculations and updates in the application layer,
 * replacing database triggers to avoid optimistic locking conflicts.
 *
 * All methods are transactional and should be called within a single transaction
 * with the main entity update (rating, collaboration, etc).
 */
@Service
class TalentStatisticsService(
    private val statisticsRepository: TalentStatisticsRepository,
    private val ratingRepository: TalentRatingRepository,
    private val collaborationRepository: CollaborationRepository
) {
    private val logger = LoggerFactory.getLogger(TalentStatisticsService::class.java)

    /**
     * Recalculates all rating-based statistics from scratch by querying all verified ratings.
     * This is the most accurate but also most expensive method.
     *
     * Use this method when:
     * - A rating's verification status changes
     * - A rating is deleted
     * - You need to ensure complete accuracy
     *
     * @param talentProfileId The talent profile ID
     */
    @Transactional
    fun recalculateRatingStatistics(talentProfileId: UUID) {
        logger.info("Recalculating rating statistics for talent: $talentProfileId")

        // Get all verified ratings
        val verifiedRatings = ratingRepository.findByTalentProfileIdAndIsVerified(talentProfileId, true)

        // Get current statistics
        val currentStats = statisticsRepository.findByTalentProfileId(talentProfileId)
            ?: TalentStatistics.createInitial(talentProfileId, 0)

        if (verifiedRatings.isEmpty()) {
            logger.debug("No verified ratings found for talent: $talentProfileId, setting ratings to zero")
            val updatedStats = currentStats.copy(
                totalRatings = 0,
                averageRating = 0.0,
                averageProfessionalismRating = 0.0,
                averageQualityRating = 0.0,
                averageCommunicationRating = 0.0,
                averageTimelinessRating = 0.0,
                wouldWorkAgainPercentage = 0.0,
                lastUpdated = Instant.now()
            )
            statisticsRepository.save(updatedStats)
            return
        }

        // Calculate aggregated metrics
        val totalRatings = verifiedRatings.size
        val avgOverall = verifiedRatings.map { it.overallRating }.average()
        val avgProfessionalism = verifiedRatings.map { it.professionalismRating }.average()
        val avgQuality = verifiedRatings.map { it.qualityRating }.average()
        val avgCommunication = verifiedRatings.map { it.communicationRating }.average()
        val avgTimeliness = verifiedRatings.map { it.timelinessRating }.average()
        val wouldWorkAgainCount = verifiedRatings.count { it.wouldWorkAgain }
        val wouldWorkAgainPct = (wouldWorkAgainCount.toDouble() / totalRatings) * 100.0

        logger.debug("Calculated statistics - Total: $totalRatings, Avg: $avgOverall, WouldWorkAgain: $wouldWorkAgainPct%")

        val updatedStats = currentStats.copy(
            totalRatings = totalRatings,
            averageRating = avgOverall,
            averageProfessionalismRating = avgProfessionalism,
            averageQualityRating = avgQuality,
            averageCommunicationRating = avgCommunication,
            averageTimelinessRating = avgTimeliness,
            wouldWorkAgainPercentage = wouldWorkAgainPct,
            lastUpdated = Instant.now()
        )

        // Recalculate tier based on new ratings
        val finalStats = updatedStats.copy(
            tier = updatedStats.recalculateTier(),
            lastUpdated = Instant.now()
        )

        statisticsRepository.save(finalStats)
        logger.info("Successfully recalculated rating statistics for talent: $talentProfileId")
    }

    /**
     * Updates statistics incrementally when a new verified rating is added.
     * This is more efficient than recalculating from scratch.
     *
     * @param newRating The newly created rating (must be verified)
     */
    @Transactional
    fun updateWithNewRating(newRating: TalentRating) {
        if (!newRating.isVerified) {
            logger.debug("Rating ${newRating.ratingId} is not verified, skipping statistics update")
            return
        }

        logger.info("Updating statistics with new rating ${newRating.ratingId} for talent: ${newRating.talentProfileId}")

        val currentStats = statisticsRepository.findByTalentProfileId(newRating.talentProfileId)
            ?: TalentStatistics.createInitial(newRating.talentProfileId, 0)

        val updatedStats = TalentStatistics.updateWithNewRating(currentStats, newRating)

        statisticsRepository.save(updatedStats)
        logger.info("Successfully updated statistics with new rating for talent: ${newRating.talentProfileId}")
    }

    /**
     * Recalculates all collaboration-based statistics from scratch by querying all collaborations.
     * This matches the behavior of the database trigger which recalculates on every change.
     *
     * Use this method whenever a collaboration status changes.
     *
     * @param talentProfileId The talent profile ID
     */
    @Transactional
    fun recalculateCollaborationStatistics(talentProfileId: UUID) {
        logger.info("Recalculating collaboration statistics for talent: $talentProfileId")

        // Get all collaborations for this talent
        val collaborations = collaborationRepository.findByTalentProfileId(talentProfileId)

        // Get current statistics
        val currentStats = statisticsRepository.findByTalentProfileId(talentProfileId)
            ?: TalentStatistics.createInitial(talentProfileId, 0)

        // Calculate metrics
        val totalCollaborations = collaborations.size
        val completedCollaborations = collaborations.count { it.status == CollaborationStatus.COMPLETED }
        val canceledCollaborations = collaborations.count { it.status == CollaborationStatus.CANCELED }
        val successRate = if (totalCollaborations > 0) {
            (completedCollaborations.toDouble() / totalCollaborations) * 100.0
        } else {
            0.0
        }
        val totalEarnings = collaborations
            .filter { it.status == CollaborationStatus.COMPLETED }
            .mapNotNull { it.budget }
            .sumOf { it.toDouble() }

        logger.debug("Calculated collaboration statistics - Total: $totalCollaborations, Completed: $completedCollaborations, Success Rate: $successRate%, Earnings: $totalEarnings")

        val updatedStats = currentStats.copy(
            totalCollaborations = totalCollaborations,
            completedCollaborations = completedCollaborations,
            canceledCollaborations = canceledCollaborations,
            successRate = successRate,
            totalEarnings = totalEarnings,
            lastUpdated = Instant.now()
        )

        // Recalculate tier based on new stats
        val finalStats = updatedStats.copy(
            tier = updatedStats.recalculateTier(),
            lastUpdated = Instant.now()
        )

        statisticsRepository.save(finalStats)
        logger.info("Successfully recalculated collaboration statistics for talent: $talentProfileId")
    }

    /**
     * Updates profile completeness score.
     *
     * @param talentProfileId The talent profile ID
     * @param hasBio Whether the profile has a bio
     * @param hasLocation Whether the profile has a location
     * @param hasProfileImage Whether the profile has a profile image
     * @param languagesCount Number of languages
     * @param tagsCount Number of tags
     * @param capabilitiesCount Number of capabilities
     */
    @Transactional
    fun updateProfileCompleteness(
        talentProfileId: UUID,
        hasBio: Boolean,
        hasLocation: Boolean,
        hasProfileImage: Boolean,
        languagesCount: Int,
        tagsCount: Int,
        capabilitiesCount: Int
    ) {
        logger.info("Updating profile completeness for talent: $talentProfileId")

        val currentStats = statisticsRepository.findByTalentProfileId(talentProfileId)
            ?: TalentStatistics.createInitial(talentProfileId, 0)

        val newCompleteness = currentStats.calculateProfileCompleteness(
            hasBio = hasBio,
            hasLocation = hasLocation,
            hasProfileImage = hasProfileImage,
            languagesCount = languagesCount,
            tagsCount = tagsCount,
            capabilitiesCount = capabilitiesCount
        )

        if (currentStats.profileCompleteness == newCompleteness) {
            logger.debug("Profile completeness unchanged at $newCompleteness%, skipping update")
            return
        }

        val updatedStats = currentStats.copy(
            profileCompleteness = newCompleteness,
            lastUpdated = Instant.now()
        )
        val finalStats = updatedStats.copy(
            tier = updatedStats.recalculateTier(),
            lastUpdated = Instant.now()
        )

        statisticsRepository.save(finalStats)
        logger.info("Updated profile completeness from ${currentStats.profileCompleteness}% to $newCompleteness% for talent: $talentProfileId")
    }
}
