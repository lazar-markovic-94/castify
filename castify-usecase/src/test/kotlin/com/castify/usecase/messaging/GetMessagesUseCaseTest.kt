package com.castify.usecase.messaging

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

class GetMessagesUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var getMessagesUseCase: GetMessagesUseCase

    @BeforeEach
    fun setUp() {
        conversationRepository = mockk()
        getMessagesUseCase = GetMessagesUseCase(conversationRepository)
    }

    @Test
    fun `should successfully get messages for participant`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = UUID.randomUUID()
        )

        val messages = listOf(
            Message(
                messageId = UUID.randomUUID(),
                conversationId = conversationId,
                senderUserId = userId,
                content = "Hello",
                sentAt = Instant.now(),
                isRead = false
            )
        )

        every { conversationRepository.findConversationById(conversationId) } returns conversation
        every { conversationRepository.findMessagesByConversation(conversationId) } returns messages

        // When
        val result = getMessagesUseCase.execute(userId, conversationId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("Hello", result[0].content)
    }

    @Test
    fun `should throw ResourceNotFoundException when conversation not found`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        every { conversationRepository.findConversationById(conversationId) } returns null

        // When & Then
        assertThrows<ResourceNotFoundException> {
            getMessagesUseCase.execute(userId, conversationId)
        }

        verify(exactly = 0) { conversationRepository.findMessagesByConversation(any()) }
    }

    @Test
    fun `should throw UnauthorizedException when user is not participant`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = UUID.randomUUID(),
            clientUserId = UUID.randomUUID()
        )

        every { conversationRepository.findConversationById(conversationId) } returns conversation

        // When & Then
        assertThrows<UnauthorizedException> {
            getMessagesUseCase.execute(userId, conversationId)
        }
    }
}
