-- =====================================================
-- Migration V19: Fix Division by Zero in Rating Trigger
-- =====================================================
-- This migration fixes a critical bug in the update_talent_statistics_on_rating() trigger
-- that causes division by zero when calculating would_work_again_percentage
-- for talents with zero verified ratings.
--
-- Issue: When inserting the first unverified rating, the trigger queries for
-- verified ratings (is_verified = TRUE), gets COUNT(*) = 0, and attempts
-- division by zero when calculating the percentage.
--
-- Fix: Add NULLIF(COUNT(*), 0) to prevent division by zero,
-- and use COALESCE to default to 0.0 when there are no verified ratings.
-- =====================================================

-- Drop and recreate the function with the fix
CREATE OR REPLACE FUNCTION update_talent_statistics_on_rating()
RETURNS TRIGGER AS $$
DECLARE
    v_total_ratings INTEGER;
    v_avg_overall DOUBLE PRECISION;
    v_avg_prof DOUBLE PRECISION;
    v_avg_quality DOUBLE PRECISION;
    v_avg_comm DOUBLE PRECISION;
    v_avg_time DOUBLE PRECISION;
    v_would_work_again_pct DOUBLE PRECISION;
BEGIN
    -- Calculate aggregated statistics
    -- FIX: Use NULLIF to prevent division by zero
    SELECT
        COUNT(*),
        AVG(overall_rating),
        AVG(professionalism_rating),
        AVG(quality_rating),
        AVG(communication_rating),
        AVG(timeliness_rating),
        (COUNT(*) FILTER (WHERE would_work_again = TRUE)::DOUBLE PRECISION / NULLIF(COUNT(*), 0)) * 100
    INTO
        v_total_ratings,
        v_avg_overall,
        v_avg_prof,
        v_avg_quality,
        v_avg_comm,
        v_avg_time,
        v_would_work_again_pct
    FROM talent_rating
    WHERE talent_profile_id = COALESCE(NEW.talent_profile_id, OLD.talent_profile_id)
      AND is_verified = TRUE;

    -- Update statistics table
    UPDATE talent_statistics
    SET
        total_ratings = v_total_ratings,
        average_rating = COALESCE(v_avg_overall, 0.0),
        average_professionalism_rating = COALESCE(v_avg_prof, 0.0),
        average_quality_rating = COALESCE(v_avg_quality, 0.0),
        average_communication_rating = COALESCE(v_avg_comm, 0.0),
        average_timeliness_rating = COALESCE(v_avg_time, 0.0),
        would_work_again_percentage = COALESCE(v_would_work_again_pct, 0.0),
        last_updated = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE talent_profile_id = COALESCE(NEW.talent_profile_id, OLD.talent_profile_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_talent_statistics_on_rating() IS
    'Automatically recalculates and updates talent_statistics when ratings change. Fixed division by zero bug.';
