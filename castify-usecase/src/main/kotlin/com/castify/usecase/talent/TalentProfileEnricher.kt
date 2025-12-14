package com.castify.usecase.talent

import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.talent.TalentProfile
import com.castify.usecase.dto.TalentProfileResponse
import org.springframework.stereotype.Component
import java.util.*

/**
 * Helper component to enrich talent profile responses with statistics data
 */
@Component
class TalentProfileEnricher(
    private val statisticsRepository: TalentStatisticsRepository
) {
    /**
     * Enrich a single talent profile with statistics
     */
    fun enrichProfile(profile: TalentProfile): TalentProfileResponse {
        val statistics = statisticsRepository.findByTalentProfileId(profile.talentProfileId)
        return enrichProfileWithStatistics(profile, statistics)
    }

    /**
     * Enrich multiple talent profiles with statistics (batch operation)
     */
    fun enrichProfiles(profiles: List<TalentProfile>): List<TalentProfileResponse> {
        if (profiles.isEmpty()) return emptyList()

        val profileIds = profiles.map { it.talentProfileId }
        val statisticsMap = statisticsRepository.findByTalentProfileIds(profileIds)
            .associateBy { it.talentProfileId }

        return profiles.map { profile ->
            enrichProfileWithStatistics(profile, statisticsMap[profile.talentProfileId])
        }
    }

    private fun enrichProfileWithStatistics(
        profile: TalentProfile,
        statistics: TalentStatistics?
    ): TalentProfileResponse {
        val baseResponse = TalentProfileResponse.fromDomain(profile)

        return if (statistics != null) {
            baseResponse.copy(
                tier = statistics.tier,
                averageRating = statistics.averageRating,
                totalRatings = statistics.totalRatings,
                completedCollaborations = statistics.completedCollaborations,
                successRate = statistics.successRate
            )
        } else {
            baseResponse
        }
    }
}
