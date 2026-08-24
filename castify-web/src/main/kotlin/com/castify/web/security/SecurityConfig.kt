package com.castify.web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    // Only the file-serving GETs are public. The upload POSTs
                    // all resolve the caller through SecurityUtils, so leaving
                    // /api/upload/** open let unauthenticated clients reach
                    // them and fail deep inside the handler instead of at the
                    // security layer.
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/upload/images/**",
                        "/api/upload/videos/**",
                        "/api/upload/portfolio/**"
                    ).permitAll()
                    .requestMatchers("/api/talents/search", "/api/talents/*", "/api/talents/user/*").permitAll()
                    .requestMatchers("/api/statistics/**", "/api/ratings/talent/**", "/api/leaderboard").permitAll()
                    .requestMatchers("/api/media/portfolio/talent/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                    // Liveness/readiness for the container HEALTHCHECK and the deploy workflow.
                    // Only /health is opened; details stay hidden (show-details: when-authorized).
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    .anyRequest().authenticated()
            }
            // Anonymous requests to protected routes answer 401, not 403.
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
