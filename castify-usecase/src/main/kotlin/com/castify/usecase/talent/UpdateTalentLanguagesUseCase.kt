package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdateLanguagesRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating a talent's languages.
 */
@Service
@Transactional
class UpdateTalentLanguagesUseCase(
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentLanguagesUseCase::class.java)

    fun execute(userId: UUID, request: UpdateLanguagesRequest): TalentProfileResponse {
        return try {
            logger.info("Updating languages for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val languages = requireNotNull(request.languages) { "Languages are required" }
            val updatedProfile = profile.updateLanguages(languages)

            val savedProfile = talentRepository.save(updatedProfile)
            logger.info("Successfully updated languages for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid languages data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating languages for user: $userId", e)
            throw RuntimeException("Failed to update languages: ${e.message}", e)
        }
    }
}

