-- V18__add_sample_introduction_videos.sql
-- Add sample introduction videos to existing test talent profiles

-- Update John Actor with introduction video
UPDATE talent_profile
SET introduction_video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    introduction_video_duration = 15
WHERE talent_profile_id = '10000001-0000-0000-0000-000000000001';

-- Update Sarah Voice with introduction video
UPDATE talent_profile
SET introduction_video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    introduction_video_duration = 15
WHERE talent_profile_id = '20000002-0000-0000-0000-000000000002';

-- Update Emma Singer with introduction video
UPDATE talent_profile
SET introduction_video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4',
    introduction_video_duration = 15
WHERE talent_profile_id = '40000004-0000-0000-0000-000000000004';
