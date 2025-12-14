package com.castify.usecase.messaging

import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class GetConversationsUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var getConversationsUseCase: GetConversationsUseCase

    @BeforeEach
    fun setUp() {
        conversationRepository = mockk()
        getConversationsUseCase = GetConversationsUseCase(conversationRepository)
    }

    @Test
    fun `should successfully get all conversations for user`() {
        // Given
        val userId = UUID.randomUUID()

        val conversations = listOf(
            Conversation(
                conversationId = UUID.randomUUID(),
                talentUserId = userId,
                clientUserId = UUID.randomUUID()
            ),
            Conversation(
                conversationId = UUID.randomUUID(),
                talentUserId = UUID.randomUUID(),
                clientUserId = userId
            )
        )

        every { conversationRepository.findConversationsByUser(userId) } returns conversations

        // When
        val result = getConversationsUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)

        verify(exactly = 1) { conversationRepository.findConversationsByUser(userId) }
    }

    @Test
    fun `should handle empty conversations list`() {
        // Given
        val userId = UUID.randomUUID()

        every { conversationRepository.findConversationsByUser(userId) } returns emptyList()

        // When
        val result = getConversationsUseCase.execute(userId)

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}
