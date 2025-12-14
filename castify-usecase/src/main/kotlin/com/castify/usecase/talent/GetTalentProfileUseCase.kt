package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for retrieving talent profile information.
 */
@Service
@Transactional(readOnly = true)
class GetTalentProfileUseCase(
    private val talentRepository: TalentRepository,
    private val profileEnricher: TalentProfileEnricher
) {
    private val logger = LoggerFactory.getLogger(GetTalentProfileUseCase::class.java)

    /**
     * Retrieves a talent profile by its ID.
     *
     * @param talentProfileId The talent profile ID
     * @return The enriched talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun execute(talentProfileId: UUID): TalentProfileResponse {
        return try {
            logger.info("Fetching talent profile: $talentProfileId")

            val profile = talentRepository.findById(talentProfileId)
                ?: throw ResourceNotFoundException("Talent profile not found: $talentProfileId")

            profileEnricher.enrichProfile(profile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found: $talentProfileId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching talent profile: $talentProfileId", e)
            throw RuntimeException("Failed to fetch talent profile: ${e.message}", e)
        }
    }

    /**
     * Retrieves a talent profile by user ID.
     *
     * @param userId The user ID
     * @return The enriched talent profile response
     * @throws ResourceNotFoundException if the talent profile is not found
     */
    fun executeByUserId(userId: UUID): TalentProfileResponse {
        return try {
            logger.info("Fetching talent profile for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            profileEnricher.enrichProfile(profile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching talent profile for user: $userId", e)
            throw RuntimeException("Failed to fetch talent profile: ${e.message}", e)
        }
    }
}

