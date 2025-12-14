package com.castify.usecase.client

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.ClientOrgRepository
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.ClientOrgResponse
import com.castify.usecase.dto.CreateClientOrgRequest
import com.castify.usecase.dto.UpdateClientOrgRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating or updating a client organization.
 */
@Service
@Transactional
class CreateOrUpdateClientOrgUseCase(
    private val clientOrgRepository: ClientOrgRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(CreateOrUpdateClientOrgUseCase::class.java)

    /**
     * Creates a new client organization or updates existing one.
     *
     * @param userId The user ID
     * @param request The client organization creation request
     * @return Client organization response
     * @throws ResourceNotFoundException if user is not found
     * @throws UnauthorizedException if user is not a CLIENT
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(userId: UUID, request: CreateClientOrgRequest): ClientOrgResponse {
        return try {
            logger.info("Creating/updating client organization for user: $userId")

            val user = userRepository.findById(userId)
                ?: throw ResourceNotFoundException("User not found: $userId")

            if (user.role != UserRole.CLIENT) {
                throw UnauthorizedException("Only client users can create client organizations")
            }

            val orgName = requireNotNull(request.orgName) { "Organization name is required" }
            val orgType = requireNotNull(request.orgType) { "Organization type is required" }

            val existingOrg = clientOrgRepository.findByUserId(userId)

            val clientOrg = if (existingOrg != null) {
                logger.info("Updating existing client organization: ${existingOrg.clientOrgId}")
                existingOrg.copy(
                    orgName = orgName,
                    orgType = orgType,
                    description = request.description,
                    website = request.website,
                    contactEmail = request.contactEmail,
                    contactPhone = request.contactPhone,
                    address = request.address,
                    logoUrl = request.logoUrl
                )
            } else {
                logger.info("Creating new client organization for user: $userId")
                ClientOrg(
                    clientOrgId = UUID.randomUUID(),
                    userId = userId,
                    orgName = orgName,
                    orgType = orgType,
                    description = request.description,
                    website = request.website,
                    contactEmail = request.contactEmail,
                    contactPhone = request.contactPhone,
                    address = request.address,
                    logoUrl = request.logoUrl
                )
            }

            val savedOrg = clientOrgRepository.save(clientOrg)
            logger.info("Successfully saved client organization: ${savedOrg.clientOrgId}")

            ClientOrgResponse.fromDomain(savedOrg)

        } catch (e: ResourceNotFoundException) {
            logger.error("User not found: $userId", e)
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized client organization creation for user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid client organization data for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error creating/updating client organization for user: $userId", e)
            throw RuntimeException("Failed to create/update client organization: ${e.message}", e)
        }
    }

    /**
     * Updates an existing client organization.
     *
     * @param userId The user ID
     * @param request The client organization update request
     * @return Updated client organization response
     * @throws ResourceNotFoundException if client organization is not found
     */
    fun update(userId: UUID, request: UpdateClientOrgRequest): ClientOrgResponse {
        return try {
            logger.info("Updating client organization for user: $userId")

            val existingOrg = clientOrgRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Client organization not found for user: $userId")

            var updatedOrg = existingOrg

            request.orgName?.let { updatedOrg = updatedOrg.copy(orgName = it) }
            request.orgType?.let { updatedOrg = updatedOrg.copy(orgType = it) }
            request.description?.let { updatedOrg = updatedOrg.updateDescription(it) }
            request.website?.let { updatedOrg = updatedOrg.updateWebsite(it) }
            request.contactEmail?.let { updatedOrg = updatedOrg.updateContactInfo(it, updatedOrg.contactPhone) }
            request.contactPhone?.let { updatedOrg = updatedOrg.updateContactInfo(updatedOrg.contactEmail, it) }
            request.address?.let { updatedOrg = updatedOrg.updateAddress(it) }
            request.logoUrl?.let { updatedOrg = updatedOrg.updateLogoUrl(it) }

            val savedOrg = clientOrgRepository.save(updatedOrg)
            logger.info("Successfully updated client organization: ${savedOrg.clientOrgId}")

            ClientOrgResponse.fromDomain(savedOrg)

        } catch (e: ResourceNotFoundException) {
            logger.error("Client organization not found for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error updating client organization for user: $userId", e)
            throw RuntimeException("Failed to update client organization: ${e.message}", e)
        }
    }
}
