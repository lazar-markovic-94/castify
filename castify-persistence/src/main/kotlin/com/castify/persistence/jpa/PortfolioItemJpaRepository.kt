package com.castify.persistence.jpa

import com.castify.persistence.entity.PortfolioItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PortfolioItemJpaRepository : JpaRepository<PortfolioItemEntity, UUID> {
    fun findByTalentProfileId(talentProfileId: UUID): List<PortfolioItemEntity>
}
