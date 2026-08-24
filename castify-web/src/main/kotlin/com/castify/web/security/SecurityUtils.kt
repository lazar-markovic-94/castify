package com.castify.web.security

import com.castify.domain.exception.UnauthorizedException
import com.castify.domain.user.UserRole
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object SecurityUtils {

    /**
     * The ID of the authenticated caller.
     *
     * JwtAuthenticationFilter stores the user's UUID as the principal. On an
     * unauthenticated request Spring Security substitutes an anonymous token
     * whose principal is the String "anonymousUser", so the type has to be
     * checked rather than cast - a blind cast surfaces to the client as a
     * ClassCastException and HTTP 500 instead of 401.
     */
    fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("Authentication required")

        if (authentication is AnonymousAuthenticationToken || !authentication.isAuthenticated) {
            throw UnauthorizedException("Authentication required")
        }

        return authentication.principal as? UUID
            ?: throw UnauthorizedException("Authentication required")
    }

    fun getCurrentUserRole(): UserRole {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("Authentication required")

        if (authentication is AnonymousAuthenticationToken || !authentication.isAuthenticated) {
            throw UnauthorizedException("Authentication required")
        }

        val roleAuthority = authentication.authorities.firstOrNull()?.authority
            ?: throw UnauthorizedException("Authentication required")

        // An unrecognised authority means the token carries a role this build
        // does not know about; treat it as unauthenticated rather than letting
        // valueOf throw IllegalArgumentException and surface as HTTP 400.
        return runCatching { UserRole.valueOf(roleAuthority.removePrefix("ROLE_")) }
            .getOrElse { throw UnauthorizedException("Authentication required") }
    }
}
