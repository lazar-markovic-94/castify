-- Add category column to talent_tag table
ALTER TABLE talent_tag ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'PRIMARY';

-- Create index for category queries
CREATE INDEX idx_talent_tag_category ON talent_tag(category);

-- Clear all existing tags (as per requirement)
DELETE FROM talent_tag;

-- Add check constraint to ensure only valid categories
ALTER TABLE talent_tag ADD CONSTRAINT check_tag_category
    CHECK (category IN ('PRIMARY', 'SECONDARY'));

-- Re-insert tags using new predefined tag names with categories
-- John Actor
INSERT INTO talent_tag (talent_profile_id, tag_name, category) VALUES
('10000001-0000-0000-0000-000000000001', 'acting', 'PRIMARY'),
('10000001-0000-0000-0000-000000000001', 'voiceover', 'PRIMARY'),
('10000001-0000-0000-0000-000000000001', 'videoshooting', 'SECONDARY');

-- Sarah Voice
INSERT INTO talent_tag (talent_profile_id, tag_name, category) VALUES
('20000002-0000-0000-0000-000000000002', 'voiceover', 'PRIMARY'),
('20000002-0000-0000-0000-000000000002', 'fullvideoproduction', 'SECONDARY');

-- Mike Model
INSERT INTO talent_tag (talent_profile_id, tag_name, category) VALUES
('30000003-0000-0000-0000-000000000003', 'photoshooting', 'SECONDARY'),
('30000003-0000-0000-0000-000000000003', 'videoshooting', 'SECONDARY');

-- Emma Singer
INSERT INTO talent_tag (talent_profile_id, tag_name, category) VALUES
('40000004-0000-0000-0000-000000000004', 'acting', 'PRIMARY'),
('40000004-0000-0000-0000-000000000004', 'fullvideoproduction', 'SECONDARY');

-- David Host
INSERT INTO talent_tag (talent_profile_id, tag_name, category) VALUES
('50000005-0000-0000-0000-000000000005', 'acting', 'PRIMARY'),
('50000005-0000-0000-0000-000000000005', 'voiceover', 'PRIMARY'),
('50000005-0000-0000-0000-000000000005', 'photoshooting', 'SECONDARY');
