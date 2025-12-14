package com.castify.usecase.messaging

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.messaging.Conversation
import com.castify.domain.messaging.ConversationRepository
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.CreateConversationRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class CreateConversationUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var createConversationUseCase: CreateConversationUseCase

    @BeforeEach
    fun setUp() {
        conversationRepository = mockk()
        userRepository = mockk()
        createConversationUseCase = CreateConversationUseCase(conversationRepository, userRepository)
    }

    @Test
    fun `should successfully create new conversation`() {
        // Given
        val currentUserId = UUID.randomUUID()
        val talentUserId = UUID.randomUUID()
        val clientUserId = UUID.randomUUID()

        val talentUser = User(
            userId = talentUserId,
            email = "talent@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val clientUser = User(
            userId = clientUserId,
            email = "client@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = CreateConversationRequest(
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        every { userRepository.findById(talentUserId) } returns talentUser
        every { userRepository.findById(clientUserId) } returns clientUser
        every { conversationRepository.findConversationBetweenUsers(talentUserId, clientUserId) } returns null
        every { conversationRepository.saveConversation(any()) } answers { firstArg() }

        // When
        val result = createConversationUseCase.execute(currentUserId, request)

        // Then
        assertNotNull(result)
        assertEquals(talentUserId, result.talentUserId)
        assertEquals(clientUserId, result.clientUserId)

        verify(exactly = 1) { conversationRepository.saveConversation(any()) }
    }

    @Test
    fun `should return existing conversation if already exists`() {
        // Given
        val currentUserId = UUID.randomUUID()
        val talentUserId = UUID.randomUUID()
        val clientUserId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()

        val talentUser = User(
            userId = talentUserId,
            email = "talent@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val clientUser = User(
            userId = clientUserId,
            email = "client@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val existingConversation = Conversation(
            conversationId = conversationId,
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        val request = CreateConversationRequest(
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        every { userRepository.findById(talentUserId) } returns talentUser
        every { userRepository.findById(clientUserId) } returns clientUser
        every { conversationRepository.findConversationBetweenUsers(talentUserId, clientUserId) } returns existingConversation

        // When
        val result = createConversationUseCase.execute(currentUserId, request)

        // Then
        assertNotNull(result)
        assertEquals(conversationId, result.conversationId)

        verify(exactly = 0) { conversationRepository.saveConversation(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when talent user not found`() {
        // Given
        val currentUserId = UUID.randomUUID()
        val talentUserId = UUID.randomUUID()
        val clientUserId = UUID.randomUUID()

        val request = CreateConversationRequest(
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        every { userRepository.findById(talentUserId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createConversationUseCase.execute(currentUserId, request)
        }

        assertTrue(exception.message!!.contains("Talent user not found"))
        verify(exactly = 0) { conversationRepository.saveConversation(any()) }
    }

    @Test
    fun `should throw BadRequestException when first user is not talent`() {
        // Given
        val currentUserId = UUID.randomUUID()
        val talentUserId = UUID.randomUUID()
        val clientUserId = UUID.randomUUID()

        val notTalentUser = User(
            userId = talentUserId,
            email = "client@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val clientUser = User(
            userId = clientUserId,
            email = "another@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = CreateConversationRequest(
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        every { userRepository.findById(talentUserId) } returns notTalentUser
        every { userRepository.findById(clientUserId) } returns clientUser

        // When & Then
        val exception = assertThrows<BadRequestException> {
            createConversationUseCase.execute(currentUserId, request)
        }

        assertTrue(exception.message!!.contains("First user must be a talent"))
        verify(exactly = 0) { conversationRepository.saveConversation(any()) }
    }

    @Test
    fun `should throw BadRequestException when second user is not client`() {
        // Given
        val currentUserId = UUID.randomUUID()
        val talentUserId = UUID.randomUUID()
        val clientUserId = UUID.randomUUID()

        val talentUser = User(
            userId = talentUserId,
            email = "talent@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val notClientUser = User(
            userId = clientUserId,
            email = "another@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = CreateConversationRequest(
            talentUserId = talentUserId,
            clientUserId = clientUserId
        )

        every { userRepository.findById(talentUserId) } returns talentUser
        every { userRepository.findById(clientUserId) } returns notClientUser

        // When & Then
        val exception = assertThrows<BadRequestException> {
            createConversationUseCase.execute(currentUserId, request)
        }

        assertTrue(exception.message!!.contains("Second user must be a client"))
        verify(exactly = 0) { conversationRepository.saveConversation(any()) }
    }
}
