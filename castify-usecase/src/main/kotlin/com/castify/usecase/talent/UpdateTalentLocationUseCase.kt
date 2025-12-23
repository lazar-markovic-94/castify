package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdatePrimaryLocationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating a talent's primary location information.
 */
@Service
@Transactional
class UpdateTalentLocationUseCase(
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentLocationUseCase::class.java)

    /**
     * Updates the primary location for a talent profile.
     *
     * @param userId The user ID
     * @param request The primary location update request
     * @return The updated talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws IllegalArgumentException if primary location is missing
     */
    fun execute(userId: UUID, request: UpdatePrimaryLocationRequest): TalentProfileResponse {
        return try {
            logger.info("Updating primary location for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val primaryLocationDto = requireNotNull(request.primaryLocation) { "Primary location is required" }
            val updatedProfile = profile.updatePrimaryLocation(primaryLocationDto.toDomain())

            val savedProfile = talentRepository.save(updatedProfile)
            logger.info("Successfully updated primary location for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid primary location data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating primary location for user: $userId", e)
            throw RuntimeException("Failed to update primary location: ${e.message}", e)
        }
    }
}

