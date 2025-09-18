-- Fix for area slug column - remove generated constraint
--liquibase formatted sql

--changeset habib.machpud:fix-area-slug-column
--comment: Remove generated constraint from area slug column to allow manual input
-- Drop the existing generated column and recreate as regular TEXT column
ALTER TABLE area DROP COLUMN slug;
ALTER TABLE area ADD COLUMN slug TEXT;

-- Populate existing areas with generated slugs as initial values
UPDATE area
SET slug = regexp_replace(lower(name), '[^a-z0-9]+', '-', 'g')
WHERE slug IS NULL;

-- Now make it NOT NULL and add unique constraint
ALTER TABLE area ALTER COLUMN slug SET NOT NULL;

-- Recreate the unique index for active areas
CREATE UNIQUE INDEX idx_area_slug_unique ON area(slug) WHERE deleted_at IS NULL;

--rollback ALTER TABLE area DROP COLUMN slug; ALTER TABLE area ADD COLUMN slug TEXT GENERATED ALWAYS AS (regexp_replace(lower(name), '[^a-z0-9]+', '-', 'g')) STORED;