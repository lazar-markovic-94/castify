package com.castify.usecase.messaging

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.messaging.ConversationRepository
import com.castify.usecase.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for retrieving messages from a conversation.
 */
@Service
@Transactional(readOnly = true)
class GetMessagesUseCase(
    private val conversationRepository: ConversationRepository
) {
    private val logger = LoggerFactory.getLogger(GetMessagesUseCase::class.java)

    /**
     * Retrieves all messages from a conversation.
     *
     * @param userId The current user ID
     * @param conversationId The conversation ID
     * @return List of message responses
     * @throws ResourceNotFoundException if conversation is not found
     * @throws UnauthorizedException if user is not a participant
     */
    fun execute(userId: UUID, conversationId: UUID): List<MessageResponse> {
        return try {
            logger.info("Fetching messages for conversation: $conversationId, user: $userId")

            val conversation = conversationRepository.findConversationById(conversationId)
                ?: throw ResourceNotFoundException("Conversation not found: $conversationId")

            if (!conversation.isParticipant(userId)) {
                throw UnauthorizedException("You are not a participant in this conversation")
            }

            val messages = conversationRepository.findMessagesByConversation(conversationId)
            logger.info("Found ${messages.size} messages in conversation: $conversationId")

            messages.map { MessageResponse.fromDomain(it) }

        } catch (e: ResourceNotFoundException) {
            logger.error("Conversation not found: $conversationId", e)
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized access to conversation: $conversationId by user: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching messages for conversation: $conversationId", e)
            throw RuntimeException("Failed to fetch messages: ${e.message}", e)
        }
    }
}
