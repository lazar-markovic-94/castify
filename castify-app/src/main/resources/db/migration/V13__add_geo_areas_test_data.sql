-- V13__add_geo_areas_test_data.sql
-- Add geographical service areas for all test talents

-- =============================================================================
-- GEO AREAS FOR TALENTS
-- =============================================================================
-- Note: Primary locations were already migrated by V12__add_multi_location_support.sql
-- This script only adds ADDITIONAL service areas (non-primary)

-- John Actor (Los Angeles based) - Covers Southern California
-- Additional areas (primary Los Angeles already exists from V12)
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary) VALUES
('10000001-0000-0000-0000-000000000001', 'San Diego', 'USA', false),
('10000001-0000-0000-0000-000000000001', 'San Francisco', 'USA', false)
ON CONFLICT (talent_profile_id, city, country) DO NOTHING;

-- Sarah Voice (New York based) - Covers Northeast corridor
-- Additional areas (primary New York already exists from V12)
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary) VALUES
('20000002-0000-0000-0000-000000000002', 'Boston', 'USA', false),
('20000002-0000-0000-0000-000000000002', 'Philadelphia', 'USA', false)
ON CONFLICT (talent_profile_id, city, country) DO NOTHING;

-- Mike Model (Miami based) - Covers Florida
-- Additional areas (primary Miami already exists from V12)
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary) VALUES
('30000003-0000-0000-0000-000000000003', 'Orlando', 'USA', false),
('30000003-0000-0000-0000-000000000003', 'Fort Lauderdale', 'USA', false),
('30000003-0000-0000-0000-000000000003', 'Tampa', 'USA', false)
ON CONFLICT (talent_profile_id, city, country) DO NOTHING;

-- Emma Singer (Nashville based) - Covers Tennessee and neighboring states
-- Additional areas (primary Nashville already exists from V12)
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary) VALUES
('40000004-0000-0000-0000-000000000004', 'Memphis', 'USA', false),
('40000004-0000-0000-0000-000000000004', 'Atlanta', 'USA', false)
ON CONFLICT (talent_profile_id, city, country) DO NOTHING;

-- David Host (Remote) - Works in multiple major cities
-- Since he's REMOTE (no primary city/country), we'll add Chicago as primary + other major cities
INSERT INTO talent_geo_area (talent_profile_id, city, country, is_primary) VALUES
('50000005-0000-0000-0000-000000000005', 'Chicago', 'USA', true),
('50000005-0000-0000-0000-000000000005', 'New York', 'USA', false),
('50000005-0000-0000-0000-000000000005', 'Los Angeles', 'USA', false),
('50000005-0000-0000-0000-000000000005', 'Las Vegas', 'USA', false),
('50000005-0000-0000-0000-000000000005', 'Miami', 'USA', false)
ON CONFLICT (talent_profile_id, city, country) DO NOTHING;

-- =============================================================================
-- NOTES
-- =============================================================================
-- This script adds realistic geographical service areas for each talent:
--
-- John Actor: Southern California market (LA primary + San Diego + San Francisco)
-- Sarah Voice: Northeast corridor (NY primary + Boston + Philadelphia)
-- Mike Model: Florida market (Miami primary + Orlando + Fort Lauderdale + Tampa)
-- Emma Singer: Music corridor (Nashville primary + Memphis + Atlanta)
-- David Host: National coverage (Chicago primary + major cities nationwide)
--
-- Each talent has:
-- - One PRIMARY location (where they're based) - migrated from V12 or added here
-- - Multiple SECONDARY locations (where they're willing to travel for work)
