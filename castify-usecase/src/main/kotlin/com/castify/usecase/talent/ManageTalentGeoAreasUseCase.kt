package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.GeoArea
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.AddGeoAreaRequest
import com.castify.usecase.dto.RemoveGeoAreaRequest
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdateGeoAreasRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for managing a talent's geographical service areas.
 * Supports adding, removing, and bulk updating geo areas.
 */
@Service
@Transactional
class ManageTalentGeoAreasUseCase(
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(ManageTalentGeoAreasUseCase::class.java)

    /**
     * Adds a new geographical area to a talent's service coverage.
     *
     * @param userId The user ID
     * @param request The add geo area request
     * @return The updated talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws IllegalArgumentException if the geo area already exists or data is invalid
     */
    fun addGeoArea(userId: UUID, request: AddGeoAreaRequest): TalentProfileResponse {
        return try {
            logger.info("Adding geo area for user: $userId - ${request.city}, ${request.country}")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val city = requireNotNull(request.city) { "City is required" }
            val country = requireNotNull(request.country) { "Country is required" }

            val geoArea = GeoArea(
                city = city,
                country = country,
                isPrimary = request.isPrimary
            )

            val updatedProfile = profile.addGeoArea(geoArea)
            val savedProfile = talentRepository.save(updatedProfile)

            logger.info("Successfully added geo area for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid geo area data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error adding geo area for user: $userId", e)
            throw RuntimeException("Failed to add geo area: ${e.message}", e)
        }
    }

    /**
     * Removes a geographical area from a talent's service coverage.
     *
     * @param userId The user ID
     * @param request The remove geo area request
     * @return The updated talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun removeGeoArea(userId: UUID, request: RemoveGeoAreaRequest): TalentProfileResponse {
        return try {
            logger.info("Removing geo area for user: $userId - ${request.city}, ${request.country}")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val city = requireNotNull(request.city) { "City is required" }
            val country = requireNotNull(request.country) { "Country is required" }

            val updatedProfile = profile.removeGeoArea(city, country)
            val savedProfile = talentRepository.save(updatedProfile)

            logger.info("Successfully removed geo area for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid geo area data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error removing geo area for user: $userId", e)
            throw RuntimeException("Failed to remove geo area: ${e.message}", e)
        }
    }

    /**
     * Updates all geographical areas for a talent profile (bulk update).
     *
     * @param userId The user ID
     * @param request The update geo areas request
     * @return The updated talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     * @throws IllegalArgumentException if data is invalid
     */
    fun updateGeoAreas(userId: UUID, request: UpdateGeoAreasRequest): TalentProfileResponse {
        return try {
            logger.info("Updating geo areas for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val geoAreaDtos = requireNotNull(request.geoAreas) { "Geo areas are required" }
            val geoAreas = geoAreaDtos.map { it.toDomain() }.toSet()

            val updatedProfile = profile.updateGeoAreas(geoAreas)
            val savedProfile = talentRepository.save(updatedProfile)

            logger.info("Successfully updated geo areas for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid geo areas data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating geo areas for user: $userId", e)
            throw RuntimeException("Failed to update geo areas: ${e.message}", e)
        }
    }
}
