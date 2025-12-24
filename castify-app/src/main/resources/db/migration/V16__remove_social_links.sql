-- Migration to remove social links feature
-- This is a business decision to protect the platform's business model

-- Drop the index first
DROP INDEX IF EXISTS idx_talent_social_link_profile;

-- Drop the table
DROP TABLE IF EXISTS talent_social_link;
