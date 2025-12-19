package com.castify.persistence.jpa

import com.castify.persistence.entity.ClientOrgEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientOrgJpaRepository : JpaRepository<ClientOrgEntity, UUID> {
    fun findByUserId(userId: UUID): ClientOrgEntity?
}
