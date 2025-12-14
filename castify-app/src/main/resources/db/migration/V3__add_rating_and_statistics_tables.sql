-- =====================================================
-- Migration V3: Add Rating and Statistics Tables
-- =====================================================
-- This migration adds support for:
-- 1. Talent ratings from clients
-- 2. Talent statistics and tier system
-- 3. Indexes for performance optimization
-- =====================================================

-- =====================================================
-- Table: talent_rating
-- Stores individual ratings given by clients to talents
-- =====================================================
CREATE TABLE talent_rating (
    rating_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    talent_profile_id UUID NOT NULL,
    client_user_id UUID NOT NULL,
    collaboration_id UUID,  -- Optional reference to specific project/collaboration

    -- Rating dimensions (1-5 scale)
    overall_rating INTEGER NOT NULL CHECK (overall_rating >= 1 AND overall_rating <= 5),
    professionalism_rating INTEGER NOT NULL CHECK (professionalism_rating >= 1 AND professionalism_rating <= 5),
    quality_rating INTEGER NOT NULL CHECK (quality_rating >= 1 AND quality_rating <= 5),
    communication_rating INTEGER NOT NULL CHECK (communication_rating >= 1 AND communication_rating <= 5),
    timeliness_rating INTEGER NOT NULL CHECK (timeliness_rating >= 1 AND timeliness_rating <= 5),

    -- Written review
    review_text TEXT,

    -- Would the client work with this talent again?
    would_work_again BOOLEAN NOT NULL DEFAULT false,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN NOT NULL DEFAULT false,

    -- Foreign keys
    CONSTRAINT fk_rating_talent_profile FOREIGN KEY (talent_profile_id)
        REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_client_user FOREIGN KEY (client_user_id)
        REFERENCES app_user(user_id) ON DELETE CASCADE,

    -- Unique constraint: one rating per client per talent per collaboration
    CONSTRAINT uk_rating_talent_client_collaboration
        UNIQUE (talent_profile_id, client_user_id, collaboration_id)
);

-- Indexes for talent_rating
CREATE INDEX idx_rating_talent_profile ON talent_rating(talent_profile_id);
CREATE INDEX idx_rating_client_user ON talent_rating(client_user_id);
CREATE INDEX idx_rating_collaboration ON talent_rating(collaboration_id);
CREATE INDEX idx_rating_created_at ON talent_rating(created_at DESC);
CREATE INDEX idx_rating_overall_rating ON talent_rating(overall_rating DESC);

-- Index for finding ratings by talent and client
CREATE INDEX idx_rating_talent_client ON talent_rating(talent_profile_id, client_user_id);

-- =====================================================
-- Table: talent_statistics
-- Stores aggregated statistics for each talent profile
-- =====================================================
CREATE TABLE talent_statistics (
    talent_profile_id UUID PRIMARY KEY,

    -- Rating metrics
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (average_rating >= 0.0 AND average_rating <= 5.0),
    total_ratings INTEGER NOT NULL DEFAULT 0 CHECK (total_ratings >= 0),
    average_professionalism_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (average_professionalism_rating >= 0.0 AND average_professionalism_rating <= 5.0),
    average_quality_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (average_quality_rating >= 0.0 AND average_quality_rating <= 5.0),
    average_communication_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (average_communication_rating >= 0.0 AND average_communication_rating <= 5.0),
    average_timeliness_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (average_timeliness_rating >= 0.0 AND average_timeliness_rating <= 5.0),

    -- Collaboration metrics
    total_collaborations INTEGER NOT NULL DEFAULT 0 CHECK (total_collaborations >= 0),
    completed_collaborations INTEGER NOT NULL DEFAULT 0 CHECK (completed_collaborations >= 0),
    canceled_collaborations INTEGER NOT NULL DEFAULT 0 CHECK (canceled_collaborations >= 0),
    success_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (success_rate >= 0.0 AND success_rate <= 100.0),

    -- Financial metrics
    total_earnings DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (total_earnings >= 0.0),

    -- Engagement metrics
    average_response_time_hours DOUBLE PRECISION,
    profile_completeness INTEGER NOT NULL DEFAULT 0 CHECK (profile_completeness >= 0 AND profile_completeness <= 100),

    -- Recommendation
    would_work_again_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (would_work_again_percentage >= 0.0 AND would_work_again_percentage <= 100.0),

    -- Tier (PREMIUM, GOLD, SILVER, BRONZE)
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE' CHECK (tier IN ('PREMIUM', 'GOLD', 'SILVER', 'BRONZE')),

    -- Metadata
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key
    CONSTRAINT fk_statistics_talent_profile FOREIGN KEY (talent_profile_id)
        REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE
);

-- Indexes for talent_statistics
CREATE INDEX idx_statistics_average_rating ON talent_statistics(average_rating DESC);
CREATE INDEX idx_statistics_tier ON talent_statistics(tier);
CREATE INDEX idx_statistics_completed_collaborations ON talent_statistics(completed_collaborations DESC);
CREATE INDEX idx_statistics_success_rate ON talent_statistics(success_rate DESC);
CREATE INDEX idx_statistics_total_earnings ON talent_statistics(total_earnings DESC);

-- Composite index for tier-based searches
CREATE INDEX idx_statistics_tier_rating ON talent_statistics(tier, average_rating DESC);

-- Index for finding talents with minimum rating
CREATE INDEX idx_statistics_rating_tier ON talent_statistics(average_rating DESC, tier);

-- =====================================================
-- Trigger: Auto-create statistics for new talent profiles
-- =====================================================
CREATE OR REPLACE FUNCTION create_initial_talent_statistics()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO talent_statistics (talent_profile_id, profile_completeness)
    VALUES (NEW.talent_profile_id, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_talent_statistics
    AFTER INSERT ON talent_profile
    FOR EACH ROW
    EXECUTE FUNCTION create_initial_talent_statistics();

-- =====================================================
-- Comments for documentation
-- =====================================================
COMMENT ON TABLE talent_rating IS 'Stores individual ratings given by clients to talents after collaborations';
COMMENT ON TABLE talent_statistics IS 'Aggregated statistics for talent profiles, used for tier calculation and search ranking';
COMMENT ON COLUMN talent_statistics.tier IS 'Talent tier: PREMIUM (top 5%), GOLD (top 20%), SILVER (top 50%), BRONZE (rest)';
COMMENT ON COLUMN talent_statistics.success_rate IS 'Percentage of completed collaborations out of total collaborations';
COMMENT ON COLUMN talent_statistics.profile_completeness IS 'Calculated score (0-100) based on profile information completeness';
