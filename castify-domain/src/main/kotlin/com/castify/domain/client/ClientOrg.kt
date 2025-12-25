package com.castify.domain.client

import java.time.Instant
import java.util.*

data class ClientOrg(
    val clientOrgId: UUID,
    val userId: UUID,
    val orgName: String,
    val orgType: OrgType,
    val description: String? = null,
    val website: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null,
    val logoUrl: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(orgName.isNotBlank()) { "Organization name cannot be blank" }
        if (website != null) {
            require(website.matches(Regex("^https?://.*"))) { "Website must start with http:// or https://" }
        }
        if (contactEmail != null) {
            require(contactEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                "Invalid contact email format"
            }
        }
    }

    fun updateDescription(newDescription: String?): ClientOrg =
        copy(description = newDescription, updatedAt = Instant.now())

    fun updateWebsite(newWebsite: String?): ClientOrg =
        copy(website = newWebsite, updatedAt = Instant.now())

    fun updateContactInfo(email: String?, phone: String?): ClientOrg =
        copy(contactEmail = email, contactPhone = phone, updatedAt = Instant.now())

    fun updateAddress(newAddress: String?): ClientOrg =
        copy(address = newAddress, updatedAt = Instant.now())

    fun updateLogoUrl(newLogoUrl: String?): ClientOrg =
        copy(logoUrl = newLogoUrl, updatedAt = Instant.now())
}

enum class OrgType {
    INDIVIDUAL,
    PRODUCTION_COMPANY,
    AGENCY,
    BRAND
}
