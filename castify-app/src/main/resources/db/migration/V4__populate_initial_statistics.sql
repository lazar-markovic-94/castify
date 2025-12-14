-- =====================================================
-- Migration V4: Populate Initial Statistics
-- =====================================================
-- This migration creates initial statistics entries for
-- all existing talent profiles that don't have them yet.
-- =====================================================

-- Insert statistics for all existing talents without statistics
INSERT INTO talent_statistics (
    talent_profile_id,
    average_rating,
    total_ratings,
    average_professionalism_rating,
    average_quality_rating,
    average_communication_rating,
    average_timeliness_rating,
    total_collaborations,
    completed_collaborations,
    canceled_collaborations,
    success_rate,
    total_earnings,
    average_response_time_hours,
    profile_completeness,
    would_work_again_percentage,
    tier,
    last_updated
)
SELECT
    tp.talent_profile_id,
    0.0 as average_rating,
    0 as total_ratings,
    0.0 as average_professionalism_rating,
    0.0 as average_quality_rating,
    0.0 as average_communication_rating,
    0.0 as average_timeliness_rating,
    0 as total_collaborations,
    0 as completed_collaborations,
    0 as canceled_collaborations,
    0.0 as success_rate,
    0.0 as total_earnings,
    NULL as average_response_time_hours,
    -- Calculate initial profile completeness
    (
        CASE WHEN tp.bio IS NOT NULL AND tp.bio != '' THEN 10 ELSE 0 END +
        10 + -- Location is always present
        CASE WHEN tp.profile_image_url IS NOT NULL AND tp.profile_image_url != '' THEN 20 ELSE 0 END +
        CASE WHEN (SELECT COUNT(*) FROM talent_language WHERE talent_profile_id = tp.talent_profile_id) > 0 THEN 10 ELSE 0 END +
        CASE WHEN (SELECT COUNT(*) FROM talent_tag WHERE talent_profile_id = tp.talent_profile_id) >= 3 THEN 10 ELSE 0 END +
        CASE WHEN (
            tp.can_do_voiceover OR
            tp.can_do_acting OR
            tp.can_do_modeling OR
            tp.can_do_singing OR
            tp.can_do_dancing OR
            tp.can_do_hosting
        ) THEN 10 ELSE 0 END +
        CASE
            WHEN (SELECT COUNT(*) FROM talent_social_link WHERE talent_profile_id = tp.talent_profile_id) >= 3 THEN 30
            WHEN (SELECT COUNT(*) FROM talent_social_link WHERE talent_profile_id = tp.talent_profile_id) >= 2 THEN 20
            WHEN (SELECT COUNT(*) FROM talent_social_link WHERE talent_profile_id = tp.talent_profile_id) >= 1 THEN 10
            ELSE 0
        END
    ) as profile_completeness,
    0.0 as would_work_again_percentage,
    'BRONZE' as tier,
    CURRENT_TIMESTAMP as last_updated
FROM talent_profile tp
WHERE tp.talent_profile_id NOT IN (
    SELECT talent_profile_id FROM talent_statistics
);

-- Log how many records were created
DO $$
DECLARE
    record_count INTEGER;
BEGIN
    GET DIAGNOSTICS record_count = ROW_COUNT;
    RAISE NOTICE 'Created initial statistics for % talent profiles', record_count;
END $$;
