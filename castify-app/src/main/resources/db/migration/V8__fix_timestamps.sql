-- =====================================================
-- Migration V8: Fix Timestamp Types to TIMESTAMPTZ
-- =====================================================
-- This migration converts all TIMESTAMP columns to TIMESTAMPTZ (HIGH - Issue #10)
-- Benefits:
-- 1. Multi-region deployment support
-- 2. Eliminates timezone ambiguity
-- 3. Proper DST handling
-- 4. Consistent with application's use of Instant (UTC)
-- =====================================================

-- =====================================================
-- Table: app_user
-- =====================================================
ALTER TABLE app_user
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: talent_profile
-- =====================================================
ALTER TABLE talent_profile
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: client_org
-- =====================================================
ALTER TABLE client_org
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: media_asset
-- =====================================================
ALTER TABLE media_asset
    ALTER COLUMN uploaded_at TYPE TIMESTAMPTZ USING uploaded_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: talent_portfolio_item
-- =====================================================
ALTER TABLE talent_portfolio_item
    ALTER COLUMN added_at TYPE TIMESTAMPTZ USING added_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: conversation
-- =====================================================
ALTER TABLE conversation
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN last_message_at TYPE TIMESTAMPTZ USING last_message_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: message
-- =====================================================
ALTER TABLE message
    ALTER COLUMN sent_at TYPE TIMESTAMPTZ USING sent_at AT TIME ZONE 'UTC',
    ALTER COLUMN read_at TYPE TIMESTAMPTZ USING read_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: talent_rating
-- =====================================================
ALTER TABLE talent_rating
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

-- =====================================================
-- Table: talent_statistics
-- =====================================================
ALTER TABLE talent_statistics
    ALTER COLUMN last_updated TYPE TIMESTAMPTZ USING last_updated AT TIME ZONE 'UTC';

-- =====================================================
-- Table: collaboration
-- =====================================================
ALTER TABLE collaboration
    ALTER COLUMN start_date TYPE TIMESTAMPTZ USING start_date AT TIME ZONE 'UTC',
    ALTER COLUMN end_date TYPE TIMESTAMPTZ USING end_date AT TIME ZONE 'UTC',
    ALTER COLUMN proposed_at TYPE TIMESTAMPTZ USING proposed_at AT TIME ZONE 'UTC',
    ALTER COLUMN responded_at TYPE TIMESTAMPTZ USING responded_at AT TIME ZONE 'UTC',
    ALTER COLUMN completed_at TYPE TIMESTAMPTZ USING completed_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- =====================================================
-- Update default values to use CURRENT_TIMESTAMP (now returns TIMESTAMPTZ)
-- =====================================================

ALTER TABLE app_user
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE talent_profile
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE client_org
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE media_asset
    ALTER COLUMN uploaded_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE talent_portfolio_item
    ALTER COLUMN added_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE conversation
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN last_message_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE message
    ALTER COLUMN sent_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE talent_rating
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE talent_statistics
    ALTER COLUMN last_updated SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE collaboration
    ALTER COLUMN proposed_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- Add comments
-- =====================================================
COMMENT ON COLUMN app_user.created_at IS 'User creation timestamp (UTC)';
COMMENT ON COLUMN app_user.updated_at IS 'Last profile update timestamp (UTC)';
COMMENT ON COLUMN message.sent_at IS 'Message sent timestamp (UTC)';
COMMENT ON COLUMN collaboration.proposed_at IS 'Collaboration proposal timestamp (UTC)';
