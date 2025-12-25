package com.castify.usecase.talent

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ForbiddenException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.CreateTalentProfileRequest
import com.castify.usecase.dto.TalentProfileResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating or updating a talent profile.
 * Handles profile creation for new talents and updates for existing profiles.
 */
@Service
@Transactional
class CreateOrUpdateTalentProfileUseCase(
    private val talentRepository: TalentRepository,
    private val userRepository: UserRepository,
    private val profileEnricher: TalentProfileEnricher,
    private val asyncStatisticsUpdateService: com.castify.usecase.upload.AsyncStatisticsUpdateService
) {
    private val logger = LoggerFactory.getLogger(CreateOrUpdateTalentProfileUseCase::class.java)

    /**
     * Creates a new talent profile or updates an existing one.
     *
     * @param userId The ID of the user creating/updating the profile
     * @param request The profile creation/update request
     * @return The created/updated talent profile response
     * @throws ResourceNotFoundException if the user is not found
     * @throws ForbiddenException if the user is not a talent user
     * @throws BadRequestException if required fields are missing
     */
    fun execute(userId: UUID, request: CreateTalentProfileRequest): TalentProfileResponse {
        logger.info("Creating/updating talent profile for user: $userId")

        // Validate user exists and has TALENT role
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User with ID $userId not found")

        if (user.role != UserRole.TALENT) {
            throw ForbiddenException("Only talent users can create talent profiles. User role: ${user.role}")
        }

        // Validate required fields
        val fullName = request.fullName
            ?: throw BadRequestException("Full name is required")
        val locationMode = request.locationMode ?: com.castify.domain.talent.LocationMode.SPECIFIC
        val primaryLocationDto = request.primaryLocation
        val geoAreaDtos = request.geoAreas ?: emptySet()

        // Check if profile exists
        val existingProfile = talentRepository.findByUserId(userId)

        val profile = if (existingProfile != null) {
            logger.info("Updating existing talent profile: ${existingProfile.talentProfileId}")
            existingProfile.copy(
                fullName = fullName,
                bio = request.bio,
                locationMode = locationMode,
                primaryLocation = primaryLocationDto?.toDomain(),
                geoAreas = geoAreaDtos.map { it.toDomain() }.toSet(),
                profileImageUrl = request.profileImageUrl
            )
        } else {
            logger.info("Creating new talent profile for user: $userId")
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = userId,
                fullName = fullName,
                bio = request.bio,
                locationMode = locationMode,
                primaryLocation = primaryLocationDto?.toDomain(),
                geoAreas = geoAreaDtos.map { it.toDomain() }.toSet(),
                profileImageUrl = request.profileImageUrl
            )
        }

        val savedProfile = talentRepository.save(profile)
        logger.info("Successfully saved talent profile: ${savedProfile.talentProfileId}")

        // Update profile completeness statistics asynchronously
        asyncStatisticsUpdateService.updateProfileCompleteness(savedProfile.talentProfileId)

        return profileEnricher.enrichProfile(savedProfile)
    }

    /**
     * Updates only the profile image URL for a talent.
     *
     * @param userId The ID of the user updating their image
     * @param imageUrl The new image URL
     * @return The updated talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun updateProfileImage(userId: UUID, imageUrl: String): TalentProfileResponse {
        logger.info("Updating profile image for user: $userId")

        val existingProfile = talentRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

        val updatedProfile = existingProfile.copy(profileImageUrl = imageUrl)
        val savedProfile = talentRepository.save(updatedProfile)

        logger.info("Successfully updated profile image for talent: ${savedProfile.talentProfileId}")

        // Update profile completeness statistics asynchronously
        asyncStatisticsUpdateService.updateProfileCompleteness(savedProfile.talentProfileId)

        return profileEnricher.enrichProfile(savedProfile)
    }
}

