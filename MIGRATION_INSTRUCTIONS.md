# Database Migration: Fix Conversation Trigger

## Problem
The `update_conversation_last_message()` trigger function was trying to update a non-existent column `updated_at` in the `conversation` table, causing message sends to fail with a 500 error.

**Error:**
```
ERROR: column "updated_at" of relation "conversation" does not exist
Where: PL/pgSQL function update_conversation_last_message() line 3 at SQL statement
```

## Solution
This migration fixes the trigger to use the correct column: `last_message_at`

---

## How to Run the Migration

### Option 1: Using psql (Command Line)

1. **Connect to your database:**
   ```bash
   psql -U your_username -d castify_db
   ```

2. **Run the migration script:**
   ```bash
   \i /Users/lazarmarkovic/Documents/MyProjects/castify-project/castify/fix_conversation_trigger.sql
   ```

3. **Verify success:**
   You should see:
   ```
   DROP TRIGGER
   DROP FUNCTION
   CREATE FUNCTION
   CREATE TRIGGER
   ```

### Option 2: Using psql with Direct Path

```bash
psql -U your_username -d castify_db -f /Users/lazarmarkovic/Documents/MyProjects/castify-project/castify/fix_conversation_trigger.sql
```

### Option 3: Using DBeaver or pgAdmin

1. Open your database tool
2. Connect to the `castify_db` database
3. Open the SQL script: `fix_conversation_trigger.sql`
4. Execute the entire script

### Option 4: Using Docker (if your DB is in Docker)

```bash
docker exec -i your_postgres_container psql -U your_username -d castify_db < fix_conversation_trigger.sql
```

---

## Verification

After running the migration, verify it worked:

### 1. Check Trigger Exists
```sql
SELECT trigger_name, event_manipulation, event_object_table, action_statement
FROM information_schema.triggers
WHERE trigger_name = 'update_conversation_last_message_trigger';
```

**Expected result:**
| trigger_name | event_manipulation | event_object_table | action_statement |
|--------------|-------------------|-------------------|------------------|
| update_conversation_last_message_trigger | INSERT | message | EXECUTE FUNCTION update_conversation_last_message() |

### 2. Check Function Exists
```sql
SELECT routine_name, routine_type, data_type
FROM information_schema.routines
WHERE routine_name = 'update_conversation_last_message';
```

**Expected result:**
| routine_name | routine_type | data_type |
|--------------|--------------|-----------|
| update_conversation_last_message | FUNCTION | trigger |

### 3. Test Message Send

Go to your frontend app and try to send a message. It should now work! ✅

---

## What the Migration Does

1. **Drops old broken trigger** (if exists)
2. **Drops old broken function** (if exists)
3. **Creates new corrected function** that:
   - Updates `conversation.last_message_at` (not `updated_at`)
   - Uses the message's `sent_at` timestamp
   - Fires whenever a message is inserted
4. **Creates new trigger** that fires the function on INSERT

---

## Rollback (If Needed)

If you need to rollback this migration:

```sql
-- Remove the trigger and function
DROP TRIGGER IF EXISTS update_conversation_last_message_trigger ON message;
DROP FUNCTION IF EXISTS update_conversation_last_message();
```

⚠️ **Note:** You'll need to manually update `conversation.last_message_at` if you rollback.

---

## Expected Behavior After Fix

✅ Messages send successfully
✅ `conversation.last_message_at` updates automatically
✅ No more 500 errors when sending messages
✅ Conversations sort correctly by last message time

---

## Troubleshooting

### Error: "permission denied"
You need superuser or database owner permissions. Try:
```bash
psql -U postgres -d castify_db -f fix_conversation_trigger.sql
```

### Error: "database does not exist"
Replace `castify_db` with your actual database name:
```bash
psql -U your_username -d your_actual_db_name -f fix_conversation_trigger.sql
```

### Still getting 500 errors?
1. Restart your Spring Boot backend
2. Check backend logs for any other errors
3. Clear the Hibernate cache (restart should do this)

---

## Questions?

If you encounter any issues:
1. Check the backend logs
2. Verify the trigger exists using the verification queries above
3. Make sure you're connected to the correct database
