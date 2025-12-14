package com.castify.web.security

import com.castify.usecase.port.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    @Value("\${app.jwt.expiration-ms}")
    private val expirationMs: Long
) : TokenService {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun generateToken(userId: UUID, email: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    override fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    override fun getUserIdFromToken(token: String): UUID {
        val claims = extractClaims(token)
        return UUID.fromString(claims.subject)
    }

    override fun getEmailFromToken(token: String): String {
        val claims = extractClaims(token)
        return claims["email"] as String
    }

    override fun getRoleFromToken(token: String): String {
        val claims = extractClaims(token)
        return claims["role"] as String
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
