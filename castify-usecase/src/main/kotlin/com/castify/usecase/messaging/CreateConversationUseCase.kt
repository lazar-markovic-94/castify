package com.castify.usecase.messaging

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.ConversationResponse
import com.castify.usecase.dto.CreateConversationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for creating a new conversation between a talent and a client.
 */
@Service
@Transactional
class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(CreateConversationUseCase::class.java)

    /**
     * Creates a new conversation between a talent and a client.
     * If a conversation already exists between these users, returns the existing conversation.
     *
     * @param currentUserId The current user ID initiating the conversation
     * @param request The conversation creation request
     * @return Conversation response
     * @throws ResourceNotFoundException if either user is not found
     * @throws BadRequestException if users don't have correct roles
     * @throws IllegalArgumentException if required fields are missing
     */
    fun execute(currentUserId: UUID, request: CreateConversationRequest): ConversationResponse {
        return try {
            val talentUserId = requireNotNull(request.talentUserId) { "Talent user ID is required" }
            val clientUserId = requireNotNull(request.clientUserId) { "Client user ID is required" }

            logger.info("Creating conversation between talent: $talentUserId and client: $clientUserId")

            // Verify both users exist and have correct roles
            val talentUser = userRepository.findById(talentUserId)
                ?: throw ResourceNotFoundException("Talent user not found: $talentUserId")
            val clientUser = userRepository.findById(clientUserId)
                ?: throw ResourceNotFoundException("Client user not found: $clientUserId")

            if (talentUser.role != UserRole.TALENT) {
                throw BadRequestException("First user must be a talent (userId: $talentUserId)")
            }
            if (clientUser.role != UserRole.CLIENT) {
                throw BadRequestException("Second user must be a client (userId: $clientUserId)")
            }

            // Check if conversation already exists
            val existingConversation = conversationRepository.findConversationBetweenUsers(
                talentUserId, clientUserId
            )
            if (existingConversation != null) {
                logger.info("Conversation already exists: ${existingConversation.conversationId}")
                return ConversationResponse.fromDomain(existingConversation)
            }

            val conversation = Conversation(
                conversationId = UUID.randomUUID(),
                talentUserId = talentUserId,
                clientUserId = clientUserId
            )

            val savedConversation = conversationRepository.saveConversation(conversation)
            logger.info("Successfully created conversation: ${savedConversation.conversationId}")

            ConversationResponse.fromDomain(savedConversation)

        } catch (e: ResourceNotFoundException) {
            logger.error("User not found during conversation creation", e)
            throw e
        } catch (e: BadRequestException) {
            logger.error("Invalid user roles for conversation creation", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid conversation creation data", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error creating conversation", e)
            throw RuntimeException("Failed to create conversation: ${e.message}", e)
        }
    }
}
