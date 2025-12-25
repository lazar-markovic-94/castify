-- =====================================================
-- Migration V20: Remove Statistics Update Triggers
-- =====================================================
-- This migration removes database triggers that update talent_statistics
-- to prevent optimistic locking conflicts with application-level updates.
--
-- Background:
-- - Database triggers were updating talent_statistics.version field
-- - Application code also updates statistics, causing version conflicts
-- - Solution: Move all statistics logic to application layer (TalentStatisticsService)
--
-- Triggers being removed:
-- 1. update_talent_statistics_on_rating() - Updates stats when ratings change
-- 2. update_talent_stats_on_collaboration() - Updates stats when collaborations change
--
-- Triggers being KEPT (no optimistic locking issues):
-- - create_initial_talent_statistics() - Creates initial stats for new profiles
-- - update_conversation_last_message() - Updates conversation timestamps
-- - update_updated_at_column() - BEFORE trigger for timestamps
-- - calculate_performance_tier() - BEFORE trigger for tier calculation
-- =====================================================

-- =====================================================
-- Drop Rating Statistics Triggers
-- =====================================================

-- Drop triggers that fire on rating changes
DROP TRIGGER IF EXISTS trigger_update_stats_on_rating_insert ON talent_rating;
DROP TRIGGER IF EXISTS trigger_update_stats_on_rating_update ON talent_rating;
DROP TRIGGER IF EXISTS trigger_update_stats_on_rating_delete ON talent_rating;

-- Drop the trigger function
DROP FUNCTION IF EXISTS update_talent_statistics_on_rating();

-- =====================================================
-- Drop Collaboration Statistics Trigger
-- =====================================================

-- Drop trigger that fires on collaboration changes
DROP TRIGGER IF EXISTS trigger_update_stats_on_collaboration ON collaboration;

-- Drop the trigger function
DROP FUNCTION IF EXISTS update_talent_stats_on_collaboration();

-- =====================================================
-- Keep Performance Tier Calculation Trigger
-- =====================================================
-- NOTE: The calculate_performance_tier() trigger is a BEFORE trigger
-- that runs on talent_statistics updates. This doesn't cause conflicts
-- because it modifies the NEW row before it's saved, not after.
-- It will continue to work with application-layer statistics updates.

-- =====================================================
-- Verify remaining triggers
-- =====================================================

-- The following triggers should still exist after this migration:
-- 1. trigger_create_talent_statistics (AFTER INSERT on talent_profile)
-- 2. trigger_update_conversation_timestamp (AFTER INSERT on message)
-- 3. trigger_app_user_updated_at (BEFORE UPDATE on app_user)
-- 4. trigger_talent_profile_updated_at (BEFORE UPDATE on talent_profile)
-- 5. trigger_client_org_updated_at (BEFORE UPDATE on client_org)
-- 6. trigger_calculate_tier (BEFORE UPDATE on talent_statistics)

-- To verify triggers are correctly set up, run:
-- SELECT trigger_name, event_object_table, action_timing, event_manipulation
-- FROM information_schema.triggers
-- WHERE trigger_schema = 'public'
-- ORDER BY event_object_table, trigger_name;
