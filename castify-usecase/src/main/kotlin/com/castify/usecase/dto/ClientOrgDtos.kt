package com.castify.usecase.dto

import com.castify.domain.client.ClientOrg
import com.castify.domain.client.OrgType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

data class CreateClientOrgRequest(
    @field:NotBlank(message = "Organization name is required")
    val orgName: String?,
    val orgType: OrgType?,
    val description: String? = null,
    val website: String? = null,
    @field:Email(message = "Invalid contact email format")
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null,
    val logoUrl: String? = null
)

data class UpdateClientOrgRequest(
    val orgName: String? = null,
    val orgType: OrgType? = null,
    val description: String? = null,
    val website: String? = null,
    @field:Email(message = "Invalid contact email format")
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null,
    val logoUrl: String? = null
)

data class ClientOrgResponse(
    val clientOrgId: UUID,
    val userId: UUID,
    val orgName: String,
    val orgType: OrgType,
    val description: String?,
    val website: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val address: String?,
    val logoUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(clientOrg: ClientOrg): ClientOrgResponse = ClientOrgResponse(
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
