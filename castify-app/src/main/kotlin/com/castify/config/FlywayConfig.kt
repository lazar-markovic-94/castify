package com.castify.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Manual Flyway configuration to ensure migrations run on startup.
 *
 * This configuration creates the Flyway bean manually because Spring Boot's
 * auto-configuration might not be working properly in our multi-module setup.
 */
@Configuration
class FlywayConfig {

    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    @Bean
    fun flyway(dataSource: DataSource): Flyway {
        logger.info("Manually configuring Flyway bean...")

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .table("flyway_schema_history")
            .load()

        logger.info("Running Flyway migrations...")
        try {
            val result = flyway.migrate()
            logger.info("✅ Flyway migration completed successfully!")
            logger.info("Migrations executed: {}", result.migrationsExecuted)
            logger.info("Target version: {}", result.targetSchemaVersion ?: "latest")
        } catch (e: Exception) {
            logger.error("❌ Flyway migration failed: {}", e.message, e)
            throw e
        }

        return flyway
    }
}
