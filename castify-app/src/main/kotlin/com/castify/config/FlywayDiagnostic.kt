package com.castify.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Diagnostic configuration to check Flyway status on application startup
 */
@Configuration
class FlywayDiagnostic {

    private val logger = LoggerFactory.getLogger(FlywayDiagnostic::class.java)

    @Bean
    fun flywayDiagnosticRunner(dataSource: DataSource, flywayList: List<Flyway>): ApplicationRunner {
        return ApplicationRunner {
            logger.info("=================================================================")
            logger.info("FLYWAY DIAGNOSTIC REPORT")
            logger.info("=================================================================")

            val flyway = flywayList.firstOrNull()

            if (flyway == null) {
                logger.error("❌ No Flyway beans found - Flyway is NOT configured!")
                logger.error("This means Spring Boot auto-configuration for Flyway failed")
            } else {
                logger.info("✅ Flyway bean found and injected")

                try {
                    val configuration = flyway.configuration
                    logger.info("Flyway Configuration:")
                    logger.info("  - Locations: {}", configuration.locations.joinToString(", "))
                    logger.info("  - Baseline on migrate: {}", configuration.isBaselineOnMigrate)
                    logger.info("  - Validate on migrate: {}", configuration.isValidateOnMigrate)
                    logger.info("  - Table: {}", configuration.table)

                    // Get migration info
                    val info = flyway.info()
                    val current = info.current()
                    val pending = info.pending()
                    val applied = info.applied()

                    logger.info("Migration Status:")
                    logger.info("  - Current version: {}", current?.version ?: "None")
                    logger.info("  - Applied migrations: {}", applied.size)
                    logger.info("  - Pending migrations: {}", pending.size)

                    if (applied.isNotEmpty()) {
                        logger.info("Applied Migrations:")
                        applied.forEach { migration ->
                            logger.info("  ✓ V{} - {}", migration.version, migration.description)
                        }
                    }

                    if (pending.isNotEmpty()) {
                        logger.warn("⚠️  Pending Migrations (NOT YET APPLIED):")
                        pending.forEach { migration ->
                            logger.warn("  ⏳ V{} - {}", migration.version, migration.description)
                        }
                        logger.warn("These migrations should have been applied automatically!")
                        logger.warn("Checking why they weren't applied...")

                        // Try to understand why
                        try {
                            logger.info("Attempting to run pending migrations manually...")
                            flyway.migrate()
                            logger.info("✅ Manual migration succeeded!")
                        } catch (e: Exception) {
                            logger.error("❌ Manual migration failed: {}", e.message, e)
                        }
                    } else {
                        logger.info("✅ No pending migrations - database is up to date")
                    }

                } catch (e: Exception) {
                    logger.error("❌ Error accessing Flyway info: {}", e.message, e)
                }
            }

            logger.info("=================================================================")
        }
    }
}
