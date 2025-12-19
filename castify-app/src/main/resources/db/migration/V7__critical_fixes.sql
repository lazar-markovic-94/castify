-- =====================================================
-- Migration V7: Critical Fixes
-- =====================================================
-- This migration addresses critical issues identified in the schema review:
-- 1. Fix collaboration FK cascade (CRITICAL - Issue #4)
-- 2. Add version columns for optimistic locking (HIGH - Issue #15)
-- 3. Fix location constraints for SPECIFIC mode (HIGH - Issue #3)
-- 4. Add unique constraint on portfolio display order (MEDIUM - Issue #5)
-- 5. Change password_hash to TEXT for future-proofing (LOW - Issue #12)
-- =====================================================

-- =====================================================
-- CRITICAL FIX 1: Fix talent_rating.collaboration_id FK cascade
-- =====================================================
-- Change from ON DELETE SET NULL to ON DELETE RESTRICT
-- This prevents orphaned ratings and maintains data integrity

-- Drop existing constraint
ALTER TABLE talent_rating
    DROP CONSTRAINT IF EXISTS fk_rating_collaboration;

-- Recreate with RESTRICT to prevent deletion of collaborations with ratings
ALTER TABLE talent_rating
    ADD CONSTRAINT fk_rating_collaboration FOREIGN KEY (collaboration_id)
        REFERENCES collaboration(collaboration_id) ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_rating_collaboration ON talent_rating IS
    'Prevents deletion of collaborations that have associated ratings. Use soft delete pattern instead.';

-- =====================================================
-- CRITICAL FIX 2: Add version columns for optimistic locking
-- =====================================================
-- Adds version columns to entities with concurrent update potential

-- collaboration table
ALTER TABLE collaboration
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN collaboration.version IS
    'Optimistic locking version. Incremented on each update to prevent lost updates.';

-- talent_statistics table
ALTER TABLE talent_statistics
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN talent_statistics.version IS
    'Optimistic locking version. Prevents race conditions during statistics recalculation.';

-- app_user table
ALTER TABLE app_user
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN app_user.version IS
    'Optimistic locking version. Prevents concurrent profile updates from overwriting each other.';

-- talent_profile table
ALTER TABLE talent_profile
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN talent_profile.version IS
    'Optimistic locking version. Ensures profile consistency during concurrent edits.';

-- =====================================================
-- HIGH FIX 3: Add location constraint for SPECIFIC mode
-- =====================================================
-- Ensures latitude/longitude are NOT NULL when location_mode = 'SPECIFIC'

ALTER TABLE talent_profile
    ADD CONSTRAINT chk_location_specific CHECK (
        location_mode != 'SPECIFIC' OR
        (latitude IS NOT NULL AND longitude IS NOT NULL AND city IS NOT NULL AND country IS NOT NULL)
    );

COMMENT ON CONSTRAINT chk_location_specific ON talent_profile IS
    'Ensures location fields are populated when location_mode is SPECIFIC';

-- =====================================================
-- MEDIUM FIX 4: Add unique constraint on portfolio display order
-- =====================================================
-- Prevents duplicate display_order values per talent profile

ALTER TABLE talent_portfolio_item
    ADD CONSTRAINT uk_portfolio_order_per_talent
    UNIQUE (talent_profile_id, display_order);

COMMENT ON CONSTRAINT uk_portfolio_order_per_talent ON talent_portfolio_item IS
    'Ensures each portfolio item has a unique display order within a talent profile';

-- =====================================================
-- LOW FIX 5: Change password_hash to TEXT
-- =====================================================
-- Future-proofs for longer hash algorithms (Argon2, scrypt)

ALTER TABLE app_user
    ALTER COLUMN password_hash TYPE TEXT;

COMMENT ON COLUMN app_user.password_hash IS
    'Hashed password. Uses TEXT type to accommodate future hashing algorithms.';

-- =====================================================
-- QUICK WIN: Rename tier column for clarity
-- =====================================================

ALTER TABLE talent_statistics
    RENAME COLUMN tier TO performance_tier;

COMMENT ON COLUMN talent_statistics.performance_tier IS
    'Performance tier based on ratings and collaborations: PREMIUM (top 5%), GOLD (top 20%), SILVER (top 50%), BRONZE (rest)';

-- =====================================================
-- Add indexes for version columns (optimize locking queries)
-- =====================================================

CREATE INDEX idx_collaboration_version ON collaboration(version);
CREATE INDEX idx_talent_statistics_version ON talent_statistics(version);
CREATE INDEX idx_app_user_version ON app_user(version);
CREATE INDEX idx_talent_profile_version ON talent_profile(version);
