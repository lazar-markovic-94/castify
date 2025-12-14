package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating profile completeness score based on profile data.
 */
@Service
@Transactional
class UpdateProfileCompletenessUseCase(
    private val statisticsRepository: TalentStatisticsRepository,
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateProfileCompletenessUseCase::class.java)

    /**
     * Updates the profile completeness score for a talent.
     *
     * @param talentProfileId The talent profile ID
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun execute(talentProfileId: UUID) {
        try {
            logger.info("Updating profile completeness for talent: $talentProfileId")

            val profile = talentRepository.findById(talentProfileId)
                ?: throw ResourceNotFoundException("Talent profile not found: $talentProfileId")

            val statistics = statisticsRepository.findByTalentProfileId(talentProfileId)
                ?: TalentStatistics.createInitial(talentProfileId, 0)

            // Calculate completeness based on profile
            val completeness = statistics.calculateProfileCompleteness(
                hasBio = !profile.bio.isNullOrBlank(),
                hasLocation = true, // Location is always present
                hasProfileImage = !profile.profileImageUrl.isNullOrBlank(),
                languagesCount = profile.languages.size,
                tagsCount = profile.tags.size,
                capabilitiesCount = profile.capabilities.countActiveCapabilities(),
                socialLinksCount = profile.socialLinks.size
            )

            val updatedStats = statistics.copy(
                profileCompleteness = completeness,
                tier = statistics.copy(profileCompleteness = completeness).recalculateTier()
            )

            statisticsRepository.save(updatedStats)
            logger.info("Updated profile completeness for talent: $talentProfileId to $completeness%")

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: $talentProfileId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating profile completeness for talent: $talentProfileId", e)
            throw RuntimeException("Failed to update profile completeness: ${e.message}", e)
        }
    }

    private fun com.castify.domain.talent.TalentCapabilities.countActiveCapabilities(): Int {
        var count = 0
        if (canDoVoiceover) count++
        if (canDoActing) count++
        if (canDoModeling) count++
        if (canDoSinging) count++
        if (canDoDancing) count++
        if (canDoHosting) count++
        return count
    }
}
