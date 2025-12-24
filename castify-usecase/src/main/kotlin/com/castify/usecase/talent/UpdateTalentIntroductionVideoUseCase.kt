package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdateIntroductionVideoRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UpdateTalentIntroductionVideoUseCase(
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentIntroductionVideoUseCase::class.java)

    fun execute(userId: UUID, request: UpdateIntroductionVideoRequest): TalentProfileResponse {
        return try {
            logger.info("Updating introduction video for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val videoUrl = requireNotNull(request.introductionVideoUrl) { "Introduction video URL is required" }
            val duration = requireNotNull(request.introductionVideoDuration) { "Introduction video duration is required" }

            require(duration > 0) { "Introduction video duration must be positive" }
            require(duration <= 30) { "Introduction video must be 30 seconds or less" }

            val updatedProfile = profile.updateIntroductionVideo(videoUrl, duration)
            val savedProfile = talentRepository.save(updatedProfile)

            logger.info("Successfully updated introduction video for talent: ${savedProfile.talentProfileId}")
            TalentProfileResponse.fromDomain(savedProfile)
        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid introduction video data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating introduction video for user: $userId", e)
            throw RuntimeException("Failed to update introduction video: ${e.message}", e)
        }
    }
}
