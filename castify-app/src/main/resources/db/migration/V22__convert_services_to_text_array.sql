-- =====================================================
-- Migration V22: Convert services_required from service_type[] to TEXT[]
-- =====================================================
-- This migration converts the enum array to a text array for better flexibility
-- =====================================================

-- Drop the constraint first
ALTER TABLE collaboration
    DROP CONSTRAINT IF EXISTS check_at_least_one_service;

-- Drop the index
DROP INDEX IF EXISTS idx_collaboration_services;

-- Convert the column type from service_type[] to TEXT[]
-- PostgreSQL will automatically convert enum values to text
ALTER TABLE collaboration
    ALTER COLUMN services_required TYPE TEXT[] USING services_required::TEXT[];

-- Drop the enum type (only if no other tables use it)
DROP TYPE IF EXISTS service_type;

-- Re-add the constraint
ALTER TABLE collaboration
    ADD CONSTRAINT check_at_least_one_service
    CHECK (array_length(services_required, 1) > 0);

-- Re-create the GIN index
CREATE INDEX idx_collaboration_services ON collaboration USING GIN(services_required);

-- Update the column comment
COMMENT ON COLUMN collaboration.services_required IS 'Array of service types required for this collaboration (VOICE_OVER, ACTING_FROM_HOME, ACTING_ON_LOCATION, PHOTO_SHOOTING, FULL_VIDEO_PRODUCTION)';
