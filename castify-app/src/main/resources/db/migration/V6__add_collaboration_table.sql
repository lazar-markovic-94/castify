-- =====================================================
-- Migration V6: Add Collaboration Table
-- =====================================================
-- This migration adds support for:
-- 1. Collaboration/project proposals between clients and talents
-- 2. Collaboration status workflow (proposed → accepted → in progress → completed)
-- 3. Indexes for performance optimization
-- =====================================================

-- =====================================================
-- Table: collaboration
-- Stores collaborations/projects between clients and talents
-- =====================================================
CREATE TABLE collaboration (
    collaboration_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Participants
    client_user_id UUID NOT NULL,
    talent_profile_id UUID NOT NULL,

    -- Project details
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    budget DECIMAL(10, 2),  -- Optional budget amount
    start_date TIMESTAMP,    -- Optional project start date
    end_date TIMESTAMP,      -- Optional project end date
    notes TEXT,              -- Additional notes/requirements

    -- Status tracking
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROPOSED', 'ACCEPTED', 'DECLINED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED')),
    proposed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,  -- When talent accepted/declined
    completed_at TIMESTAMP,  -- When collaboration was completed

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_collaboration_client_user FOREIGN KEY (client_user_id)
        REFERENCES app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_talent_profile FOREIGN KEY (talent_profile_id)
        REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE
);

-- =====================================================
-- Indexes for collaboration
-- =====================================================

-- Find collaborations by client
CREATE INDEX idx_collaboration_client_user ON collaboration(client_user_id);

-- Find collaborations by talent
CREATE INDEX idx_collaboration_talent_profile ON collaboration(talent_profile_id);

-- Find collaborations by status
CREATE INDEX idx_collaboration_status ON collaboration(status);

-- Find collaborations by client and status
CREATE INDEX idx_collaboration_client_status ON collaboration(client_user_id, status);

-- Find collaborations by talent and status
CREATE INDEX idx_collaboration_talent_status ON collaboration(talent_profile_id, status);

-- Sort collaborations by date
CREATE INDEX idx_collaboration_created_at ON collaboration(created_at DESC);
CREATE INDEX idx_collaboration_proposed_at ON collaboration(proposed_at DESC);
CREATE INDEX idx_collaboration_completed_at ON collaboration(completed_at DESC);

-- Check if collaboration exists between client and talent
CREATE INDEX idx_collaboration_client_talent ON collaboration(client_user_id, talent_profile_id);

-- =====================================================
-- Trigger: Update updated_at timestamp
-- =====================================================
CREATE OR REPLACE FUNCTION update_collaboration_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_collaboration_timestamp
    BEFORE UPDATE ON collaboration
    FOR EACH ROW
    EXECUTE FUNCTION update_collaboration_updated_at();

-- =====================================================
-- Comments for documentation
-- =====================================================
COMMENT ON TABLE collaboration IS 'Stores collaboration/project proposals and their lifecycle between clients and talents';
COMMENT ON COLUMN collaboration.status IS 'Collaboration status: PROPOSED (new proposal), ACCEPTED (talent accepted), DECLINED (talent declined), IN_PROGRESS (work started), COMPLETED (successfully finished), CANCELED (canceled by either party)';
COMMENT ON COLUMN collaboration.proposed_at IS 'When the client proposed the collaboration';
COMMENT ON COLUMN collaboration.responded_at IS 'When the talent accepted or declined the proposal';
COMMENT ON COLUMN collaboration.completed_at IS 'When the collaboration was marked as completed';

-- =====================================================
-- Add Foreign Key Constraint to Existing Tables
-- =====================================================
-- Now that the collaboration table exists, we can add a foreign key constraint
-- from talent_rating.collaboration_id to collaboration.collaboration_id
ALTER TABLE talent_rating
    ADD CONSTRAINT fk_rating_collaboration FOREIGN KEY (collaboration_id)
        REFERENCES collaboration(collaboration_id) ON DELETE SET NULL;
