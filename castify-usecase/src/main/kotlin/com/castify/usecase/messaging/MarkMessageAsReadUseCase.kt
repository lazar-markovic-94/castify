package com.castify.usecase.messaging

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.messaging.ConversationRepository
import com.castify.usecase.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for marking a message as read.
 */
@Service
@Transactional
class MarkMessageAsReadUseCase(
    private val conversationRepository: ConversationRepository
) {
    private val logger = LoggerFactory.getLogger(MarkMessageAsReadUseCase::class.java)

    /**
     * Marks a message as read.
     * Only marks the message as read if the current user is not the sender.
     *
     * @param userId The current user ID
     * @param conversationId The conversation ID
     * @param messageId The message ID to mark as read
     * @return Message response
     * @throws ResourceNotFoundException if conversation or message is not found
     * @throws UnauthorizedException if user is not a participant
     * @throws BadRequestException if message doesn't belong to the conversation
     */
    fun execute(userId: UUID, conversationId: UUID, messageId: UUID): MessageResponse {
        return try {
            logger.info("Marking message as read: $messageId in conversation: $conversationId by user: $userId")

            val conversation = conversationRepository.findConversationById(conversationId)
                ?: throw ResourceNotFoundException("Conversation not found: $conversationId")

            if (!conversation.isParticipant(userId)) {
                throw UnauthorizedException("You are not a participant in this conversation")
            }

            val message = conversationRepository.findMessageById(messageId)
                ?: throw ResourceNotFoundException("Message not found: $messageId")

            if (message.conversationId != conversationId) {
                throw BadRequestException("Message does not belong to this conversation")
            }

            // Only mark as read if the current user is not the sender
            if (message.senderUserId == userId) {
                logger.info("User is the sender, not marking as read: $messageId")
                return MessageResponse.fromDomain(message)
            }

            val updatedMessage = message.markAsRead()
            val savedMessage = conversationRepository.saveMessage(updatedMessage)
            logger.info("Successfully marked message as read: $messageId")

            MessageResponse.fromDomain(savedMessage)

        } catch (e: ResourceNotFoundException) {
            logger.error("Resource not found when marking message as read: $messageId", e)
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized access to mark message as read: $messageId by user: $userId", e)
            throw e
        } catch (e: BadRequestException) {
            logger.error("Message does not belong to conversation: $conversationId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error marking message as read: $messageId", e)
            throw RuntimeException("Failed to mark message as read: ${e.message}", e)
        }
    }
}
