package com.castify.web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Returns 401 for requests that arrive without usable credentials.
 *
 * Without an entry point Spring Security answers anonymous requests to
 * protected routes with 403, which tells a client "you may not do this" when
 * the truth is "you are not signed in" - the two need different handling in the
 * frontend (redirect to login vs. show a permission error).
 *
 * The body mirrors GlobalExceptionHandler's ErrorResponse so every error the
 * API emits has the same shape, whether it comes from the filter chain or from
 * a controller.
 *
 * The payload is written by hand rather than through an injected ObjectMapper:
 * Spring Boot 4 auto-configures Jackson 3's tools.jackson.databind.ObjectMapper
 * while Jackson 2 is also on the classpath via jjwt-jackson, so injecting by
 * that type is ambiguous and ties this class to whichever version wins.
 */
@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val correlationId = request.getHeader("X-Correlation-ID")
            ?: request.getHeader("X-Request-ID")
            ?: UUID.randomUUID().toString()

        // path and correlationId originate from the request (the latter from a
        // client-supplied header), so both are escaped before being embedded.
        val body = """
            {"status":${HttpStatus.UNAUTHORIZED.value()},"errorCode":"UNAUTHORIZED","message":"Authentication required","path":"${escape(request.requestURI)}","timestamp":"${Instant.now()}","correlationId":"${escape(correlationId)}"}
        """.trimIndent()

        response.writer.write(body)
    }

    private fun escape(value: String): String = buildString(value.length) {
        for (ch in value) {
            when {
                ch == '"' -> append("\\\"")
                ch == '\\' -> append("\\\\")
                ch == '\n' -> append("\\n")
                ch == '\r' -> append("\\r")
                ch == '\t' -> append("\\t")
                ch < ' ' -> append("\\u%04x".format(ch.code))
                else -> append(ch)
            }
        }
    }
}
