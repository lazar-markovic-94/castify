-- =====================================================
-- Migration V21: Add Services Required and Deadline to Collaboration
-- =====================================================
-- This migration adds support for:
-- 1. Service type selection (voice over, acting, photo shooting, etc.)
-- 2. Deadline field for collaboration completion
-- =====================================================

-- =====================================================
-- Create ServiceType enum
-- =====================================================
CREATE TYPE service_type AS ENUM (
    'VOICE_OVER',
    'ACTING_FROM_HOME',
    'ACTING_ON_LOCATION',
    'PHOTO_SHOOTING',
    'FULL_VIDEO_PRODUCTION'
);

-- =====================================================
-- Add new columns to collaboration table
-- =====================================================
ALTER TABLE collaboration
    ADD COLUMN services_required service_type[] NOT NULL DEFAULT ARRAY[]::service_type[],
    ADD COLUMN deadline TIMESTAMP;

-- =====================================================
-- Add constraint to ensure at least one service is selected
-- =====================================================
ALTER TABLE collaboration
    ADD CONSTRAINT check_at_least_one_service
    CHECK (array_length(services_required, 1) > 0);

-- =====================================================
-- Create index for service type queries
-- =====================================================
CREATE INDEX idx_collaboration_services ON collaboration USING GIN(services_required);

-- =====================================================
-- Create index for deadline queries
-- =====================================================
CREATE INDEX idx_collaboration_deadline ON collaboration(deadline) WHERE deadline IS NOT NULL;

-- =====================================================
-- Comments for documentation
-- =====================================================
COMMENT ON COLUMN collaboration.services_required IS 'Array of service types required for this collaboration (voice over, acting from home, acting on location, photo shooting, full video production)';
COMMENT ON COLUMN collaboration.deadline IS 'Optional deadline by which the collaboration should be completed';
