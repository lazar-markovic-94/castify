package com.castify.usecase.auth

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.user.UserRepository
import com.castify.usecase.dto.UserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Use case for retrieving current user information.
 */
@Service
class GetCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(GetCurrentUserUseCase::class.java)

    /**
     * Retrieves user information by user ID.
     *
     * @param userId The user ID
     * @return User information response
     * @throws ResourceNotFoundException if user is not found
     */
    fun execute(userId: UUID): UserInfoResponse {
        return try {
            logger.info("Fetching user info for: $userId")

            val user = userRepository.findById(userId)
                ?: throw ResourceNotFoundException("User not found: $userId")

            UserInfoResponse(
                userId = user.userId,
                email = user.email,
                role = user.role,
                phone = user.phone,
                isActive = user.isActive
            )

        } catch (e: ResourceNotFoundException) {
            logger.error("User not found: $userId", e)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching user info for: $userId", e)
            throw RuntimeException("Failed to fetch user info: ${e.message}", e)
        }
    }
}
