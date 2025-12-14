package com.castify.domain.client

import java.util.*

interface ClientOrgRepository {
    fun save(clientOrg: ClientOrg): ClientOrg
    fun findById(clientOrgId: UUID): ClientOrg?
    fun findByUserId(userId: UUID): ClientOrg?
}
