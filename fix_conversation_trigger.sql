-- ============================================================================
-- Migration: Fix conversation last_message_at trigger
-- Date: 2025-12-23
-- Description: Fixes the update_conversation_last_message() trigger function
--              to use the correct column name (last_message_at instead of updated_at)
-- ============================================================================

-- Step 1: Drop existing trigger if it exists
DROP TRIGGER IF EXISTS update_conversation_last_message_trigger ON message;

-- Step 2: Drop existing function if it exists
DROP FUNCTION IF EXISTS update_conversation_last_message();

-- Step 3: Create the corrected trigger function
CREATE OR REPLACE FUNCTION update_conversation_last_message()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the conversation's last_message_at timestamp
    -- whenever a new message is inserted
    UPDATE conversation
    SET last_message_at = NEW.sent_at
    WHERE conversation_id = NEW.conversation_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Step 4: Create the trigger that fires after message insert
CREATE TRIGGER update_conversation_last_message_trigger
    AFTER INSERT ON message
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_last_message();

-- ============================================================================
-- Verification Queries (Run these to verify the fix)
-- ============================================================================

-- Check if the trigger exists
-- SELECT trigger_name, event_manipulation, event_object_table, action_statement
-- FROM information_schema.triggers
-- WHERE trigger_name = 'update_conversation_last_message_trigger';

-- Check if the function exists
-- SELECT routine_name, routine_type, data_type
-- FROM information_schema.routines
-- WHERE routine_name = 'update_conversation_last_message';

-- ============================================================================
-- Test (Optional - uncomment to test)
-- ============================================================================

-- Test the trigger by inserting a test message
-- DO $$
-- DECLARE
--     test_conversation_id UUID;
--     test_message_id UUID;
--     old_last_message_at TIMESTAMP;
--     new_last_message_at TIMESTAMP;
-- BEGIN
--     -- Get a random existing conversation
--     SELECT conversation_id INTO test_conversation_id
--     FROM conversation
--     LIMIT 1;

--     IF test_conversation_id IS NOT NULL THEN
--         -- Get current last_message_at
--         SELECT last_message_at INTO old_last_message_at
--         FROM conversation
--         WHERE conversation_id = test_conversation_id;

--         -- Insert a test message
--         test_message_id := gen_random_uuid();
--         INSERT INTO message (message_id, conversation_id, sender_user_id, content, sent_at, is_read)
--         VALUES (
--             test_message_id,
--             test_conversation_id,
--             (SELECT client_user_id FROM conversation WHERE conversation_id = test_conversation_id),
--             'Test message - can be deleted',
--             NOW(),
--             false
--         );

--         -- Get new last_message_at
--         SELECT last_message_at INTO new_last_message_at
--         FROM conversation
--         WHERE conversation_id = test_conversation_id;

--         -- Verify the trigger worked
--         IF new_last_message_at > old_last_message_at THEN
--             RAISE NOTICE 'SUCCESS: Trigger working correctly! Old: %, New: %', old_last_message_at, new_last_message_at;
--         ELSE
--             RAISE NOTICE 'FAILED: Trigger did not update last_message_at';
--         END IF;

--         -- Clean up test message
--         DELETE FROM message WHERE message_id = test_message_id;
--         RAISE NOTICE 'Test message cleaned up';
--     ELSE
--         RAISE NOTICE 'No conversations found to test with';
--     END IF;
-- END $$;

-- ============================================================================
-- Done!
-- ============================================================================
