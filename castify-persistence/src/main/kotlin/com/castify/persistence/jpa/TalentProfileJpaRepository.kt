package com.castify.persistence.jpa

import com.castify.persistence.entity.TalentProfileEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TalentProfileJpaRepository : JpaRepository<TalentProfileEntity, UUID> {
    fun findByUserId(userId: UUID): TalentProfileEntity?

    @EntityGraph(attributePaths = ["languages", "tags", "socialLinks"])
    override fun findById(id: UUID): Optional<TalentProfileEntity>

    @EntityGraph(attributePaths = ["languages", "tags", "socialLinks"])
    @Query("SELECT t FROM TalentProfileEntity t WHERE t.talentProfileId IN :ids")
    fun findAllByIdWithCollections(@Param("ids") ids: List<UUID>): List<TalentProfileEntity>

    @EntityGraph(attributePaths = ["languages", "tags", "socialLinks"])
    @Query("SELECT t FROM TalentProfileEntity t WHERE t.userId = :userId")
    fun findByUserIdWithCollections(@Param("userId") userId: UUID): TalentProfileEntity?
}
