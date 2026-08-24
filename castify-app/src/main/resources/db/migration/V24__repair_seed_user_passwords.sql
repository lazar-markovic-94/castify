-- =====================================================
-- Migration V24: Give the seeded demo users a usable password
-- =====================================================
-- V2 seeded every demo account with this hash:
--
--     $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka
--
-- and a comment claiming the password was "password123". It is not. That
-- string is a placeholder copied around countless tutorials and it verifies
-- against nothing - BCryptPasswordEncoder.matches() returns false for every
-- input. Nobody noticed because LoginUserUseCase never called matches() at
-- all, so any password logged these accounts in.
--
-- With password verification in place the demo accounts became unusable, so
-- this sets a hash that genuinely corresponds to "password123", generated with
-- the same BCryptPasswordEncoder the application authenticates with.
--
-- The WHERE clause targets only rows still carrying the placeholder, so a real
-- user's password can never be overwritten by this migration.
--
-- These are demo credentials for a development environment. Delete these
-- accounts before this database holds anything that matters.
-- =====================================================

UPDATE app_user
SET password_hash = '$2a$10$RuRiEKq.uW.ZHF3TIEWEvuUuAkI0r8MAmcJyaPB7g6H/xvm8/H6.C',
    updated_at    = CURRENT_TIMESTAMP
WHERE password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka';
