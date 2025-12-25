package com.castify.domain.talent

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class TalentProfileTest {

    private fun createValidPrimaryLocation() = PrimaryLocation(
        latitude = BigDecimal("40.7128"),
        longitude = BigDecimal("-74.0060"),
        city = "New York",
        country = "USA"
    )

    private fun createValidTalentProfile() = TalentProfile(
        talentProfileId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        fullName = "John Doe",
        locationMode = LocationMode.SPECIFIC,
        primaryLocation = createValidPrimaryLocation(),
        geoAreas = setOf(GeoArea("New York", "USA", true))
    )

    @Test
    fun `should create valid talent profile with all fields`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val fullName = "Jane Smith"
        val bio = "Experienced voice actor"
        val primaryLocation = createValidPrimaryLocation()
        val geoAreas = setOf(GeoArea("New York", "USA", true), GeoArea("Los Angeles", "USA", false))
        val languages = setOf("English", "Spanish")
        val tags = setOf("voice-acting", "commercial")
        val capabilities = TalentCapabilities(canDoVoiceover = true)

        // When
        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = fullName,
            bio = bio,
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = primaryLocation,
            geoAreas = geoAreas,
            languages = languages,
            tags = tags,
            capabilities = capabilities,
            verificationStatus = VerificationStatus.VERIFIED
        )

        // Then
        assertNotNull(profile)
        assertEquals(talentProfileId, profile.talentProfileId)
        assertEquals(userId, profile.userId)
        assertEquals(fullName, profile.fullName)
        assertEquals(bio, profile.bio)
        assertEquals(LocationMode.SPECIFIC, profile.locationMode)
        assertEquals(primaryLocation, profile.primaryLocation)
        assertEquals(geoAreas, profile.geoAreas)
        assertEquals(languages, profile.languages)
        assertEquals(tags, profile.tags)
        assertEquals(capabilities, profile.capabilities)
        assertEquals(VerificationStatus.VERIFIED, profile.verificationStatus)
    }

    @Test
    fun `should create talent profile with minimal required fields`() {
        // When
        val profile = createValidTalentProfile()

        // Then
        assertNotNull(profile)
        assertNull(profile.bio)
        assertTrue(profile.languages.isEmpty())
        assertTrue(profile.tags.isEmpty())
        assertEquals(VerificationStatus.PENDING, profile.verificationStatus)
    }

    @Test
    fun `should throw exception when full name is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            TalentProfile(
                talentProfileId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                fullName = "",
                locationMode = LocationMode.SPECIFIC,
                primaryLocation = createValidPrimaryLocation()
            )
        }

        assertEquals("Full name cannot be blank", exception.message)
    }

    @Test
    fun `should update bio`() {
        // Given
        val profile = createValidTalentProfile()
        val newBio = "Updated bio text"

        // When
        val updatedProfile = profile.copy(bio = newBio, updatedAt = Instant.now())

        // Then
        assertEquals(newBio, updatedProfile.bio)
        assertTrue(updatedProfile.updatedAt >= profile.updatedAt)
    }

    @Test
    fun `should update primary location`() {
        // Given
        val profile = createValidTalentProfile()
        val newLocation = PrimaryLocation(
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        // When
        val updatedProfile = profile.updatePrimaryLocation(newLocation)

        // Then
        assertEquals(newLocation, updatedProfile.primaryLocation)
        assertEquals(profile.talentProfileId, updatedProfile.talentProfileId)
    }

    @Test
    fun `should add geo area`() {
        // Given
        val profile = createValidTalentProfile()
        val newArea = GeoArea("Chicago", "USA", false)

        // When
        val updatedProfile = profile.addGeoArea(newArea)

        // Then
        assertTrue(updatedProfile.geoAreas.contains(newArea))
        assertEquals(profile.geoAreas.size + 1, updatedProfile.geoAreas.size)
    }

    @Test
    fun `should remove geo area`() {
        // Given
        val profile = createValidTalentProfile()
        val areaToRemove = profile.geoAreas.first()

        // When
        val updatedProfile = profile.removeGeoArea(areaToRemove.city, areaToRemove.country)

        // Then
        assertFalse(updatedProfile.geoAreas.contains(areaToRemove))
    }

    @Test
    fun `should throw exception when adding duplicate geo area`() {
        // Given
        val profile = createValidTalentProfile()
        val existingArea = profile.geoAreas.first()

        // When & Then
        assertThrows<IllegalArgumentException> {
            profile.addGeoArea(existingArea)
        }
    }

    @Test
    fun `should update languages`() {
        // Given
        val profile = createValidTalentProfile()
        val newLanguages = setOf("English", "French", "German")

        // When
        val updatedProfile = profile.updateLanguages(newLanguages)

        // Then
        assertEquals(newLanguages, updatedProfile.languages)
    }

    @Test
    fun `should add language`() {
        // Given
        val profile = createValidTalentProfile()
        val existingLanguages = setOf("English")
        val profileWithLang = profile.copy(languages = existingLanguages)

        // When
        val updatedProfile = profileWithLang.updateLanguages(existingLanguages + "Spanish")

        // Then
        assertTrue(updatedProfile.languages.contains("English"))
        assertTrue(updatedProfile.languages.contains("Spanish"))
        assertEquals(2, updatedProfile.languages.size)
    }

    @Test
    fun `should remove language`() {
        // Given
        val profile = createValidTalentProfile()
        val existingLanguages = setOf("English", "Spanish", "French")
        val profileWithLangs = profile.copy(languages = existingLanguages)

        // When
        val updatedProfile = profileWithLangs.updateLanguages(existingLanguages - "Spanish")

        // Then
        assertTrue(updatedProfile.languages.contains("English"))
        assertTrue(updatedProfile.languages.contains("French"))
        assertFalse(updatedProfile.languages.contains("Spanish"))
        assertEquals(2, updatedProfile.languages.size)
    }

    @Test
    fun `should update tags`() {
        // Given
        val profile = createValidTalentProfile()
        val newTags = setOf("acting", "voiceover", "photoshooting")

        // When
        val updatedProfile = profile.updateTags(newTags)

        // Then
        assertEquals(newTags, updatedProfile.tags)
    }

    @Test
    fun `should add tag`() {
        // Given
        val profile = createValidTalentProfile()
        val existingTags = setOf("acting")
        val profileWithTag = profile.copy(tags = existingTags)

        // When
        val updatedProfile = profileWithTag.updateTags(existingTags + "voiceover")

        // Then
        assertTrue(updatedProfile.tags.contains("acting"))
        assertTrue(updatedProfile.tags.contains("voiceover"))
        assertEquals(2, updatedProfile.tags.size)
    }

    @Test
    fun `should remove tag`() {
        // Given
        val profile = createValidTalentProfile()
        val existingTags = setOf("acting", "voiceover", "photoshooting")
        val profileWithTags = profile.copy(tags = existingTags)

        // When
        val updatedProfile = profileWithTags.updateTags(existingTags - "voiceover")

        // Then
        assertTrue(updatedProfile.tags.contains("acting"))
        assertTrue(updatedProfile.tags.contains("photoshooting"))
        assertFalse(updatedProfile.tags.contains("voiceover"))
        assertEquals(2, updatedProfile.tags.size)
    }

    @Test
    fun `should update capabilities`() {
        // Given
        val profile = createValidTalentProfile()
        val newCapabilities = TalentCapabilities(
            canDoVoiceover = true,
            canDoActing = true,
            canDoModeling = false
        )

        // When
        val updatedProfile = profile.updateCapabilities(newCapabilities)

        // Then
        assertEquals(newCapabilities, updatedProfile.capabilities)
    }

    @Test
    fun `should verify profile`() {
        // Given
        val profile = createValidTalentProfile()

        // When
        val verifiedProfile = profile.verify()

        // Then
        assertEquals(VerificationStatus.VERIFIED, verifiedProfile.verificationStatus)
        assertTrue(verifiedProfile.isVerified())
    }

    @Test
    fun `should reject profile`() {
        // Given
        val profile = createValidTalentProfile()

        // When
        val rejectedProfile = profile.reject()

        // Then
        assertEquals(VerificationStatus.REJECTED, rejectedProfile.verificationStatus)
        assertFalse(rejectedProfile.isVerified())
    }

    @Test
    fun `isVerified should return true only for verified status`() {
        // Given
        val pendingProfile = createValidTalentProfile()
        val verifiedProfile = pendingProfile.copy(verificationStatus = VerificationStatus.VERIFIED)
        val rejectedProfile = pendingProfile.copy(verificationStatus = VerificationStatus.REJECTED)

        // Then
        assertFalse(pendingProfile.isVerified())
        assertTrue(verifiedProfile.isVerified())
        assertFalse(rejectedProfile.isVerified())
    }
}

class PrimaryLocationTest {

    @Test
    fun `should create primary location with coordinates`() {
        // Given
        val latitude = BigDecimal("40.7128")
        val longitude = BigDecimal("-74.0060")

        // When
        val location = PrimaryLocation(
            latitude = latitude,
            longitude = longitude,
            city = "New York",
            country = "USA"
        )

        // Then
        assertEquals(latitude, location.latitude)
        assertEquals(longitude, location.longitude)
        assertEquals("New York", location.city)
        assertEquals("USA", location.country)
    }

    @Test
    fun `should throw exception when city is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            PrimaryLocation(
                latitude = BigDecimal("40.7128"),
                longitude = BigDecimal("-74.0060"),
                city = "",
                country = "USA"
            )
        }

        assertEquals("City cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when country is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            PrimaryLocation(
                latitude = BigDecimal("40.7128"),
                longitude = BigDecimal("-74.0060"),
                city = "New York",
                country = ""
            )
        }

        assertEquals("Country cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when latitude is out of range`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            PrimaryLocation(
                latitude = BigDecimal("91"),
                longitude = BigDecimal("-74.0060"),
                city = "New York",
                country = "USA"
            )
        }

        assertTrue(exception.message!!.contains("Latitude"))
    }
}

