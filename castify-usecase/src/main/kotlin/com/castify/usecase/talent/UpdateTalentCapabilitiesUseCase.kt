package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdateCapabilitiesRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating a talent's capabilities.
 */
@Service
@Transactional
class UpdateTalentCapabilitiesUseCase(
    private val talentRepository: TalentRepository,
    private val asyncStatisticsUpdateService: com.castify.usecase.upload.AsyncStatisticsUpdateService
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentCapabilitiesUseCase::class.java)

    fun execute(userId: UUID, request: UpdateCapabilitiesRequest): TalentProfileResponse {
        return try {
            logger.info("Updating capabilities for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val updatedProfile = profile.updateCapabilities(request.toDomain())

            val savedProfile = talentRepository.save(updatedProfile)
            logger.info("Successfully updated capabilities for talent: ${savedProfile.talentProfileId}")

            // Update profile completeness statistics in a separate transaction
            // This is called on a separate service to ensure proper Spring AOP proxy interception
            asyncStatisticsUpdateService.updateProfileCompleteness(savedProfile.talentProfileId)

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating capabilities for user: $userId", e)
            throw RuntimeException("Failed to update capabilities: ${e.message}", e)
        }
    }
}

