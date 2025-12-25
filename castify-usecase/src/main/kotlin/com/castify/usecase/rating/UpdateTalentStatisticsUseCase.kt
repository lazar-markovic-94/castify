package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Use case for updating talent statistics (especially profile completeness).
 */
@Service
class UpdateTalentStatisticsUseCase(
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentStatisticsUseCase::class.java)

    /**
     * Updates profile completeness based on current talent profile data.
     *
     * IMPORTANT: Uses REQUIRES_NEW propagation to ensure each retry attempt
     * gets a fresh transaction with fresh entity data from the database.
     * This prevents Hibernate session-level caching from causing optimistic locking
     * failures on retries.
     *
     * @param talentProfileId The talent profile ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateProfileCompleteness(talentProfileId: UUID) {
        logger.info("Updating profile completeness for talent: $talentProfileId")

        // Get talent profile - always fresh read
        val talentProfile = talentRepository.findById(talentProfileId)
            ?: throw ResourceNotFoundException("Talent profile not found: $talentProfileId")

        logger.debug("Talent profile data - Bio: ${!talentProfile.bio.isNullOrBlank()}, " +
                "Location: ${talentProfile.primaryLocation != null}, " +
                "ProfileImage: ${!talentProfile.profileImageUrl.isNullOrBlank()}, " +
                "IntroVideo: ${!talentProfile.introductionVideoUrl.isNullOrBlank()}, " +
                "Languages: ${talentProfile.languages.size}, " +
                "Tags: ${talentProfile.tags.size}")

        // IMPORTANT: Always get fresh statistics from database to avoid stale version
        // This ensures we have the latest version number for optimistic locking
        val currentStatistics = statisticsRepository.findByTalentProfileId(talentProfileId)
            ?: TalentStatistics.createInitial(talentProfileId, 0)

        logger.debug("Current statistics - ProfileCompleteness: ${currentStatistics.profileCompleteness}%, " +
                "Tier: ${currentStatistics.tier}")

        // Calculate profile completeness
        val capabilitiesCount = listOf(
            talentProfile.capabilities.canDoActing,
            talentProfile.capabilities.canDoVoiceover,
            talentProfile.capabilities.canDoModeling,
            talentProfile.capabilities.canDoSinging,
            talentProfile.capabilities.canDoDancing,
            talentProfile.capabilities.canDoHosting
        ).count { it }

        logger.debug("Capabilities count: $capabilitiesCount")

        val profileCompleteness = currentStatistics.calculateProfileCompleteness(
            hasBio = !talentProfile.bio.isNullOrBlank(),
            hasLocation = talentProfile.primaryLocation != null,
            hasProfileImage = !talentProfile.profileImageUrl.isNullOrBlank(),
            languagesCount = talentProfile.languages.size,
            tagsCount = talentProfile.tags.size,
            capabilitiesCount = capabilitiesCount
        )

        logger.debug("Calculated profile completeness: $profileCompleteness%")

        // Check if value actually changed to avoid unnecessary updates
        if (currentStatistics.profileCompleteness == profileCompleteness) {
            logger.debug("Profile completeness unchanged at $profileCompleteness%, skipping update")
            return
        }

        // Update statistics with new completeness score and timestamp
        val updatedStatistics = currentStatistics.copy(
            profileCompleteness = profileCompleteness,
            lastUpdated = Instant.now()
        )

        // Recalculate tier based on updated completeness
        val finalStatistics = updatedStatistics.copy(
            tier = updatedStatistics.recalculateTier(),
            lastUpdated = Instant.now()
        )

        val savedStatistics = statisticsRepository.save(finalStatistics)
        logger.info("Profile completeness updated from ${currentStatistics.profileCompleteness}% to ${savedStatistics.profileCompleteness}% for talent: $talentProfileId")
    }
}
