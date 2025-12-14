package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.CreateTalentProfileRequest
import com.castify.usecase.dto.LocationDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class CreateOrUpdateTalentProfileUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var userRepository: UserRepository
    private lateinit var createOrUpdateTalentProfileUseCase: CreateOrUpdateTalentProfileUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        userRepository = mockk()
        val profileEnricher = mockk<com.castify.usecase.talent.TalentProfileEnricher>(relaxed = true)
        createOrUpdateTalentProfileUseCase = CreateOrUpdateTalentProfileUseCase(talentRepository, userRepository, profileEnricher)
    }

    @Test
    fun `should successfully create new talent profile`() {
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

        val location = LocationDto(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val request = CreateTalentProfileRequest(
            fullName = "John Actor",
            bio = "Experienced actor",
            location = location,
            profileImageUrl = "https://example.com/profile.jpg"
        )

        every { userRepository.findById(userId) } returns user
        every { talentRepository.findByUserId(userId) } returns null
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = createOrUpdateTalentProfileUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("John Actor", result.fullName)
        assertEquals("Experienced actor", result.bio)
        assertEquals(LocationMode.SPECIFIC, result.location.mode)
        assertEquals("Los Angeles", result.location.city)

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should successfully update existing talent profile`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val user = User(
            userId = userId,
            email = "talent@example.com",
            role = UserRole.TALENT,
            phone = null,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val existingLocation = Location(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("40.7128"),
            longitude = BigDecimal("-74.0060"),
            city = "New York",
            country = "USA"
        )

        val existingProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Old Name",
            bio = "Old bio",
            location = existingLocation,
            profileImageUrl = null
        )

        val newLocation = LocationDto(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val request = CreateTalentProfileRequest(
            fullName = "Updated Name",
            bio = "Updated bio",
            location = newLocation,
            profileImageUrl = "https://example.com/new-profile.jpg"
        )

        every { userRepository.findById(userId) } returns user
        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = createOrUpdateTalentProfileUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Updated Name", result.fullName)
        assertEquals("Updated bio", result.bio)
        assertEquals("Los Angeles", result.location.city)

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when user not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = CreateTalentProfileRequest(
            fullName = "John Actor",
            bio = "Bio",
            location = LocationDto(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertEquals("User not found", exception.message)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw UnauthorizedException when user is not talent`() {
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

        val request = CreateTalentProfileRequest(
            fullName = "John Actor",
            bio = "Bio",
            location = LocationDto(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<UnauthorizedException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertEquals("Only talent users can create talent profiles", exception.message)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when fullName is null`() {
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

        val request = CreateTalentProfileRequest(
            fullName = null,
            bio = "Bio",
            location = LocationDto(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertEquals("Full name is required", exception.message)
    }

    @Test
    fun `should throw IllegalArgumentException when location is null`() {
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

        val request = CreateTalentProfileRequest(
            fullName = "John Actor",
            bio = "Bio",
            location = null,
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertEquals("Location is required", exception.message)
    }
}
