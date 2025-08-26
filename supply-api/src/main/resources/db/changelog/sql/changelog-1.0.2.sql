-- Liquibase SQL Changeset for Areas & Hotels Soft Deletes
--liquibase formatted sql

--changeset habib.machpud:alter-table-area-soft_delete
--comment: Add soft delete support to area table
ALTER TABLE area ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;

CREATE INDEX idx_area_active ON area(deleted_at) WHERE deleted_at IS NULL;
--rollback ALTER TABLE area DROP COLUMN deleted_at; DROP INDEX IF EXISTS idx_area_active;

--changeset habib.machpud:alter-table-hotel-soft_delete
--comment: Add soft delete support to hotel table
ALTER TABLE hotel ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;

-- Create index for deleted_at filtering
CREATE INDEX idx_hotel_deleted_at ON hotel(deleted_at) WHERE deleted_at IS NULL;

-- Create combined index for area filtering with both active states
CREATE INDEX idx_hotel_area_active_deleted ON hotel(area_id, active, deleted_at)
    WHERE active = TRUE AND deleted_at IS NULL;
--rollback ALTER TABLE hotel DROP COLUMN deleted_at; DROP INDEX IF EXISTS idx_hotel_deleted_at; DROP INDEX IF EXISTS idx_hotel_area_active_deleted;

--changeset habib.machpud:add-index-area-slug-constraint
--comment: Update area slug constraint to only apply to active records
DROP INDEX IF EXISTS idx_area_slug;

CREATE UNIQUE INDEX idx_area_slug_unique ON area(slug) WHERE deleted_at IS NULL;
--rollback DROP INDEX IF EXISTS idx_area_slug_unique; CREATE UNIQUE INDEX idx_area_slug ON area(slug);

--changeset habib.machpud:alter-table-hotel-slug
--comment: Add slug column to hotel table
ALTER TABLE hotel ADD COLUMN slug TEXT NOT NULL DEFAULT '';

-- Remove default now that we've populated existing records
ALTER TABLE hotel ALTER COLUMN slug DROP DEFAULT;

CREATE UNIQUE INDEX idx_hotel_slug_unique ON hotel(slug) WHERE deleted_at IS NULL;
--rollback ALTER TABLE hotel DROP COLUMN slug; DROP INDEX IF EXISTS idx_hotel_slug_unique;