-- =====================================================
-- Migration V5: Update Sample Statistics with Varied Tiers and Ratings
-- =====================================================
-- This migration updates existing talent statistics with
-- sample ratings and different tiers for testing
-- =====================================================

-- Update statistics for existing talents with varied data
-- We'll assign different tiers and ratings to make the filters testable

-- Get the first talent and make them PREMIUM
UPDATE talent_statistics
SET
    average_rating = 4.8,
    total_ratings = 42,
    average_professionalism_rating = 4.9,
    average_quality_rating = 4.7,
    average_communication_rating = 4.8,
    average_timeliness_rating = 4.8,
    total_collaborations = 50,
    completed_collaborations = 48,
    canceled_collaborations = 2,
    success_rate = 96.0,
    total_earnings = 25000.00,
    would_work_again_percentage = 95.0,
    tier = 'PREMIUM',
    last_updated = CURRENT_TIMESTAMP
WHERE talent_profile_id = (
    SELECT talent_profile_id FROM talent_statistics ORDER BY talent_profile_id LIMIT 1 OFFSET 0
);

-- Second talent: GOLD tier
UPDATE talent_statistics
SET
    average_rating = 4.5,
    total_ratings = 28,
    average_professionalism_rating = 4.6,
    average_quality_rating = 4.4,
    average_communication_rating = 4.5,
    average_timeliness_rating = 4.5,
    total_collaborations = 35,
    completed_collaborations = 32,
    canceled_collaborations = 3,
    success_rate = 91.0,
    total_earnings = 15000.00,
    would_work_again_percentage = 87.0,
    tier = 'GOLD',
    last_updated = CURRENT_TIMESTAMP
WHERE talent_profile_id = (
    SELECT talent_profile_id FROM talent_statistics ORDER BY talent_profile_id LIMIT 1 OFFSET 1
);

-- Third talent: SILVER tier
UPDATE talent_statistics
SET
    average_rating = 4.0,
    total_ratings = 15,
    average_professionalism_rating = 4.1,
    average_quality_rating = 3.9,
    average_communication_rating = 4.0,
    average_timeliness_rating = 4.0,
    total_collaborations = 20,
    completed_collaborations = 17,
    canceled_collaborations = 3,
    success_rate = 85.0,
    total_earnings = 8000.00,
    would_work_again_percentage = 80.0,
    tier = 'SILVER',
    last_updated = CURRENT_TIMESTAMP
WHERE talent_profile_id = (
    SELECT talent_profile_id FROM talent_statistics ORDER BY talent_profile_id LIMIT 1 OFFSET 2
);

-- Fourth talent: BRONZE tier (with some activity)
UPDATE talent_statistics
SET
    average_rating = 3.5,
    total_ratings = 6,
    average_professionalism_rating = 3.7,
    average_quality_rating = 3.4,
    average_communication_rating = 3.5,
    average_timeliness_rating = 3.4,
    total_collaborations = 8,
    completed_collaborations = 6,
    canceled_collaborations = 2,
    success_rate = 75.0,
    total_earnings = 2500.00,
    would_work_again_percentage = 70.0,
    tier = 'BRONZE',
    last_updated = CURRENT_TIMESTAMP
WHERE talent_profile_id = (
    SELECT talent_profile_id FROM talent_statistics ORDER BY talent_profile_id LIMIT 1 OFFSET 3
);

-- Log the update
DO $$
BEGIN
    RAISE NOTICE 'Updated sample statistics with varied tiers and ratings for testing';
END $$;