class GeoAreaTest {

    @Test
    fun `should create geo area`() {
        // When
        val geoArea = GeoArea(
            city = "Belgrade",
            country = "Serbia",
            isPrimary = true
        )

        // Then
        assertEquals("Belgrade", geoArea.city)
        assertEquals("Serbia", geoArea.country)
        assertTrue(geoArea.isPrimary)
    }

    @Test
    fun `should create non-primary geo area by default`() {
        // When
        val geoArea = GeoArea(
            city = "Novi Sad",
            country = "Serbia"
        )

        // Then
        assertFalse(geoArea.isPrimary)
    }

    @Test
    fun `should throw exception when city is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            GeoArea(
                city = "",
                country = "Serbia"
            )
        }

        assertEquals("City cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when country is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            GeoArea(
                city = "Belgrade",
                country = ""
            )
        }

        assertEquals("Country cannot be blank", exception.message)
    }
}

class TalentCapabilitiesTest {

    @Test
    fun `should create capabilities with default values`() {
        // When
        val capabilities = TalentCapabilities()

        // Then
        assertFalse(capabilities.canDoVoiceover)
        assertFalse(capabilities.canDoActing)
        assertFalse(capabilities.canDoModeling)
        assertFalse(capabilities.canDoSinging)
        assertFalse(capabilities.canDoDancing)
        assertFalse(capabilities.canDoHosting)
        assertNull(capabilities.otherCapabilities)
        assertFalse(capabilities.hasAnyCapability())
    }

    @Test
    fun `should create capabilities with specific values`() {
        // When
        val capabilities = TalentCapabilities(
            canDoVoiceover = true,
            canDoActing = true,
            otherCapabilities = "Stunt work"
        )

        // Then
        assertTrue(capabilities.canDoVoiceover)
        assertTrue(capabilities.canDoActing)
        assertFalse(capabilities.canDoModeling)
        assertEquals("Stunt work", capabilities.otherCapabilities)
        assertTrue(capabilities.hasAnyCapability())
    }

    @Test
    fun `hasAnyCapability should return true when at least one capability is true`() {
        val testCases = listOf(
            TalentCapabilities(canDoVoiceover = true),
            TalentCapabilities(canDoActing = true),
            TalentCapabilities(canDoModeling = true),
            TalentCapabilities(canDoSinging = true),
            TalentCapabilities(canDoDancing = true),
            TalentCapabilities(canDoHosting = true)
        )

        testCases.forEach { capability ->
            assertTrue(capability.hasAnyCapability())
        }
    }

    @Test
    fun `hasAnyCapability should return false when all capabilities are false`() {
        // Given
        val capabilities = TalentCapabilities(
            canDoVoiceover = false,
            canDoActing = false,
            canDoModeling = false,
            canDoSinging = false,
            canDoDancing = false,
            canDoHosting = false
        )

        // Then
        assertFalse(capabilities.hasAnyCapability())
    }
}
