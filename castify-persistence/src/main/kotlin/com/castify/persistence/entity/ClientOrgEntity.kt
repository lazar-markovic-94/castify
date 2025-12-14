package com.castify.persistence.entity

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.OrgType
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "client_org")
class ClientOrgEntity(
    @Id
    @Column(name = "client_org_id", columnDefinition = "UUID")
    var clientOrgId: UUID = UUID.randomUUID(),

    @Column(name = "user_id", unique = true, nullable = false, columnDefinition = "UUID")
    var userId: UUID,

    @Column(name = "org_name", nullable = false)
    var orgName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false, length = 50)
    var orgType: OrgType,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var website: String? = null,

    @Column(name = "contact_email")
    var contactEmail: String? = null,

    @Column(name = "contact_phone", length = 20)
    var contactPhone: String? = null,

    @Column(columnDefinition = "TEXT")
    var address: String? = null,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    fun toDomain(): ClientOrg = ClientOrg(
        clientOrgId = clientOrgId,
        userId = userId,
        orgName = orgName,
        orgType = orgType,
        description = description,
        website = website,
        contactEmail = contactEmail,
        contactPhone = contactPhone,
        address = address,
        logoUrl = logoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(clientOrg: ClientOrg): ClientOrgEntity = ClientOrgEntity(
            clientOrgId = clientOrg.clientOrgId,
            userId = clientOrg.userId,
            orgName = clientOrg.orgName,
            orgType = clientOrg.orgType,
            description = clientOrg.description,
            website = clientOrg.website,
            contactEmail = clientOrg.contactEmail,
            contactPhone = clientOrg.contactPhone,
            address = clientOrg.address,
            logoUrl = clientOrg.logoUrl,
            createdAt = clientOrg.createdAt,
            updatedAt = clientOrg.updatedAt
        )
    }
}
