package com.castify.web.controller

import com.castify.usecase.dto.AddToPortfolioRequest
import com.castify.usecase.dto.CreateMediaAssetRequest
import com.castify.usecase.dto.MediaAssetResponse
import com.castify.usecase.dto.PortfolioItemResponse
import com.castify.usecase.dto.PortfolioItemWithMediaResponse
import com.castify.usecase.media.AddToPortfolioUseCase
import com.castify.usecase.media.CreateMediaAssetUseCase
import com.castify.usecase.media.GetPortfolioItemsUseCase
import com.castify.usecase.media.RemoveFromPortfolioUseCase
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "Media asset management endpoints")
class MediaController(
    private val createMediaAssetUseCase: CreateMediaAssetUseCase,
    private val addToPortfolioUseCase: AddToPortfolioUseCase,
    private val removeFromPortfolioUseCase: RemoveFromPortfolioUseCase,
    private val getPortfolioItemsUseCase: GetPortfolioItemsUseCase
) {

    @PostMapping("/assets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a media asset")
    fun createMediaAsset(@Valid @RequestBody request: CreateMediaAssetRequest): MediaAssetResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return createMediaAssetUseCase.execute(userId, request)
    }

    @PostMapping("/portfolio")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add media asset to portfolio")
    fun addToPortfolio(@Valid @RequestBody request: AddToPortfolioRequest): PortfolioItemResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return addToPortfolioUseCase.execute(userId, request)
    }

    @DeleteMapping("/portfolio/{portfolioItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove media asset from portfolio")
    fun removeFromPortfolio(@PathVariable portfolioItemId: UUID) {
        val userId = SecurityUtils.getCurrentUserId()
        removeFromPortfolioUseCase.execute(userId, portfolioItemId)
    }

    @GetMapping("/portfolio/talent/{talentProfileId}")
    @Operation(summary = "Get portfolio items for a talent")
    fun getPortfolioItems(@PathVariable talentProfileId: UUID): List<PortfolioItemWithMediaResponse> {
        return getPortfolioItemsUseCase.execute(talentProfileId)
    }
}
