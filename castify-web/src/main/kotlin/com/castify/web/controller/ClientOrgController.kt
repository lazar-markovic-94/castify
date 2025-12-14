package com.castify.web.controller

import com.castify.usecase.client.CreateOrUpdateClientOrgUseCase
import com.castify.usecase.client.GetClientOrgUseCase
import com.castify.usecase.dto.ClientOrgResponse
import com.castify.usecase.dto.CreateClientOrgRequest
import com.castify.usecase.dto.UpdateClientOrgRequest
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Client Organization", description = "Client organization management endpoints")
class ClientOrgController(
    private val createOrUpdateClientOrgUseCase: CreateOrUpdateClientOrgUseCase,
    private val getClientOrgUseCase: GetClientOrgUseCase
) {

    @PostMapping("/organization")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or update client organization")
    fun createOrUpdateOrganization(@Valid @RequestBody request: CreateClientOrgRequest): ClientOrgResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return createOrUpdateClientOrgUseCase.execute(userId, request)
    }

    @PutMapping("/organization")
    @Operation(summary = "Update client organization")
    fun updateOrganization(@Valid @RequestBody request: UpdateClientOrgRequest): ClientOrgResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return createOrUpdateClientOrgUseCase.update(userId, request)
    }

    @GetMapping("/organization/{clientOrgId}")
    @Operation(summary = "Get client organization by ID")
    fun getOrganization(@PathVariable clientOrgId: UUID): ClientOrgResponse {
        return getClientOrgUseCase.execute(clientOrgId)
    }

    @GetMapping("/my-organization")
    @Operation(summary = "Get current user's client organization")
    fun getMyOrganization(): ClientOrgResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return getClientOrgUseCase.executeByUserId(userId)
    }

    @GetMapping("/organization/user/{userId}")
    @Operation(summary = "Get client organization by user ID")
    fun getOrganizationByUserId(@PathVariable userId: UUID): ClientOrgResponse {
        return getClientOrgUseCase.executeByUserId(userId)
    }
}
