package com.castify.persistence.adapter

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.ClientOrgRepository
import com.castify.persistence.entity.ClientOrgEntity
import com.castify.persistence.jpa.ClientOrgJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClientOrgRepositoryAdapter(
    private val clientOrgJpaRepository: ClientOrgJpaRepository
) : ClientOrgRepository {

    override fun save(clientOrg: ClientOrg): ClientOrg {
        val entity = ClientOrgEntity.fromDomain(clientOrg)
        val saved = clientOrgJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(clientOrgId: UUID): ClientOrg? {
        return clientOrgJpaRepository.findById(clientOrgId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: UUID): ClientOrg? {
        return clientOrgJpaRepository.findByUserId(userId)?.toDomain()
    }
}
