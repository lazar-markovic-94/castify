-- Migration V12: Add multi-location GEO area support for talents
-- Rename existing location columns to primary_* to support primary location
-- Create new talent_geo_area table for multiple service locations

-- Step 1: Rename existing location columns to primary_location columns
ALTER TABLE talent_profile RENAME COLUMN latitude TO primary_latitude;
ALTER TABLE talent_profile RENAME COLUMN longitude TO primary_longitude;
ALTER TABLE talent_profile RENAME COLUMN city TO primary_city;
ALTER TABLE talent_profile RENAME COLUMN country TO primary_country;

-- Step 2: Drop old index and create new one for primary location
DROP INDEX IF EXISTS idx_talent_profile_location;
CREATE INDEX idx_talent_profile_primary_location ON talent_profile(primary_latitude, primary_longitude)
    WHERE primary_latitude IS NOT NULL AND primary_longitude IS NOT NULL;

-- Step 3: Create talent_geo_area table for multiple service locations
CREATE TABLE talent_geo_area (
    id BIGSERIAL PRIMARY KEY,
    talent_profile_id UUID NOT NULL REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_talent_city_country UNIQUE (talent_profile_id, city, country)
);

-- Step 4: Create indexes for efficient querying
CREATE INDEX idx_talent_geo_area_profile ON talent_geo_area(talent_profile_id);
CREATE INDEX idx_talent_geo_area_location ON talent_geo_area(city, country);
CREATE INDEX idx_talent_geo_area_primary ON talent_geo_area(talent_profile_id, is_primary) WHERE is_primary = TRUE;

-- Step 5: Migrate existing location data to geo_area table
-- For talents with city and country, create a geo area entry marked as primary
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary)
SELECT talent_profile_id, primary_city, primary_country, TRUE
FROM talent_profile
WHERE primary_city IS NOT NULL AND primary_country IS NOT NULL;

-- Add comment for documentation
COMMENT ON TABLE talent_geo_area IS 'Stores multiple geographical areas where a talent provides services. Talents can specify primary location and additional coverage areas.';
COMMENT ON COLUMN talent_geo_area.is_primary IS 'Indicates if this is the primary geographical area where the talent is based.';
