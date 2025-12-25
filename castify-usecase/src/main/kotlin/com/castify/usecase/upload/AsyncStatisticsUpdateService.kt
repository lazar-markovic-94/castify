package com.castify.usecase.upload

import com.castify.usecase.rating.UpdateTalentStatisticsUseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service for updating talent statistics asynchronously in a separate transaction.
 *
 * This service is intentionally separated from upload use cases to ensure proper
 * Spring AOP proxy interception. When statistics updates are called from within
 * the same class, Spring cannot intercept the call and REQUIRES_NEW propagation
 * won't work, causing transaction rollback issues.
 *
 * By having a separate service, Spring can properly create a proxy and manage
 * the independent transaction lifecycle.
 */
@Service
class AsyncStatisticsUpdateService(
    private val updateTalentStatisticsUseCase: UpdateTalentStatisticsUseCase
) {
    private val logger = LoggerFactory.getLogger(AsyncStatisticsUpdateService::class.java)

    /**
     * Updates profile completeness statistics in a new, independent transaction.
     *
     * This method uses NOT_SUPPORTED propagation to ensure that:
     * 1. It suspends any existing transaction from the caller
     * 2. Runs independently without affecting the parent transaction
     * 3. If it fails, it won't affect the caller's transaction
     *
     * Implements retry logic for optimistic locking failures, which can occur
     * when multiple requests update the same talent's statistics concurrently.
     *
     * Failures are logged but not propagated, as statistics updates are
     * considered non-critical and should not block file uploads.
     *
     * @param talentProfileId The ID of the talent profile to update statistics for
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun updateProfileCompleteness(talentProfileId: UUID) {
        logger.debug("Starting async profile completeness update for talent: $talentProfileId")
        var attempts = 0
        val maxAttempts = 3
        var lastException: Exception? = null

        while (attempts < maxAttempts) {
            try {
                logger.debug("Attempting profile completeness update for $talentProfileId (attempt ${attempts + 1}/$maxAttempts)")
                // Run in a completely new transaction, independent of the caller
                updateTalentStatisticsUseCase.updateProfileCompleteness(talentProfileId)
                logger.info("Successfully updated profile completeness statistics for talent: $talentProfileId (attempt ${attempts + 1})")
                return // Success - exit method
            } catch (e: Exception) {
                logger.warn("Exception during statistics update for $talentProfileId: ${e.javaClass.simpleName} - ${e.message}", e)

                // Check if this is an optimistic locking failure
                val isOptimisticLocking = e.javaClass.simpleName.contains("OptimisticLocking") ||
                        e.javaClass.simpleName.contains("StaleObject") ||
                        e.message?.contains("already updated or deleted") == true

                if (isOptimisticLocking) {
                    attempts++
                    lastException = e
                    if (attempts < maxAttempts) {
                        // Optimistic locking failure - another transaction updated the entity
                        // Wait a bit and retry
                        val backoffMs = attempts * 50L // Incremental backoff: 50ms, 100ms, 150ms
                        logger.info("Optimistic locking failure updating statistics for $talentProfileId, retrying in ${backoffMs}ms (attempt $attempts/$maxAttempts)")
                        Thread.sleep(backoffMs)
                    } else {
                        logger.warn("Failed to update profile completeness for $talentProfileId after $maxAttempts attempts due to optimistic locking")
                    }
                } else {
                    // Other exceptions - log and give up immediately
                    logger.error("Failed to update profile completeness for talent $talentProfileId: ${e.javaClass.simpleName} - ${e.message}", e)
                    return
                }
            }
        }

        // If we get here, all retries failed
        logger.error("Failed to update profile completeness for talent $talentProfileId after $maxAttempts attempts: ${lastException?.message}", lastException)
    }
}
