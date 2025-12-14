package com.castify.web.security

import com.castify.domain.user.UserRole
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object SecurityUtils {

    fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found")
        return authentication.principal as UUID
    }

    fun getCurrentUserRole(): UserRole {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found")
        val roleAuthority = authentication.authorities.firstOrNull()?.authority
            ?: throw IllegalStateException("No role found")
        return UserRole.valueOf(roleAuthority.removePrefix("ROLE_"))
    }
}
