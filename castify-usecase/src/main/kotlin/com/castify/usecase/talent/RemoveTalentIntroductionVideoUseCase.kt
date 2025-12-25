package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for removing a talent's introduction video.
 */
@Service
@Transactional
class RemoveTalentIntroductionVideoUseCase(
    private val talentRepository: TalentRepository,
    private val asyncStatisticsUpdateService: com.castify.usecase.upload.AsyncStatisticsUpdateService
) {
    private val logger = LoggerFactory.getLogger(RemoveTalentIntroductionVideoUseCase::class.java)

    fun execute(userId: UUID): TalentProfileResponse {
        return try {
            logger.info("Removing introduction video for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            // Remove the introduction video by setting URL and duration to null
            val updatedProfile = profile.copy(
                introductionVideoUrl = null,
                introductionVideoDuration = null
            )

            val savedProfile = talentRepository.save(updatedProfile)
            logger.info("Successfully removed introduction video for talent: ${savedProfile.talentProfileId}")

            // Update profile completeness statistics in a separate transaction
            asyncStatisticsUpdateService.updateProfileCompleteness(savedProfile.talentProfileId)

            TalentProfileResponse.fromDomain(savedProfile)
        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error removing introduction video for user: $userId", e)
            throw RuntimeException("Failed to remove introduction video: ${e.message}", e)
        }
    }
}
