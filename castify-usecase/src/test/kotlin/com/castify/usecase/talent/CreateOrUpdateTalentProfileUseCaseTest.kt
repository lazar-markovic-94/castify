package com.castify.usecase.talent

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ForbiddenException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.GeoArea
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.PrimaryLocation
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.domain.user.User
import com.castify.domain.user.UserRepository
import com.castify.domain.user.UserRole
import com.castify.usecase.dto.CreateTalentProfileRequest
import com.castify.usecase.dto.GeoAreaDto
import com.castify.usecase.dto.PrimaryLocationDto
import com.castify.usecase.dto.TalentProfileResponse
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
    private lateinit var profileEnricher: TalentProfileEnricher
    private lateinit var asyncStatisticsUpdateService: com.castify.usecase.upload.AsyncStatisticsUpdateService
    private lateinit var createOrUpdateTalentProfileUseCase: CreateOrUpdateTalentProfileUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        userRepository = mockk()
        profileEnricher = mockk()
        asyncStatisticsUpdateService = mockk(relaxed = true)
        createOrUpdateTalentProfileUseCase = CreateOrUpdateTalentProfileUseCase(talentRepository, userRepository, profileEnricher, asyncStatisticsUpdateService)
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

        val primaryLocation = PrimaryLocationDto(
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val geoAreas = setOf(
            GeoAreaDto(city = "Los Angeles", country = "USA", isPrimary = true)
        )

        val request = CreateTalentProfileRequest(
            fullName = "John Actor",
            bio = "Experienced actor",
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = primaryLocation,
            geoAreas = geoAreas,
            profileImageUrl = "https://example.com/profile.jpg"
        )

        every { userRepository.findById(userId) } returns user
        every { talentRepository.findByUserId(userId) } returns null
        every { talentRepository.save(any()) } answers { firstArg() }
        every { profileEnricher.enrichProfile(any()) } answers {
            TalentProfileResponse.fromDomain(firstArg<TalentProfile>())
        }

        // When
        val result = createOrUpdateTalentProfileUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("John Actor", result.fullName)
        assertEquals("Experienced actor", result.bio)
        assertEquals(LocationMode.SPECIFIC, result.locationMode)
        assertEquals("Los Angeles", result.primaryLocation?.city)

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

        val existingPrimaryLocation = PrimaryLocation(
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
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = existingPrimaryLocation,
            geoAreas = setOf(GeoArea("New York", "USA", true)),
            profileImageUrl = null
        )

        val newPrimaryLocation = PrimaryLocationDto(
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val newGeoAreas = setOf(
            GeoAreaDto(city = "Los Angeles", country = "USA", isPrimary = true)
        )

        val request = CreateTalentProfileRequest(
            fullName = "Updated Name",
            bio = "Updated bio",
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = newPrimaryLocation,
            geoAreas = newGeoAreas,
            profileImageUrl = "https://example.com/new-profile.jpg"
        )

        every { userRepository.findById(userId) } returns user
        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }
        every { profileEnricher.enrichProfile(any()) } answers {
            TalentProfileResponse.fromDomain(firstArg<TalentProfile>())
        }

        // When
        val result = createOrUpdateTalentProfileUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Updated Name", result.fullName)
        assertEquals("Updated bio", result.bio)
        assertEquals("Los Angeles", result.primaryLocation?.city)

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
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet(),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertTrue(exception.message?.contains("User") ?: false)
        assertTrue(exception.message?.contains("not found") ?: false)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ForbiddenException when user is not talent`() {
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
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet(),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<ForbiddenException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertTrue(exception.message?.contains("Only talent users can create talent profiles") ?: false)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw BadRequestException when fullName is null`() {
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
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet(),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user

        // When & Then
        val exception = assertThrows<BadRequestException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertEquals("Full name is required", exception.message)
    }

    @Test
    fun `should throw IllegalArgumentException when primary location is null for SPECIFIC mode`() {
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
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = null,
            geoAreas = emptySet(),
            profileImageUrl = null
        )

        every { userRepository.findById(userId) } returns user
        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            createOrUpdateTalentProfileUseCase.execute(userId, request)
        }

        assertTrue(exception.message?.contains("Primary location") ?: false)
    }
}
