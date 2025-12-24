package com.castify.web.controller

import com.castify.usecase.dto.*
import com.castify.usecase.talent.*
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/talents")
@Tag(name = "Talent", description = "Talent profile management endpoints")
class TalentController(
    private val createOrUpdateTalentProfileUseCase: CreateOrUpdateTalentProfileUseCase,
    private val getTalentProfileUseCase: GetTalentProfileUseCase,
    private val updateTalentLocationUseCase: UpdateTalentLocationUseCase,
    private val manageTalentGeoAreasUseCase: ManageTalentGeoAreasUseCase,
    private val updateTalentLanguagesUseCase: UpdateTalentLanguagesUseCase,
    private val updateTalentTagsUseCase: UpdateTalentTagsUseCase,
    private val updateTalentCapabilitiesUseCase: UpdateTalentCapabilitiesUseCase,
    private val updateTalentIntroductionVideoUseCase: UpdateTalentIntroductionVideoUseCase,
    private val searchTalentsUseCase: SearchTalentsUseCase
) {

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or update talent profile")
    fun createOrUpdateProfile(@Valid @RequestBody request: CreateTalentProfileRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return createOrUpdateTalentProfileUseCase.execute(userId, request)
    }

    @GetMapping("/{talentProfileId}")
    @Operation(summary = "Get talent profile by ID")
    fun getProfile(@PathVariable talentProfileId: UUID): TalentProfileResponse {
        return getTalentProfileUseCase.execute(talentProfileId)
    }

    @GetMapping("/my-profile")
    @Operation(summary = "Get current user's talent profile")
    fun getMyProfile(): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return getTalentProfileUseCase.executeByUserId(userId)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get talent profile by user ID")
    fun getProfileByUserId(@PathVariable userId: UUID): TalentProfileResponse {
        return getTalentProfileUseCase.executeByUserId(userId)
    }

    @PutMapping("/profile-image")
    @Operation(summary = "Update talent profile image")
    fun updateProfileImage(@RequestBody request: Map<String, String>): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        val imageUrl = request["profileImageUrl"]
            ?: throw com.castify.domain.exception.BadRequestException("Profile image URL is required")
        return createOrUpdateTalentProfileUseCase.updateProfileImage(userId, imageUrl)
    }

    @PutMapping("/primary-location")
    @Operation(summary = "Update talent primary location")
    fun updatePrimaryLocation(@Valid @RequestBody request: UpdatePrimaryLocationRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return updateTalentLocationUseCase.execute(userId, request)
    }

    @PostMapping("/geo-areas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new geographical service area")
    fun addGeoArea(@Valid @RequestBody request: AddGeoAreaRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return manageTalentGeoAreasUseCase.addGeoArea(userId, request)
    }

    @DeleteMapping("/geo-areas")
    @Operation(summary = "Remove a geographical service area")
    fun removeGeoArea(@Valid @RequestBody request: RemoveGeoAreaRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return manageTalentGeoAreasUseCase.removeGeoArea(userId, request)
    }

    @PutMapping("/geo-areas")
    @Operation(summary = "Update all geographical service areas (bulk update)")
    fun updateGeoAreas(@Valid @RequestBody request: UpdateGeoAreasRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return manageTalentGeoAreasUseCase.updateGeoAreas(userId, request)
    }

    @PutMapping("/languages")
    @Operation(summary = "Update talent languages")
    fun updateLanguages(@Valid @RequestBody request: UpdateLanguagesRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return updateTalentLanguagesUseCase.execute(userId, request)
    }

    @PutMapping("/tags")
    @Operation(summary = "Update talent tags")
    fun updateTags(@Valid @RequestBody request: UpdateTagsRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return updateTalentTagsUseCase.execute(userId, request)
    }

    @PutMapping("/capabilities")
    @Operation(summary = "Update talent capabilities")
    fun updateCapabilities(@Valid @RequestBody request: UpdateCapabilitiesRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return updateTalentCapabilitiesUseCase.execute(userId, request)
    }

    @PutMapping("/introduction-video")
    @Operation(summary = "Update talent introduction video (max 30 seconds)")
    fun updateIntroductionVideo(@Valid @RequestBody request: UpdateIntroductionVideoRequest): TalentProfileResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return updateTalentIntroductionVideoUseCase.execute(userId, request)
    }

    @PostMapping("/search")
    @Operation(summary = "Search talents with pagination")
    fun searchTalents(@RequestBody request: TalentSearchRequest): PagedResult<TalentProfileResponse> {
        return searchTalentsUseCase.execute(request)
    }

    @GetMapping("/available-tags")
    @Operation(summary = "Get available talent tags (primary and secondary)")
    fun getAvailableTags(): AvailableTagsResponse {
        return AvailableTagsResponse.create()
    }
}
