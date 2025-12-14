package com.castify.usecase.messaging

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.messaging.ConversationRepository
import com.castify.domain.messaging.Message
import com.castify.usecase.dto.MessageResponse
import com.castify.usecase.dto.SendMessageRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for sending a message in a conversation.
 */
@Service
@Transactional
class SendMessageUseCase(
    private val conversationRepository: ConversationRepository
) {
    private val logger = LoggerFactory.getLogger(SendMessageUseCase::class.java)

    /**
     * Sends a message in a conversation.
     *
     * @param userId The sender user ID
     * @param conversationId The conversation ID
     * @param request The message send request
     * @return Message response
     * @throws ResourceNotFoundException if conversation is not found
     * @throws UnauthorizedException if user is not a participant
     * @throws IllegalArgumentException if message content is missing
     */
    fun execute(userId: UUID, conversationId: UUID, request: SendMessageRequest): MessageResponse {
        return try {
            logger.info("Sending message in conversation: $conversationId from user: $userId")

            val conversation = conversationRepository.findConversationById(conversationId)
                ?: throw ResourceNotFoundException("Conversation not found: $conversationId")

            if (!conversation.isParticipant(userId)) {
                throw UnauthorizedException("You are not a participant in this conversation")
            }

            val content = requireNotNull(request.content) { "Message content is required" }

            val message = Message(
                messageId = UUID.randomUUID(),
                conversationId = conversationId,
                senderUserId = userId,
                content = content
            )

            val savedMessage = conversationRepository.saveMessage(message)
            logger.info("Successfully saved message: ${savedMessage.messageId}")

            // Update conversation's last message time
            val updatedConversation = conversation.updateLastMessageTime()
            conversationRepository.saveConversation(updatedConversation)
            logger.info("Updated conversation last message time: $conversationId")

            MessageResponse.fromDomain(savedMessage)

        } catch (e: ResourceNotFoundException) {
            logger.error("Conversation not found: $conversationId", e)
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized message send in conversation: $conversationId by user: $userId", e)
            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid message data for conversation: $conversationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error sending message in conversation: $conversationId", e)
            throw RuntimeException("Failed to send message: ${e.message}", e)
        }
    }
}
