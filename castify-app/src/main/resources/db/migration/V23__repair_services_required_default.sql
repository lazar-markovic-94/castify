-- =====================================================
-- Migration V23: Repair the orphaned default on collaboration.services_required
-- =====================================================
-- V21 created the column as:
--     services_required service_type[] NOT NULL DEFAULT ARRAY[]::service_type[]
--
-- V22 then ran:
--     ALTER COLUMN services_required TYPE TEXT[] USING services_required::TEXT[];
--     DROP TYPE IF EXISTS service_type;
--
-- ALTER COLUMN ... TYPE rewrites the column but NOT the stored default
-- expression, so the pg_attrdef entry kept its cast to service_type. Dropping
-- the enum then left that expression pointing at a type OID that no longer
-- exists.
--
-- The table still works for inserts that supply a value (which the application
-- always does), so this went unnoticed. But anything that renders the default
-- fails hard:
--     pg_dump                -> "cache lookup failed for type NNNNN", no backups
--     psql \d collaboration  -> same error
--
-- Dropping and re-adding the default rebuilds the expression against TEXT[].
-- Safe to run on a healthy database too: DROP DEFAULT only deletes the
-- pg_attrdef row and never evaluates the broken expression.
-- =====================================================

ALTER TABLE collaboration
    ALTER COLUMN services_required DROP DEFAULT;

ALTER TABLE collaboration
    ALTER COLUMN services_required SET DEFAULT ARRAY[]::TEXT[];
