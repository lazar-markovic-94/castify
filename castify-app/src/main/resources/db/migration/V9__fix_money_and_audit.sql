-- =====================================================
-- Migration V9: Fix Money Types and Add Audit Columns
-- =====================================================
-- This migration addresses:
-- 1. Fix money type from DOUBLE PRECISION to NUMERIC (MEDIUM - Issue #11)
-- 2. Add audit columns to critical tables (MEDIUM - Issue #13)
-- 3. Add CHECK constraints for data integrity
-- =====================================================

-- =====================================================
-- FIX 1: Change total_earnings from DOUBLE PRECISION to NUMERIC
-- =====================================================
-- Prevents floating-point rounding errors in financial calculations

ALTER TABLE talent_statistics
    ALTER COLUMN total_earnings TYPE NUMERIC(12, 2) USING ROUND(total_earnings::NUMERIC, 2);

COMMENT ON COLUMN talent_statistics.total_earnings IS
    'Total earnings from all completed collaborations. Uses NUMERIC for precise financial calculations.';

-- Update CHECK constraint to reflect new type
ALTER TABLE talent_statistics
    DROP CONSTRAINT IF EXISTS talent_statistics_total_earnings_check;

ALTER TABLE talent_statistics
    ADD CONSTRAINT talent_statistics_total_earnings_check
    CHECK (total_earnings >= 0.0);

-- =====================================================
-- FIX 2: Add audit columns to critical tables
-- =====================================================

-- app_user - track admin actions
ALTER TABLE app_user
    ADD COLUMN created_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL,
    ADD COLUMN updated_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL;

CREATE INDEX idx_app_user_created_by ON app_user(created_by);
CREATE INDEX idx_app_user_updated_by ON app_user(updated_by);

COMMENT ON COLUMN app_user.created_by IS 'User who created this account (admin/system)';
COMMENT ON COLUMN app_user.updated_by IS 'User who last updated this account';

-- talent_profile - track verification actions
ALTER TABLE talent_profile
    ADD COLUMN verified_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL,
    ADD COLUMN verified_at TIMESTAMPTZ;

CREATE INDEX idx_talent_profile_verified_by ON talent_profile(verified_by);

COMMENT ON COLUMN talent_profile.verified_by IS 'Admin user who verified this profile';
COMMENT ON COLUMN talent_profile.verified_at IS 'Timestamp when profile was verified (UTC)';

-- collaboration - track who created/updated
ALTER TABLE collaboration
    ADD COLUMN created_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL,
    ADD COLUMN updated_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL;

CREATE INDEX idx_collaboration_created_by ON collaboration(created_by);
CREATE INDEX idx_collaboration_updated_by ON collaboration(updated_by);

COMMENT ON COLUMN collaboration.created_by IS 'User who created this collaboration (typically the client)';
COMMENT ON COLUMN collaboration.updated_by IS 'User who last updated this collaboration';

-- talent_rating - track verification
ALTER TABLE talent_rating
    ADD COLUMN verified_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL,
    ADD COLUMN verified_at TIMESTAMPTZ;

CREATE INDEX idx_talent_rating_verified_by ON talent_rating(verified_by);

COMMENT ON COLUMN talent_rating.verified_by IS 'Admin who verified this rating as legitimate';
COMMENT ON COLUMN talent_rating.verified_at IS 'Timestamp when rating was verified (UTC)';

-- =====================================================
-- FIX 3: Add soft delete support for GDPR compliance
-- =====================================================

-- app_user soft delete
ALTER TABLE app_user
    ADD COLUMN deleted_at TIMESTAMPTZ,
    ADD COLUMN deleted_by UUID REFERENCES app_user(user_id) ON DELETE SET NULL;

CREATE INDEX idx_app_user_deleted_at ON app_user(deleted_at) WHERE deleted_at IS NOT NULL;

COMMENT ON COLUMN app_user.deleted_at IS 'Soft delete timestamp for GDPR compliance (UTC)';
COMMENT ON COLUMN app_user.deleted_by IS 'Admin who performed soft delete';

-- talent_profile soft delete
ALTER TABLE talent_profile
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_talent_profile_deleted_at ON talent_profile(deleted_at) WHERE deleted_at IS NOT NULL;

COMMENT ON COLUMN talent_profile.deleted_at IS 'Soft delete timestamp for GDPR compliance (UTC)';

-- collaboration soft delete
ALTER TABLE collaboration
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_collaboration_deleted_at ON collaboration(deleted_at) WHERE deleted_at IS NOT NULL;

COMMENT ON COLUMN collaboration.deleted_at IS 'Soft delete timestamp for GDPR compliance (UTC)';

-- message soft delete
ALTER TABLE message
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_message_not_deleted ON message(conversation_id, sent_at) WHERE deleted_at IS NULL;

COMMENT ON COLUMN message.deleted_at IS 'Soft delete timestamp for message retention policy (UTC)';

-- =====================================================
-- FIX 4: Add data retention metadata
-- =====================================================

ALTER TABLE message
    ADD COLUMN retention_until TIMESTAMPTZ;

CREATE INDEX idx_message_retention ON message(retention_until) WHERE retention_until IS NOT NULL;

COMMENT ON COLUMN message.retention_until IS 'Automatic deletion date per retention policy (UTC)';

-- =====================================================
-- Update existing records with default values
-- =====================================================

-- Set created_by to client_user_id for existing collaborations
UPDATE collaboration
SET created_by = client_user_id
WHERE created_by IS NULL;

-- Set retention_until for existing messages (e.g., 2 years from now)
UPDATE message
SET retention_until = sent_at + INTERVAL '2 years'
WHERE retention_until IS NULL AND deleted_at IS NULL;
