-- ============================================================================
-- Migration V14: Fix conversation last_message_at trigger
-- Date: 2025-12-23
-- Description: Fixes the update_conversation_last_message() trigger function
--              to remove reference to non-existent updated_at column.
--              The conversation table only has: conversation_id, talent_user_id,
--              client_user_id, created_at, and last_message_at.
-- ============================================================================
-- Issue: V10 created a trigger that tries to update conversation.updated_at
--        which doesn't exist, causing 500 errors when inserting messages:
--        ERROR: column "updated_at" of relation "conversation" does not exist
-- ============================================================================

-- Step 1: Drop existing trigger (created in V10)
DROP TRIGGER IF EXISTS trigger_update_conversation_timestamp ON message;

-- Step 2: Drop existing function (created in V10)
DROP FUNCTION IF EXISTS update_conversation_last_message();

-- Step 3: Create the corrected trigger function
-- Only updates last_message_at (removed the non-existent updated_at column)
CREATE OR REPLACE FUNCTION update_conversation_last_message()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the conversation's last_message_at timestamp
    -- whenever a new message is inserted
    UPDATE conversation
    SET last_message_at = NEW.sent_at
    WHERE conversation_id = NEW.conversation_id
      AND last_message_at < NEW.sent_at;  -- Only update if newer
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Step 4: Create the trigger that fires after message insert
CREATE TRIGGER trigger_update_conversation_timestamp
    AFTER INSERT ON message
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_last_message();

COMMENT ON FUNCTION update_conversation_last_message() IS
    'Automatically updates conversation.last_message_at when new message inserted. Fixed in V14 to remove non-existent updated_at column.';

-- ============================================================================
-- This migration fixes the 500 error when sending messages:
-- ERROR: column "updated_at" of relation "conversation" does not exist
-- Where: PL/pgSQL function update_conversation_last_message() line 3
-- ============================================================================
