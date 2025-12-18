package com.castify.domain.talent

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class TalentProfileTest {

    private fun createValidLocation() = Location(
        mode = LocationMode.REMOTE,
        city = "New York",
        country = "USA"
    )

    private fun createValidTalentProfile() = TalentProfile(
        talentProfileId = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        fullName = "John Doe",
        location = createValidLocation()
    )

    @Test
    fun `should create valid talent profile with all fields`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val fullName = "Jane Smith"
        val bio = "Experienced voice actor"
        val location = createValidLocation()
        val languages = setOf("English", "Spanish")
        val tags = setOf("voice-acting", "commercial")
        val capabilities = TalentCapabilities(canDoVoiceover = true)
        val socialLinks = setOf(
            SocialLink("Twitter", "https://twitter.com/jane", 1000)
        )

        // When
        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = fullName,
            bio = bio,
            location = location,
            languages = languages,
            tags = tags,
            capabilities = capabilities,
            socialLinks = socialLinks,
            verificationStatus = VerificationStatus.VERIFIED
        )

        // Then
        assertNotNull(profile)
        assertEquals(talentProfileId, profile.talentProfileId)
        assertEquals(userId, profile.userId)
        assertEquals(fullName, profile.fullName)
        assertEquals(bio, profile.bio)
        assertEquals(location, profile.location)
        assertEquals(languages, profile.languages)
        assertEquals(tags, profile.tags)
        assertEquals(capabilities, profile.capabilities)
        assertEquals(socialLinks, profile.socialLinks)
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
        assertTrue(profile.socialLinks.isEmpty())
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
                location = createValidLocation()
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
        val updatedProfile = profile.updateBio(newBio)

        // Then
        assertEquals(newBio, updatedProfile.bio)
        assertTrue(updatedProfile.updatedAt.isAfter(profile.updatedAt) ||
                   updatedProfile.updatedAt == profile.updatedAt)
    }

    @Test
    fun `should update location`() {
        // Given
        val profile = createValidTalentProfile()
        val newLocation = Location(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("40.7128"),
            longitude = BigDecimal("-74.0060"),
            city = "New York",
            country = "USA"
        )

        // When
        val updatedProfile = profile.updateLocation(newLocation)

        // Then
        assertEquals(newLocation, updatedProfile.location)
        assertEquals(profile.talentProfileId, updatedProfile.talentProfileId)
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
        val updatedProfile = profileWithLang.addLanguage("Spanish")

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
        val updatedProfile = profileWithLangs.removeLanguage("Spanish")

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
        val newTags = setOf("acting", "singing", "dancing")

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
        val updatedProfile = profileWithTag.addTag("singing")

        // Then
        assertTrue(updatedProfile.tags.contains("acting"))
        assertTrue(updatedProfile.tags.contains("singing"))
        assertEquals(2, updatedProfile.tags.size)
    }

    @Test
    fun `should remove tag`() {
        // Given
        val profile = createValidTalentProfile()
        val existingTags = setOf("acting", "singing", "dancing")
        val profileWithTags = profile.copy(tags = existingTags)

        // When
        val updatedProfile = profileWithTags.removeTag("singing")

        // Then
        assertTrue(updatedProfile.tags.contains("acting"))
        assertTrue(updatedProfile.tags.contains("dancing"))
        assertFalse(updatedProfile.tags.contains("singing"))
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
    fun `should add social link`() {
        // Given
        val profile = createValidTalentProfile()
        val socialLink = SocialLink("Instagram", "https://instagram.com/profile", 5000)

        // When
        val updatedProfile = profile.addSocialLink(socialLink)

        // Then
        assertTrue(updatedProfile.socialLinks.contains(socialLink))
        assertEquals(1, updatedProfile.socialLinks.size)
    }

    @Test
    fun `should remove social link by platform`() {
        // Given
        val profile = createValidTalentProfile()
        val link1 = SocialLink("Twitter", "https://twitter.com/profile", 1000)
        val link2 = SocialLink("Instagram", "https://instagram.com/profile", 2000)
        val profileWithLinks = profile.copy(socialLinks = setOf(link1, link2))

        // When
        val updatedProfile = profileWithLinks.removeSocialLink("Twitter")

        // Then
        assertFalse(updatedProfile.socialLinks.any { it.platform == "Twitter" })
        assertTrue(updatedProfile.socialLinks.any { it.platform == "Instagram" })
        assertEquals(1, updatedProfile.socialLinks.size)
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

class LocationTest {

    @Test
    fun `should create remote location`() {
        // When
        val location = Location(
            mode = LocationMode.REMOTE,
            city = "New York",
            country = "USA"
        )

        // Then
        assertEquals(LocationMode.REMOTE, location.mode)
        assertEquals("New York", location.city)
        assertEquals("USA", location.country)
        assertNull(location.latitude)
        assertNull(location.longitude)
    }

    @Test
    fun `should create specific location with coordinates`() {
        // Given
        val latitude = BigDecimal("40.7128")
        val longitude = BigDecimal("-74.0060")

        // When
        val location = Location(
            mode = LocationMode.SPECIFIC,
            latitude = latitude,
            longitude = longitude,
            city = "New York",
            country = "USA"
        )

        // Then
        assertEquals(LocationMode.SPECIFIC, location.mode)
        assertEquals(latitude, location.latitude)
        assertEquals(longitude, location.longitude)
    }

    @Test
    fun `should throw exception when specific location missing coordinates`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Location(
                mode = LocationMode.SPECIFIC,
                city = "New York",
                country = "USA"
            )
        }

        assertEquals("Latitude and longitude are required for specific location mode", exception.message)
    }

    @Test
    fun `should create hybrid location`() {
        // When
        val location = Location(
            mode = LocationMode.HYBRID,
            city = "San Francisco",
            country = "USA"
        )

        // Then
        assertEquals(LocationMode.HYBRID, location.mode)
        assertEquals("San Francisco", location.city)
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

class SocialLinkTest {

    @Test
    fun `should create valid social link`() {
        // When
        val socialLink = SocialLink(
            platform = "Instagram",
            url = "https://instagram.com/user",
            followerCount = 10000
        )

        // Then
        assertEquals("Instagram", socialLink.platform)
        assertEquals("https://instagram.com/user", socialLink.url)
        assertEquals(10000, socialLink.followerCount)
    }

    @Test
    fun `should create social link without follower count`() {
        // When
        val socialLink = SocialLink(
            platform = "Twitter",
            url = "https://twitter.com/user"
        )

        // Then
        assertEquals("Twitter", socialLink.platform)
        assertEquals("https://twitter.com/user", socialLink.url)
        assertNull(socialLink.followerCount)
    }

    @Test
    fun `should throw exception when platform is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            SocialLink(
                platform = "",
                url = "https://instagram.com/user"
            )
        }

        assertEquals("Platform cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when URL is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            SocialLink(
                platform = "Instagram",
                url = ""
            )
        }

        assertEquals("URL cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception when URL does not start with http or https`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            SocialLink(
                platform = "Instagram",
                url = "www.instagram.com/user"
            )
        }

        assertEquals("URL must start with http:// or https://", exception.message)
    }

    @Test
    fun `should accept both http and https URLs`() {
        val httpLink = SocialLink("Twitter", "http://twitter.com/user")
        val httpsLink = SocialLink("Instagram", "https://instagram.com/user")

        assertEquals("http://twitter.com/user", httpLink.url)
        assertEquals("https://instagram.com/user", httpsLink.url)
    }
}
