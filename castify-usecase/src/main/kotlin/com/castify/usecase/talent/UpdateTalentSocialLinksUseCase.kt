package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.UpdateSocialLinksRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for updating a talent's social links.
 */
@Service
@Transactional
class UpdateTalentSocialLinksUseCase(
    private val talentRepository: TalentRepository
) {
    private val logger = LoggerFactory.getLogger(UpdateTalentSocialLinksUseCase::class.java)

    fun execute(userId: UUID, request: UpdateSocialLinksRequest): TalentProfileResponse {
        return try {
            logger.info("Updating social links for user: $userId")

            val profile = talentRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Talent profile not found for user: $userId")

            val socialLinksDto = requireNotNull(request.socialLinks) { "Social links are required" }
            val socialLinks = socialLinksDto.map { it.toDomain() }.toSet()

            var updatedProfile = profile
            for (link in socialLinks) {
                updatedProfile = updatedProfile.addSocialLink(link)
            }

            val savedProfile = talentRepository.save(updatedProfile)
            logger.info("Successfully updated social links for talent: ${savedProfile.talentProfileId}")

            TalentProfileResponse.fromDomain(savedProfile)

        } catch (e: ResourceNotFoundException) {
            logger.error("Talent profile not found for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid social links data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating social links for user: $userId", e)
            throw RuntimeException("Failed to update social links: ${e.message}", e)
        }
    }
}

