package com.castify.persistence.adapter

import com.castify.domain.talent.PagedTalentResult
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.domain.talent.TalentSearchCriteria
import com.castify.persistence.entity.TalentProfileEntity
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
        val entity = TalentProfileEntity.fromDomain(talentProfile)
        val saved = talentProfileJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(talentProfileId: UUID): TalentProfile? {
        return talentProfileJpaRepository.findById(talentProfileId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: UUID): TalentProfile? {
        return talentProfileJpaRepository.findByUserId(userId)?.toDomain()
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
