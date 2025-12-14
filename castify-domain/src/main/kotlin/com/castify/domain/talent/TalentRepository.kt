package com.castify.domain.talent

import java.math.BigDecimal
import java.util.*

interface TalentRepository {
    fun save(talentProfile: TalentProfile): TalentProfile
    fun findById(talentProfileId: UUID): TalentProfile?
    fun findByUserId(userId: UUID): TalentProfile?
    fun search(searchCriteria: TalentSearchCriteria): List<TalentProfile>
    fun searchPaginated(searchCriteria: TalentSearchCriteria, page: Int, size: Int): PagedTalentResult
}

data class TalentSearchCriteria(
    val tags: Set<String>? = null,
    val languages: Set<String>? = null,
    val capabilities: TalentCapabilities? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val radiusKm: Double? = null,
    val verificationStatus: VerificationStatus? = null
)

data class PagedTalentResult(
    val profiles: List<TalentProfile>,
    val totalElements: Long
)
