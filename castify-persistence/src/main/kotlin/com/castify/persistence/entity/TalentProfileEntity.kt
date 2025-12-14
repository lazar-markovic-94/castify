package com.castify.persistence.entity

import com.castify.domain.talent.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "talent_profile")
class TalentProfileEntity(
    @Id
    @Column(name = "talent_profile_id", columnDefinition = "UUID")
    var talentProfileId: UUID = UUID.randomUUID(),

    @Column(name = "user_id", unique = true, nullable = false, columnDefinition = "UUID")
    var userId: UUID,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(columnDefinition = "TEXT")
    var bio: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "location_mode", nullable = false, length = 20)
    var locationMode: LocationMode,

    @Column(precision = 10, scale = 7)
    var latitude: BigDecimal? = null,

    @Column(precision = 10, scale = 7)
    var longitude: BigDecimal? = null,

    @Column(length = 100)
    var city: String? = null,

    @Column(length = 100)
    var country: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(name = "can_do_voiceover", nullable = false)
    var canDoVoiceover: Boolean = false,

    @Column(name = "can_do_acting", nullable = false)
    var canDoActing: Boolean = false,

    @Column(name = "can_do_modeling", nullable = false)
    var canDoModeling: Boolean = false,

    @Column(name = "can_do_singing", nullable = false)
    var canDoSinging: Boolean = false,

    @Column(name = "can_do_dancing", nullable = false)
    var canDoDancing: Boolean = false,

    @Column(name = "can_do_hosting", nullable = false)
    var canDoHosting: Boolean = false,

    @Column(name = "other_capabilities", columnDefinition = "TEXT")
    var otherCapabilities: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var languages: MutableSet<TalentLanguageEntity> = mutableSetOf()

    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var tags: MutableSet<TalentTagEntity> = mutableSetOf()

    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var socialLinks: MutableSet<TalentSocialLinkEntity> = mutableSetOf()

    fun toDomain(): TalentProfile {
        val location = Location(
            mode = locationMode,
            latitude = latitude,
            longitude = longitude,
            city = city,
            country = country
        )

        val capabilities = TalentCapabilities(
            canDoVoiceover = canDoVoiceover,
            canDoActing = canDoActing,
            canDoModeling = canDoModeling,
            canDoSinging = canDoSinging,
            canDoDancing = canDoDancing,
            canDoHosting = canDoHosting,
            otherCapabilities = otherCapabilities
        )

        return TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = fullName,
            bio = bio,
            location = location,
            languages = languages.map { it.languageCode }.toSet(),
            tags = tags.map { it.tagName }.toSet(),
            capabilities = capabilities,
            socialLinks = socialLinks.map { it.toDomain() }.toSet(),
            verificationStatus = verificationStatus,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(profile: TalentProfile): TalentProfileEntity {
            val entity = TalentProfileEntity(
                talentProfileId = profile.talentProfileId,
                userId = profile.userId,
                fullName = profile.fullName,
                bio = profile.bio,
                locationMode = profile.location.mode,
                latitude = profile.location.latitude,
                longitude = profile.location.longitude,
                city = profile.location.city,
                country = profile.location.country,
                verificationStatus = profile.verificationStatus,
                profileImageUrl = profile.profileImageUrl,
                canDoVoiceover = profile.capabilities.canDoVoiceover,
                canDoActing = profile.capabilities.canDoActing,
                canDoModeling = profile.capabilities.canDoModeling,
                canDoSinging = profile.capabilities.canDoSinging,
                canDoDancing = profile.capabilities.canDoDancing,
                canDoHosting = profile.capabilities.canDoHosting,
                otherCapabilities = profile.capabilities.otherCapabilities,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt
            )

            // Add languages
            entity.languages = profile.languages.map { lang ->
                TalentLanguageEntity(
                    talentProfile = entity,
                    languageCode = lang
                )
            }.toMutableSet()

            // Add tags
            entity.tags = profile.tags.map { tag ->
                TalentTagEntity(
                    talentProfile = entity,
                    tagName = tag
                )
            }.toMutableSet()

            // Add social links
            entity.socialLinks = profile.socialLinks.map { link ->
                TalentSocialLinkEntity.fromDomain(entity, link)
            }.toMutableSet()

            return entity
        }
    }
}

@Entity
@Table(name = "talent_language")
class TalentLanguageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_profile_id", nullable = false)
    var talentProfile: TalentProfileEntity,

    @Column(name = "language_code", nullable = false, length = 10)
    var languageCode: String
)

@Entity
@Table(name = "talent_tag")
class TalentTagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_profile_id", nullable = false)
    var talentProfile: TalentProfileEntity,

    @Column(name = "tag_name", nullable = false, length = 50)
    var tagName: String
)

@Entity
@Table(name = "talent_social_link")
class TalentSocialLinkEntity(
    @Id
    @Column(name = "social_link_id", columnDefinition = "UUID")
    var socialLinkId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_profile_id", nullable = false)
    var talentProfile: TalentProfileEntity,

    @Column(nullable = false, length = 50)
    var platform: String,

    @Column(nullable = false)
    var url: String,

    @Column(name = "follower_count")
    var followerCount: Long? = null
) {
    fun toDomain(): SocialLink = SocialLink(
        platform = platform,
        url = url,
        followerCount = followerCount
    )

    companion object {
        fun fromDomain(talentProfile: TalentProfileEntity, link: SocialLink): TalentSocialLinkEntity =
            TalentSocialLinkEntity(
                socialLinkId = UUID.randomUUID(),
                talentProfile = talentProfile,
                platform = link.platform,
                url = link.url,
                followerCount = link.followerCount
            )
    }
}
