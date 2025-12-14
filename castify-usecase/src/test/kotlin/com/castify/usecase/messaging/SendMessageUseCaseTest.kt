package com.castify.usecase.messaging

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import com.castify.usecase.dto.SendMessageRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class SendMessageUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var sendMessageUseCase: SendMessageUseCase

    @BeforeEach
    fun setUp() {
        conversationRepository = mockk()
        sendMessageUseCase = SendMessageUseCase(conversationRepository)
    }

    @Test
    fun `should successfully send message`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = UUID.randomUUID()
        )

        val request = SendMessageRequest(content = "Hello!")

        every { conversationRepository.findConversationById(conversationId) } returns conversation
        every { conversationRepository.saveMessage(any()) } answers { firstArg() }
        every { conversationRepository.saveConversation(any()) } answers { firstArg() }

        // When
        val result = sendMessageUseCase.execute(userId, conversationId, request)

        // Then
        assertNotNull(result)
        assertEquals("Hello!", result.content)
        assertEquals(userId, result.senderUserId)

        verify(exactly = 1) { conversationRepository.saveMessage(any()) }
        verify(exactly = 1) { conversationRepository.saveConversation(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when conversation not found`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val request = SendMessageRequest(content = "Hello!")

        every { conversationRepository.findConversationById(conversationId) } returns null

        // When & Then
        assertThrows<ResourceNotFoundException> {
            sendMessageUseCase.execute(userId, conversationId, request)
        }

        verify(exactly = 0) { conversationRepository.saveMessage(any()) }
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

        val request = SendMessageRequest(content = "Hello!")

        every { conversationRepository.findConversationById(conversationId) } returns conversation

        // When & Then
        assertThrows<UnauthorizedException> {
            sendMessageUseCase.execute(userId, conversationId, request)
        }
    }

    @Test
    fun `should throw IllegalArgumentException when content is null`() {
        // Given
        val userId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        val conversation = Conversation(
            conversationId = conversationId,
            talentUserId = userId,
            clientUserId = UUID.randomUUID()
        )

        val request = SendMessageRequest(content = null)

        every { conversationRepository.findConversationById(conversationId) } returns conversation

        // When & Then
        assertThrows<IllegalArgumentException> {
            sendMessageUseCase.execute(userId, conversationId, request)
        }
    }
}
