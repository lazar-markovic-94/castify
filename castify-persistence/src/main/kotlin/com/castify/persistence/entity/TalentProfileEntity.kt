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

    @Column(name = "primary_latitude", precision = 10, scale = 7)
    var primaryLatitude: BigDecimal? = null,

    @Column(name = "primary_longitude", precision = 10, scale = 7)
    var primaryLongitude: BigDecimal? = null,

    @Column(name = "primary_city", length = 100)
    var primaryCity: String? = null,

    @Column(name = "primary_country", length = 100)
    var primaryCountry: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(name = "introduction_video_url")
    var introductionVideoUrl: String? = null,

    @Column(name = "introduction_video_duration")
    var introductionVideoDuration: Int? = null,

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
    var updatedAt: Instant = Instant.now(),

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0,

    @Column(name = "verified_by")
    var verifiedBy: UUID? = null,

    @Column(name = "verified_at")
    var verifiedAt: Instant? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
) {
    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var languages: MutableSet<TalentLanguageEntity> = mutableSetOf()

    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var tags: MutableSet<TalentTagEntity> = mutableSetOf()

    @OneToMany(mappedBy = "talentProfile", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var geoAreas: MutableSet<TalentGeoAreaEntity> = mutableSetOf()

    fun toDomain(): TalentProfile {
        val primaryLocation = if (primaryLatitude != null && primaryLongitude != null
            && primaryCity != null && primaryCountry != null) {
            PrimaryLocation(
                latitude = primaryLatitude!!,
                longitude = primaryLongitude!!,
                city = primaryCity!!,
                country = primaryCountry!!
            )
        } else null

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
            locationMode = locationMode,
            primaryLocation = primaryLocation,
            geoAreas = geoAreas.map { it.toDomain() }.toSet(),
            languages = languages.map { it.languageCode }.toSet(),
            tags = tags.map { it.tagName }.toSet(),
            capabilities = capabilities,
            verificationStatus = verificationStatus,
            profileImageUrl = profileImageUrl,
            introductionVideoUrl = introductionVideoUrl,
            introductionVideoDuration = introductionVideoDuration,
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
                locationMode = profile.locationMode,
                primaryLatitude = profile.primaryLocation?.latitude,
                primaryLongitude = profile.primaryLocation?.longitude,
                primaryCity = profile.primaryLocation?.city,
                primaryCountry = profile.primaryLocation?.country,
                verificationStatus = profile.verificationStatus,
                profileImageUrl = profile.profileImageUrl,
                introductionVideoUrl = profile.introductionVideoUrl,
                introductionVideoDuration = profile.introductionVideoDuration,
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
                val category = PredefinedTags.getCategoryForTag(tag)
                    ?: throw IllegalArgumentException("Invalid tag: $tag")
                TalentTagEntity(
                    talentProfile = entity,
                    tagName = tag,
                    category = category
                )
            }.toMutableSet()

            // Add geo areas
            entity.geoAreas = profile.geoAreas.map { geoArea ->
                TalentGeoAreaEntity.fromDomain(entity, geoArea)
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
    var tagName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    var category: TagCategory
)

@Entity
@Table(name = "talent_geo_area")
class TalentGeoAreaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_profile_id", nullable = false)
    var talentProfile: TalentProfileEntity,

    @Column(nullable = false, length = 100)
    var city: String,

    @Column(nullable = false, length = 100)
    var country: String,

    @Column(name = "is_primary", nullable = false)
    var isPrimary: Boolean = false
) {
    fun toDomain(): GeoArea = GeoArea(
        city = city,
        country = country,
        isPrimary = isPrimary
    )

    companion object {
        fun fromDomain(talentProfile: TalentProfileEntity, geoArea: GeoArea): TalentGeoAreaEntity =
            TalentGeoAreaEntity(
                talentProfile = talentProfile,
                city = geoArea.city,
                country = geoArea.country,
                isPrimary = geoArea.isPrimary
            )
    }
}
