-- =====================================================
-- Migration V10: Performance Indexes and Database Triggers
-- =====================================================
-- This migration addresses:
-- 1. Add missing performance indexes (MEDIUM - Issue #9)
-- 2. Create database triggers for automatic updates (MEDIUM - Issue #19)
-- 3. Create statistics update trigger (HIGH - Issue #18)
-- 4. Add composite indexes for common query patterns
-- =====================================================

-- =====================================================
-- PART 1: Performance Indexes
-- =====================================================

-- Index for unread messages query (MEDIUM - Issue #9)
CREATE INDEX idx_message_unread_by_conversation
    ON message(conversation_id, is_read, sent_at DESC)
    WHERE is_read = FALSE AND deleted_at IS NULL;

COMMENT ON INDEX idx_message_unread_by_conversation IS
    'Optimizes queries for unread messages. Partial index excludes read messages.';

-- Composite index for collaboration queries by client and status
CREATE INDEX idx_collaboration_client_status_created
    ON collaboration(client_user_id, status, created_at DESC)
    WHERE deleted_at IS NULL;

-- Composite index for talent profile searches with verification
CREATE INDEX idx_talent_profile_verification_created
    ON talent_profile(verification_status, created_at DESC)
    WHERE deleted_at IS NULL;

-- Index for finding ratings by talent and date
CREATE INDEX idx_rating_talent_created
    ON talent_rating(talent_profile_id, created_at DESC)
    WHERE is_verified = TRUE;

-- Index for active users
CREATE INDEX idx_user_active
    ON app_user(is_active, role)
    WHERE deleted_at IS NULL;

-- Index for portfolio items ordered by display order
CREATE INDEX idx_portfolio_talent_order
    ON talent_portfolio_item(talent_profile_id, display_order ASC);

-- =====================================================
-- PART 2: Database Triggers for Automatic Updates
-- =====================================================

-- =====================================================
-- Trigger 1: Auto-update conversation.last_message_at
-- =====================================================
-- Eliminates race conditions when inserting messages

CREATE OR REPLACE FUNCTION update_conversation_last_message()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversation
    SET last_message_at = NEW.sent_at,
        updated_at = CURRENT_TIMESTAMP
    WHERE conversation_id = NEW.conversation_id
      AND last_message_at < NEW.sent_at;  -- Only update if newer
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_conversation_timestamp
    AFTER INSERT ON message
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_last_message();

COMMENT ON FUNCTION update_conversation_last_message() IS
    'Automatically updates conversation.last_message_at when new message inserted';

-- =====================================================
-- Trigger 2: Auto-update talent_statistics on rating insert/update
-- =====================================================
-- Ensures statistics stay in sync with ratings (HIGH - Issue #18)

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
    SELECT
        COUNT(*),
        AVG(overall_rating),
        AVG(professionalism_rating),
        AVG(quality_rating),
        AVG(communication_rating),
        AVG(timeliness_rating),
        (COUNT(*) FILTER (WHERE would_work_again = TRUE)::DOUBLE PRECISION / COUNT(*)) * 100
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

CREATE TRIGGER trigger_update_stats_on_rating_insert
    AFTER INSERT ON talent_rating
    FOR EACH ROW
    EXECUTE FUNCTION update_talent_statistics_on_rating();

CREATE TRIGGER trigger_update_stats_on_rating_update
    AFTER UPDATE ON talent_rating
    FOR EACH ROW
    WHEN (OLD.is_verified IS DISTINCT FROM NEW.is_verified OR
          OLD.overall_rating IS DISTINCT FROM NEW.overall_rating)
    EXECUTE FUNCTION update_talent_statistics_on_rating();

CREATE TRIGGER trigger_update_stats_on_rating_delete
    AFTER DELETE ON talent_rating
    FOR EACH ROW
    EXECUTE FUNCTION update_talent_statistics_on_rating();

COMMENT ON FUNCTION update_talent_statistics_on_rating() IS
    'Automatically recalculates and updates talent_statistics when ratings change. Ensures consistency.';

-- =====================================================
-- Trigger 3: Auto-update collaboration statistics
-- =====================================================

CREATE OR REPLACE FUNCTION update_talent_stats_on_collaboration()
RETURNS TRIGGER AS $$
DECLARE
    v_total_collab INTEGER;
    v_completed_collab INTEGER;
    v_canceled_collab INTEGER;
    v_success_rate DOUBLE PRECISION;
    v_total_earnings NUMERIC(12, 2);
BEGIN
    -- Calculate collaboration metrics
    SELECT
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'COMPLETED'),
        COUNT(*) FILTER (WHERE status = 'CANCELED'),
        CASE
            WHEN COUNT(*) > 0 THEN
                (COUNT(*) FILTER (WHERE status = 'COMPLETED')::DOUBLE PRECISION / COUNT(*)) * 100
            ELSE 0.0
        END,
        COALESCE(SUM(budget) FILTER (WHERE status = 'COMPLETED'), 0)
    INTO
        v_total_collab,
        v_completed_collab,
        v_canceled_collab,
        v_success_rate,
        v_total_earnings
    FROM collaboration
    WHERE talent_profile_id = COALESCE(NEW.talent_profile_id, OLD.talent_profile_id);

    -- Update statistics
    UPDATE talent_statistics
    SET
        total_collaborations = v_total_collab,
        completed_collaborations = v_completed_collab,
        canceled_collaborations = v_canceled_collab,
        success_rate = v_success_rate,
        total_earnings = v_total_earnings,
        last_updated = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE talent_profile_id = COALESCE(NEW.talent_profile_id, OLD.talent_profile_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_stats_on_collaboration
    AFTER INSERT OR UPDATE OR DELETE ON collaboration
    FOR EACH ROW
    EXECUTE FUNCTION update_talent_stats_on_collaboration();

COMMENT ON FUNCTION update_talent_stats_on_collaboration() IS
    'Updates talent statistics when collaborations are created, completed, or canceled';

-- =====================================================
-- Trigger 4: Auto-update updated_at timestamps
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables with updated_at column
CREATE TRIGGER trigger_app_user_updated_at
    BEFORE UPDATE ON app_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_talent_profile_updated_at
    BEFORE UPDATE ON talent_profile
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_client_org_updated_at
    BEFORE UPDATE ON client_org
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Note: collaboration already has this trigger from V6

COMMENT ON FUNCTION update_updated_at_column() IS
    'Automatically updates updated_at timestamp on record modification';

-- =====================================================
-- Trigger 5: Calculate performance tier automatically
-- =====================================================

CREATE OR REPLACE FUNCTION calculate_performance_tier()
RETURNS TRIGGER AS $$
DECLARE
    v_percentile DOUBLE PRECISION;
BEGIN
    -- Calculate percentile rank based on average_rating
    SELECT PERCENT_RANK() OVER (ORDER BY average_rating DESC)
    INTO v_percentile
    FROM talent_statistics
    WHERE talent_profile_id = NEW.talent_profile_id;

    -- Assign tier based on percentile
    NEW.performance_tier := CASE
        WHEN v_percentile <= 0.05 THEN 'PREMIUM'::VARCHAR(20)  -- Top 5%
        WHEN v_percentile <= 0.20 THEN 'GOLD'::VARCHAR(20)     -- Top 20%
        WHEN v_percentile <= 0.50 THEN 'SILVER'::VARCHAR(20)   -- Top 50%
        ELSE 'BRONZE'::VARCHAR(20)
    END;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_tier
    BEFORE UPDATE ON talent_statistics
    FOR EACH ROW
    WHEN (OLD.average_rating IS DISTINCT FROM NEW.average_rating OR
          OLD.total_ratings IS DISTINCT FROM NEW.total_ratings)
    EXECUTE FUNCTION calculate_performance_tier();

COMMENT ON FUNCTION calculate_performance_tier() IS
    'Automatically calculates and assigns performance tier based on average rating percentile';

-- =====================================================
-- PART 3: Analyze tables for query planner
-- =====================================================

ANALYZE app_user;
ANALYZE talent_profile;
ANALYZE talent_rating;
ANALYZE talent_statistics;
ANALYZE collaboration;
ANALYZE message;
ANALYZE conversation;
