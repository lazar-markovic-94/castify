package com.castify.usecase.messaging

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import com.castify.domain.messaging.Message
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class MarkMessageAsReadUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var markMessageAsReadUseCase: MarkMessageAsReadUseCase

    @BeforeEach
    fun setUp() {
        conversationRepository = mockk()
        markMessageAsReadUseCase = MarkMessageAsReadUseCase(conversationRepository)
    }

    @Test
    fun `should successfully mark message as read`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val messageId = UUID.randomUUID()
        val senderUserId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = senderUserId
        )

        val message = Message(
            messageId = messageId,
            conversationId = conversationId,
            senderUserId = senderUserId,
            content = "Hello",
            sentAt = Instant.now(),
            isRead = false
        )

        every { conversationRepository.findConversationById(conversationId) } returns conversation
        every { conversationRepository.findMessageById(messageId) } returns message
        every { conversationRepository.saveMessage(any()) } answers { firstArg() }

        // When
        val result = markMessageAsReadUseCase.execute(userId, conversationId, messageId)

        // Then
        assertNotNull(result)
        verify(exactly = 1) { conversationRepository.saveMessage(any()) }
    }

    @Test
    fun `should not mark message as read when user is sender`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val messageId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = UUID.randomUUID()
        )

        val message = Message(
            messageId = messageId,
            conversationId = conversationId,
            senderUserId = userId,
            content = "Hello",
            sentAt = Instant.now(),
            isRead = false
        )

        every { conversationRepository.findConversationById(conversationId) } returns conversation
        every { conversationRepository.findMessageById(messageId) } returns message

        // When
        val result = markMessageAsReadUseCase.execute(userId, conversationId, messageId)

        // Then
        assertNotNull(result)
        verify(exactly = 0) { conversationRepository.saveMessage(any()) }
    }

    @Test
    fun `should throw BadRequestException when message does not belong to conversation`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val messageId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = UUID.randomUUID()
        )

        val message = Message(
            messageId = messageId,
            conversationId = UUID.randomUUID(),
            senderUserId = UUID.randomUUID(),
            content = "Hello",
            sentAt = Instant.now(),
            isRead = false
        )

        every { conversationRepository.findConversationById(conversationId) } returns conversation
        every { conversationRepository.findMessageById(messageId) } returns message

        // When & Then
        assertThrows<BadRequestException> {
            markMessageAsReadUseCase.execute(userId, conversationId, messageId)
        }
    }
}
