package com.castify.usecase.messaging

import com.castify.domain.messaging.ConversationRepository
import com.castify.usecase.dto.ConversationResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use case for retrieving all conversations for a user.
 */
@Service
@Transactional(readOnly = true)
class GetConversationsUseCase(
    private val conversationRepository: ConversationRepository
) {
    private val logger = LoggerFactory.getLogger(GetConversationsUseCase::class.java)

    /**
     * Retrieves all conversations for a user.
     *
     * @param userId The user ID
     * @return List of conversation responses
     */
    fun execute(userId: UUID): List<ConversationResponse> {
        return try {
            logger.info("Fetching conversations for user: $userId")

            val conversations = conversationRepository.findConversationsByUser(userId)
            logger.info("Found ${conversations.size} conversations for user: $userId")

            conversations.map { ConversationResponse.fromDomain(it) }

        } catch (e: Exception) {
            logger.error("Error fetching conversations for user: $userId", e)
            throw RuntimeException("Failed to fetch conversations: ${e.message}", e)
        }
    }
}
