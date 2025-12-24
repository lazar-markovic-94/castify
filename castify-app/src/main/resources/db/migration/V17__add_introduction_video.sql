-- Migration to add introduction video fields to talent_profile table
-- This allows talents to upload a 30-second introduction video

ALTER TABLE talent_profile
ADD COLUMN introduction_video_url TEXT,
ADD COLUMN introduction_video_duration INTEGER;

-- Add comment for documentation
COMMENT ON COLUMN talent_profile.introduction_video_url IS 'URL of the talent introduction video (max 30 seconds)';
COMMENT ON COLUMN talent_profile.introduction_video_duration IS 'Duration of the introduction video in seconds (max 30)';

-- Add check constraint to ensure duration is between 1 and 30 seconds if provided
ALTER TABLE talent_profile
ADD CONSTRAINT chk_introduction_video_duration
CHECK (introduction_video_duration IS NULL OR (introduction_video_duration > 0 AND introduction_video_duration <= 30));
