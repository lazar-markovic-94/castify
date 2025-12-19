-- =====================================================
-- Migration V11: Add version column to talent_rating
-- =====================================================
-- This migration adds the missing version column for optimistic locking

ALTER TABLE talent_rating
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN talent_rating.version IS 'Optimistic locking version number';
