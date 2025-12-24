package com.castify.persistence.adapter

import com.castify.domain.talent.PagedTalentResult
import com.castify.domain.talent.PredefinedTags
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.domain.talent.TalentSearchCriteria
import com.castify.persistence.entity.TalentGeoAreaEntity
import com.castify.persistence.entity.TalentLanguageEntity
import com.castify.persistence.entity.TalentProfileEntity
import com.castify.persistence.entity.TalentSocialLinkEntity
import com.castify.persistence.entity.TalentTagEntity
import com.castify.persistence.jpa.TalentProfileJpaRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.*

@Component
class TalentRepositoryAdapter(
    private val talentProfileJpaRepository: TalentProfileJpaRepository,
    @PersistenceContext private val entityManager: EntityManager
) : TalentRepository {

    override fun save(talentProfile: TalentProfile): TalentProfile {
        val existingEntity = talentProfileJpaRepository.findById(talentProfile.talentProfileId).orElse(null)

        val entity = if (existingEntity != null) {
            // Update existing entity to preserve version and avoid optimistic locking issues
            existingEntity.apply {
                userId = talentProfile.userId
                fullName = talentProfile.fullName
                bio = talentProfile.bio
                locationMode = talentProfile.locationMode
                primaryLatitude = talentProfile.primaryLocation?.latitude
                primaryLongitude = talentProfile.primaryLocation?.longitude
                primaryCity = talentProfile.primaryLocation?.city
                primaryCountry = talentProfile.primaryLocation?.country
                verificationStatus = talentProfile.verificationStatus
                profileImageUrl = talentProfile.profileImageUrl
                canDoVoiceover = talentProfile.capabilities.canDoVoiceover
                canDoActing = talentProfile.capabilities.canDoActing
                canDoModeling = talentProfile.capabilities.canDoModeling
                canDoSinging = talentProfile.capabilities.canDoSinging
                canDoDancing = talentProfile.capabilities.canDoDancing
                canDoHosting = talentProfile.capabilities.canDoHosting
                otherCapabilities = talentProfile.capabilities.otherCapabilities
                updatedAt = talentProfile.updatedAt

                // Update languages collection - remove obsolete ones and add new ones
                val newLanguageCodes = talentProfile.languages
                languages.removeIf { !newLanguageCodes.contains(it.languageCode) }
                val existingLanguageCodes = languages.map { it.languageCode }.toSet()
                newLanguageCodes.filter { !existingLanguageCodes.contains(it) }.forEach { lang ->
                    languages.add(TalentLanguageEntity(talentProfile = this, languageCode = lang))
                }

                // Update tags collection - remove obsolete ones and add new ones
                val newTagNames = talentProfile.tags
                tags.removeIf { !newTagNames.contains(it.tagName) }
                val existingTagNames = tags.map { it.tagName }.toSet()
                newTagNames.filter { !existingTagNames.contains(it) }.forEach { tagName ->
                    val category = PredefinedTags.getCategoryForTag(tagName)
                        ?: throw IllegalArgumentException("Invalid tag: $tagName")
                    tags.add(TalentTagEntity(talentProfile = this, tagName = tagName, category = category))
                }

                // Update social links collection - remove obsolete ones and add/update existing ones
                val newPlatforms = talentProfile.socialLinks.map { it.platform }.toSet()
                socialLinks.removeIf { !newPlatforms.contains(it.platform) }
                val existingPlatforms = socialLinks.map { it.platform }.toSet()
                talentProfile.socialLinks.forEach { link ->
                    if (!existingPlatforms.contains(link.platform)) {
                        socialLinks.add(TalentSocialLinkEntity.fromDomain(this, link))
                    } else {
                        // Update existing social link
                        socialLinks.find { it.platform == link.platform }?.apply {
                            url = link.url
                            followerCount = link.followerCount
                        }
                    }
                }

                // Update geo areas collection - remove obsolete ones and add new ones
                val newGeoAreaKeys = talentProfile.geoAreas.map { it.city to it.country }.toSet()
                geoAreas.removeIf { (it.city to it.country) !in newGeoAreaKeys }
                val existingGeoAreaKeys = geoAreas.map { it.city to it.country }.toSet()
                talentProfile.geoAreas.filter { (it.city to it.country) !in existingGeoAreaKeys }.forEach { geoArea ->
                    geoAreas.add(TalentGeoAreaEntity.fromDomain(this, geoArea))
                }
                // Update isPrimary flag for existing geo areas
                geoAreas.forEach { entity ->
                    val domainGeoArea = talentProfile.geoAreas.find { it.city == entity.city && it.country == entity.country }
                    domainGeoArea?.let { entity.isPrimary = it.isPrimary }
                }
            }
        } else {
            // Create new entity
            TalentProfileEntity.fromDomain(talentProfile)
        }

        val saved = talentProfileJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(talentProfileId: UUID): TalentProfile? {
        return talentProfileJpaRepository.findById(talentProfileId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: UUID): TalentProfile? {
        return talentProfileJpaRepository.findByUserIdWithCollections(userId)?.toDomain()
    }

    override fun existsById(talentProfileId: UUID): Boolean {
        return talentProfileJpaRepository.existsById(talentProfileId)
    }

    override fun search(searchCriteria: TalentSearchCriteria): List<TalentProfile> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(TalentProfileEntity::class.java)
        val root: Root<TalentProfileEntity> = query.from(TalentProfileEntity::class.java)

        val predicates = mutableListOf<Predicate>()

        // Verification status filter
        searchCriteria.verificationStatus?.let { status ->
            predicates.add(cb.equal(root.get<String>("verificationStatus"), status))
        }

        // Capabilities filters
        searchCriteria.capabilities?.let { caps ->
            if (caps.canDoVoiceover) {
                predicates.add(cb.isTrue(root.get("canDoVoiceover")))
            }
            if (caps.canDoActing) {
                predicates.add(cb.isTrue(root.get("canDoActing")))
            }
            if (caps.canDoModeling) {
                predicates.add(cb.isTrue(root.get("canDoModeling")))
            }
            if (caps.canDoSinging) {
                predicates.add(cb.isTrue(root.get("canDoSinging")))
            }
            if (caps.canDoDancing) {
                predicates.add(cb.isTrue(root.get("canDoDancing")))
            }
            if (caps.canDoHosting) {
                predicates.add(cb.isTrue(root.get("canDoHosting")))
            }
        }

        query.where(*predicates.toTypedArray())

        var results = entityManager.createQuery(query).resultList.map { it.toDomain() }

        // Apply language filter (in-memory due to Set<String>)
        searchCriteria.languages?.let { requiredLanguages ->
            results = results.filter { profile ->
                profile.languages.containsAll(requiredLanguages)
            }
        }

        // Apply tag filter (in-memory due to Set<String>)
        searchCriteria.tags?.let { requiredTags ->
            results = results.filter { profile ->
                requiredTags.any { tag -> profile.tags.contains(tag) }
            }
        }

        // Apply location filter with Haversine distance using primary location
        if (searchCriteria.latitude != null && searchCriteria.longitude != null && searchCriteria.radiusKm != null) {
            val searchLat = searchCriteria.latitude!!.toDouble()
            val searchLon = searchCriteria.longitude!!.toDouble()
            val radiusKm = searchCriteria.radiusKm!!

            results = results.filter { profile ->
                val primaryLocation = profile.primaryLocation

                if (primaryLocation != null) {
                    val profileLat = primaryLocation.latitude.toDouble()
                    val profileLon = primaryLocation.longitude.toDouble()
                    val distance = calculateHaversineDistance(searchLat, searchLon, profileLat, profileLon)
                    distance <= radiusKm
                } else {
                    false
                }
            }
        }

        return results
    }

    override fun searchPaginated(searchCriteria: TalentSearchCriteria, page: Int, size: Int): PagedTalentResult {
        // Reuse the existing search logic
        val allResults = search(searchCriteria)

        // Apply pagination in-memory
        val totalElements = allResults.size.toLong()
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, allResults.size)

        val paginatedResults = if (startIndex < allResults.size) {
            allResults.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return PagedTalentResult(
            profiles = paginatedResults,
            totalElements = totalElements
        )
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
