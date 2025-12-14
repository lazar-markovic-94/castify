package com.castify.usecase.client

import com.castify.domain.client.ClientOrgRepository
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.usecase.dto.ClientOrgResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for retrieving client organization information.
 */
@Service
@Transactional(readOnly = true)
class GetClientOrgUseCase(
    private val clientOrgRepository: ClientOrgRepository
) {
    private val logger = LoggerFactory.getLogger(GetClientOrgUseCase::class.java)

    /**
     * Retrieves a client organization by its ID.
     *
     * @param clientOrgId The client organization ID
     * @return Client organization response
     * @throws ResourceNotFoundException if client organization is not found
     */
    fun execute(clientOrgId: UUID): ClientOrgResponse {
        return try {
            logger.info("Fetching client organization: $clientOrgId")

            val clientOrg = clientOrgRepository.findById(clientOrgId)
                ?: throw ResourceNotFoundException("Client organization not found: $clientOrgId")

            ClientOrgResponse.fromDomain(clientOrg)

        } catch (e: ResourceNotFoundException) {
            logger.error("Client organization not found: $clientOrgId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching client organization: $clientOrgId", e)
            throw RuntimeException("Failed to fetch client organization: ${e.message}", e)
        }
    }

    /**
     * Retrieves a client organization by user ID.
     *
     * @param userId The user ID
     * @return Client organization response
     * @throws ResourceNotFoundException if client organization is not found for the user
     */
    fun executeByUserId(userId: UUID): ClientOrgResponse {
        return try {
            logger.info("Fetching client organization for user: $userId")

            val clientOrg = clientOrgRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Client organization not found for user: $userId")

            ClientOrgResponse.fromDomain(clientOrg)

        } catch (e: ResourceNotFoundException) {
            logger.error("Client organization not found for user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching client organization for user: $userId", e)
            throw RuntimeException("Failed to fetch client organization: ${e.message}", e)
        }
    }
}
