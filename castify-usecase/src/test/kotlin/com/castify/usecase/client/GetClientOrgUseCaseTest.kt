package com.castify.usecase.client

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.ClientOrgRepository
import com.castify.domain.client.OrgType
import com.castify.domain.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class GetClientOrgUseCaseTest {

    private lateinit var clientOrgRepository: ClientOrgRepository
    private lateinit var getClientOrgUseCase: GetClientOrgUseCase

    @BeforeEach
    fun setUp() {
        clientOrgRepository = mockk()
        getClientOrgUseCase = GetClientOrgUseCase(clientOrgRepository)
    }

    @Test
    fun `should successfully get client org by id`() {
        // Given
        val clientOrgId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val clientOrg = ClientOrg(
            clientOrgId = clientOrgId,
            userId = userId,
            orgName = "Acme Productions",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = "Film production company",
            website = "https://acme.com",
            contactEmail = "contact@acme.com",
            contactPhone = "+1234567890",
            address = "123 Main St, Hollywood, CA",
            logoUrl = "https://acme.com/logo.png"
        )

        every { clientOrgRepository.findById(clientOrgId) } returns clientOrg

        // When
        val result = getClientOrgUseCase.execute(clientOrgId)

        // Then
        assertNotNull(result)
        assertEquals(clientOrgId, result.clientOrgId)
        assertEquals("Acme Productions", result.orgName)
        assertEquals(OrgType.PRODUCTION_COMPANY, result.orgType)
        assertEquals("Film production company", result.description)

        verify(exactly = 1) { clientOrgRepository.findById(clientOrgId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when org not found by id`() {
        // Given
        val clientOrgId = UUID.randomUUID()

        every { clientOrgRepository.findById(clientOrgId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getClientOrgUseCase.execute(clientOrgId)
        }

        assertTrue(exception.message!!.contains("Client organization not found"))
        verify(exactly = 1) { clientOrgRepository.findById(clientOrgId) }
    }

    @Test
    fun `should successfully get client org by user id`() {
        // Given
        val clientOrgId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val clientOrg = ClientOrg(
            clientOrgId = clientOrgId,
            userId = userId,
            orgName = "Acme Productions",
            orgType = OrgType.PRODUCTION_COMPANY,
            description = "Film production company",
            website = "https://acme.com",
            contactEmail = "contact@acme.com",
            contactPhone = "+1234567890",
            address = "123 Main St, Hollywood, CA",
            logoUrl = "https://acme.com/logo.png"
        )

        every { clientOrgRepository.findByUserId(userId) } returns clientOrg

        // When
        val result = getClientOrgUseCase.executeByUserId(userId)

        // Then
        assertNotNull(result)
        assertEquals(clientOrgId, result.clientOrgId)
        assertEquals(userId, result.userId)
        assertEquals("Acme Productions", result.orgName)

        verify(exactly = 1) { clientOrgRepository.findByUserId(userId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when org not found by user id`() {
        // Given
        val userId = UUID.randomUUID()

        every { clientOrgRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getClientOrgUseCase.executeByUserId(userId)
        }

        assertTrue(exception.message!!.contains("Client organization not found for user"))
        verify(exactly = 1) { clientOrgRepository.findByUserId(userId) }
    }

    @Test
    fun `should handle org with minimal information`() {
        // Given
        val clientOrgId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val clientOrg = ClientOrg(
            clientOrgId = clientOrgId,
            userId = userId,
            orgName = "Basic Org",
            orgType = OrgType.INDIVIDUAL,
            description = null,
            website = null,
            contactEmail = null,
            contactPhone = null,
            address = null,
            logoUrl = null
        )

        every { clientOrgRepository.findById(clientOrgId) } returns clientOrg

        // When
        val result = getClientOrgUseCase.execute(clientOrgId)

        // Then
        assertNotNull(result)
        assertEquals("Basic Org", result.orgName)
        assertEquals(OrgType.INDIVIDUAL, result.orgType)
        assertNull(result.description)
        assertNull(result.website)
    }
}
