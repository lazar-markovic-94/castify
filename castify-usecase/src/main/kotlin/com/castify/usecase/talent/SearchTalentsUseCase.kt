package com.castify.usecase.talent

import com.castify.domain.talent.TalentCapabilities
import com.castify.domain.talent.TalentRepository
import com.castify.domain.talent.TalentSearchCriteria
import com.castify.usecase.dto.PagedResult
import com.castify.usecase.dto.TalentProfileResponse
import com.castify.usecase.dto.TalentSearchRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for searching and filtering talents.
 * Supports pagination, capabilities filtering, and rating-based filtering.
 */
@Service
@Transactional(readOnly = true)
class SearchTalentsUseCase(
    private val talentRepository: TalentRepository,
    private val profileEnricher: TalentProfileEnricher
) {
    private val logger = LoggerFactory.getLogger(SearchTalentsUseCase::class.java)
    /**
     * Searches for talents based on various criteria.
     *
     * @param request The search request with filters and pagination
     * @return Paginated search results
     */
    fun execute(request: TalentSearchRequest): PagedResult<TalentProfileResponse> {
        logger.info("Searching talents with criteria: page=${request.page}, size=${request.size}")
        return executeSearch(request)
    }

    private fun executeSearch(request: TalentSearchRequest): PagedResult<TalentProfileResponse> {
        val capabilities = if (request.canDoVoiceover != null ||
            request.canDoActing != null ||
            request.canDoModeling != null ||
            request.canDoSinging != null ||
            request.canDoDancing != null ||
            request.canDoHosting != null
        ) {
            TalentCapabilities(
                canDoVoiceover = request.canDoVoiceover ?: false,
                canDoActing = request.canDoActing ?: false,
                canDoModeling = request.canDoModeling ?: false,
                canDoSinging = request.canDoSinging ?: false,
                canDoDancing = request.canDoDancing ?: false,
                canDoHosting = request.canDoHosting ?: false
            )
        } else {
            null
        }

        val searchCriteria = TalentSearchCriteria(
            tags = request.tags,
            languages = request.languages,
            capabilities = capabilities,
            latitude = request.latitude,
            longitude = request.longitude,
            radiusKm = request.radiusKm,
            verificationStatus = request.verificationStatus
        )

        // Check if we have rating/tier filters
        val hasRatingFilters = request.minAverageRating != null ||
                               request.tiers != null ||
                               request.minCompletedCollaborations != null

        if (hasRatingFilters) {
            // If rating filters exist, we need to fetch ALL results, filter, then paginate
            // This is less efficient but necessary for accurate filtering
            val allResults = talentRepository.searchPaginated(searchCriteria, 0, Int.MAX_VALUE)
            val enrichedProfiles = profileEnricher.enrichProfiles(allResults.profiles)
            val filtered = applyRatingFilters(enrichedProfiles, request)

            // Now apply pagination to the filtered results
            val start = request.page * request.size
            val end = minOf(start + request.size, filtered.size)
            val paginatedContent = if (start < filtered.size) {
                filtered.subList(start, end)
            } else {
                emptyList()
            }

            val totalPages = (filtered.size + request.size - 1) / request.size

            return PagedResult(
                content = paginatedContent,
                page = request.page,
                size = request.size,
                totalElements = filtered.size.toLong(),
                totalPages = totalPages
            )
        } else {
            // No rating filters, use efficient database pagination
            val pagedResult = talentRepository.searchPaginated(searchCriteria, request.page, request.size)
            val enrichedProfiles = profileEnricher.enrichProfiles(pagedResult.profiles)
            val totalPages = (pagedResult.totalElements + request.size - 1) / request.size

            return PagedResult(
                content = enrichedProfiles,
                page = request.page,
                size = request.size,
                totalElements = pagedResult.totalElements,
                totalPages = totalPages.toInt()
            )
        }
    }

    private fun applyRatingFilters(
        profiles: List<TalentProfileResponse>,
        request: TalentSearchRequest
    ): List<TalentProfileResponse> {
        var filtered = profiles

        // Filter by minimum average rating
        request.minAverageRating?.let { minRating ->
            filtered = filtered.filter { (it.averageRating ?: 0.0) >= minRating }
        }

        // Filter by tiers
        request.tiers?.let { tiers ->
            if (tiers.isNotEmpty()) {
                filtered = filtered.filter { it.tier in tiers }
            }
        }

        // Filter by minimum completed collaborations
        request.minCompletedCollaborations?.let { minCollabs ->
            filtered = filtered.filter { (it.completedCollaborations ?: 0) >= minCollabs }
        }

        return filtered
    }
}
