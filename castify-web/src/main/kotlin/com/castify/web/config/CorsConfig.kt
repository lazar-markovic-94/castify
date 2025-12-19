package com.castify.web.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String
) {

    private val logger = LoggerFactory.getLogger(CorsConfig::class.java)

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Parse allowed origins from comma-separated string
        val origins = allowedOrigins.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        logger.info("Configuring CORS with allowed origins: $origins")

        // Validate that production environments don't use localhost
        val activeProfile = System.getenv("SPRING_PROFILES_ACTIVE") ?: "local"
        if (activeProfile == "production" && origins.any { it.contains("localhost") || it.contains("127.0.0.1") }) {
            logger.warn("WARNING: Production profile is using localhost origins! This is insecure.")
        }

        configuration.allowedOrigins = origins

        // Allow all HTTP methods
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

        // Allow all headers
        configuration.allowedHeaders = listOf("*")

        // Allow credentials (cookies, authorization headers)
        configuration.allowCredentials = true

        // Cache preflight response for 1 hour
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        logger.info("CORS configuration initialized successfully")
        return source
    }
}
