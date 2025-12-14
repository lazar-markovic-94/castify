package com.castify.usecase.client

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.ClientOrgRepository
import com.castify.domain.client.OrgType
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.CreateClientOrgRequest
import com.castify.usecase.dto.UpdateClientOrgRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class CreateOrUpdateClientOrgUseCaseTest {

    private lateinit var clientOrgRepository: ClientOrgRepository
    private lateinit var userRepository: UserRepository
    private lateinit var createOrUpdateClientOrgUseCase: CreateOrUpdateClientOrgUseCase

    @BeforeEach
    fun setUp() {
        clientOrgRepository = mockk()
        userRepository = mockk()
        createOrUpdateClientOrgUseCase = CreateOrUpdateClientOrgUseCase(clientOrgRepository, userRepository)
    }

    @Test
    fun `should successfully create new client organization`() {
        // Given
        val userId = UUID.randomUUID()
        val user = User(
            userId = userId,
            email = "client@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = CreateClientOrgRequest(
            orgName = "Acme Productions",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = "Film production company",
            website = "https://acme.com",
            contactEmail = "contact@acme.com",
            contactPhone = "+1234567890",
            address = "123 Main St, Hollywood, CA",
            logoUrl = "https://acme.com/logo.png"
        )

        every { userRepository.findById(userId) } returns user
        every { clientOrgRepository.findByUserId(userId) } returns null
        every { clientOrgRepository.save(any()) } answers { firstArg() }

        // When
        val result = createOrUpdateClientOrgUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Acme Productions", result.orgName)
        assertEquals(OrgType.PRODUCTION_COMPANY, result.orgType)
        assertEquals("Film production company", result.description)

        verify(exactly = 1) { clientOrgRepository.save(any()) }
    }

    @Test
    fun `should successfully update existing client organization`() {
        // Given
        val userId = UUID.randomUUID()
        val clientOrgId = UUID.randomUUID()

        val user = User(
            userId = userId,
            email = "client@example.com",
            role = UserRole.CLIENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val existingOrg = ClientOrg(
            clientOrgId = clientOrgId,
            userId = userId,
            orgName = "Old Name",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = "Old description",
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        val request = CreateClientOrgRequest(
            orgName = "New Name",
            orgType = OrgType.AGENCY,
            description = "New description",
            website = "https://newsite.com",
            contactEmail = "new@example.com",
            contactPhone = "+9999999999",
            address = "New Address",
            logoUrl = "https://newsite.com/logo.png"
        )

        every { userRepository.findById(userId) } returns user
        every { clientOrgRepository.findByUserId(userId) } returns existingOrg
        every { clientOrgRepository.save(any()) } answers { firstArg() }

        // When
        val result = createOrUpdateClientOrgUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("New Name", result.orgName)
        assertEquals(OrgType.AGENCY, result.orgType)
        assertEquals("New description", result.description)

        verify(exactly = 1) { clientOrgRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when user not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = CreateClientOrgRequest(
            orgName = "Test Org",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = null,
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        every { userRepository.findById(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createOrUpdateClientOrgUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("User not found"))
        verify(exactly = 0) { clientOrgRepository.save(any()) }
    }

    @Test
    fun `should throw UnauthorizedException when user is not CLIENT`() {
        // Given
        val userId = UUID.randomUUID()
        val user = User(
            userId = userId,
            email = "talent@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = CreateClientOrgRequest(
            orgName = "Test Org",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = null,
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<UnauthorizedException> {
            createOrUpdateClientOrgUseCase.execute(userId, request)
        }

        assertEquals("Only client users can create client organizations", exception.message)
        verify(exactly = 0) { clientOrgRepository.save(any()) }
    }

    @Test
    fun `should successfully update organization using update method`() {
        // Given
        val userId = UUID.randomUUID()
        val clientOrgId = UUID.randomUUID()

        val existingOrg = ClientOrg(
            clientOrgId = clientOrgId,
            userId = userId,
            orgName = "Old Name",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = "Old description",
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        val request = UpdateClientOrgRequest(
            orgName = "Updated Name",
            orgType = null,
            description = "Updated description",
            website = "https://updated.com",
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        every { clientOrgRepository.findByUserId(userId) } returns existingOrg
        every { clientOrgRepository.save(any()) } answers { firstArg() }

        // When
        val result = createOrUpdateClientOrgUseCase.update(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Updated Name", result.orgName)
        assertEquals("Updated description", result.description)
        assertEquals("https://updated.com", result.website)

        verify(exactly = 1) { clientOrgRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when updating non-existent org`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdateClientOrgRequest(
            orgName = "Updated Name",
            orgType = null,
            description = null,
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        every { clientOrgRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createOrUpdateClientOrgUseCase.update(userId, request)
        }

        assertTrue(exception.message!!.contains("Client organization not found"))
        verify(exactly = 0) { clientOrgRepository.save(any()) }
    }
}
