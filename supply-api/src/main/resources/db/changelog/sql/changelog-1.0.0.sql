-- Extensions
-- changeset habib.machpud:create-extension_pg_trgm
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Area / Hotel / RoomType / Plan (minimal columns)
-- changeset habib.machpud:create-table-area
CREATE TABLE IF NOT EXISTS area (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  slug TEXT GENERATED ALWAYS AS (regexp_replace(lower(name), '[^a-z0-9]+', '-', 'g')) STORED
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_area_slug ON area(slug);

-- changeset habib.machpud:create-table-hotel
CREATE TABLE IF NOT EXISTS hotel (
  id BIGSERIAL PRIMARY KEY,
  area_id BIGINT NOT NULL REFERENCES area(id) ON DELETE RESTRICT,
  name TEXT NOT NULL,
  timezone TEXT NOT NULL,              -- e.g., 'Asia/Jakarta'
  rank NUMERIC(6,5) NOT NULL DEFAULT 0.0
);

-- changeset habib.machpud:create-table-room_type
CREATE TABLE IF NOT EXISTS room_type (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT NOT NULL REFERENCES hotel(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  capacity_max INT NOT NULL CHECK (capacity_max > 0)
);
CREATE INDEX IF NOT EXISTS idx_roomtype_hotel ON room_type(hotel_id);

-- changeset habib.machpud:create-table-plan
CREATE TABLE IF NOT EXISTS plan (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'IDR',
  refundable BOOLEAN NOT NULL DEFAULT TRUE,
  board_type TEXT,
  pricing TEXT NOT NULL DEFAULT 'NIGHTLY' CHECK (pricing IN ('NIGHTLY','LOS'))
);

-- changeset habib.machpud:create-table-plan_room_type
CREATE TABLE IF NOT EXISTS plan_room_type (
  plan_id BIGINT NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
  room_type_id BIGINT NOT NULL REFERENCES room_type(id) ON DELETE CASCADE,
  occupancy_min INT,
  occupancy_max INT,
  extra_guest_fee_minor INT NOT NULL DEFAULT 0,
  PRIMARY KEY (plan_id, room_type_id)
);

-- INVENTORY (partitioned by dt)
-- changeset habib.machpud:create-table-room_type_inventory
CREATE TABLE IF NOT EXISTS room_type_inventory (
  room_type_id BIGINT NOT NULL REFERENCES room_type(id) ON DELETE CASCADE,
  dt DATE NOT NULL,
  allotment INT NOT NULL CHECK (allotment >= 0),
  stop_sell BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (room_type_id, dt)
) PARTITION BY RANGE (dt);

-- NIGHTLY RATES (partitioned by dt)
-- changeset habib.machpud:create-table-rate_nightly
CREATE TABLE IF NOT EXISTS rate_nightly (
  plan_id BIGINT NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
  dt DATE NOT NULL,
  occupancy_from INT NOT NULL DEFAULT 1 CHECK (occupancy_from >= 1),
  amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (plan_id, dt, occupancy_from)
) PARTITION BY RANGE (dt);

-- LOS RATES (partitioned by checkin_dt)
-- changeset habib.machpud:create-table-rate_los
CREATE TABLE IF NOT EXISTS rate_los (
  plan_id BIGINT NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
  checkin_dt DATE NOT NULL,
  los INT NOT NULL CHECK (los >= 1 AND los <= 30),
  occupancy_from INT NOT NULL DEFAULT 1 CHECK (occupancy_from >= 1),
  amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (plan_id, checkin_dt, los, occupancy_from)
) PARTITION BY RANGE (checkin_dt);

-- PLAN RESTRICTIONS (partitioned by dt)
-- changeset habib.machpud:create-table-plan_restriction
CREATE TABLE IF NOT EXISTS plan_restriction (
  plan_id BIGINT NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
  dt DATE NOT NULL,
  cta BOOLEAN NOT NULL DEFAULT FALSE,
  ctd BOOLEAN NOT NULL DEFAULT FALSE,
  min_los INT,
  max_los INT,
  min_advance_days INT,
  max_advance_days INT,
  closed BOOLEAN NOT NULL DEFAULT FALSE,     -- plan-level stop-sell for stay date
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (plan_id, dt)
) PARTITION BY RANGE (dt);

-- Helpful indexes (partials keep hot ranges small)
-- changeset habib.machpud:create-index-init
CREATE INDEX IF NOT EXISTS idx_inv_room_dt ON room_type_inventory(room_type_id, dt);
CREATE INDEX IF NOT EXISTS idx_rate_nightly_plan_dt ON rate_nightly(plan_id, dt);
CREATE INDEX IF NOT EXISTS idx_rate_los_plan_dt ON rate_los(plan_id, checkin_dt);
CREATE INDEX IF NOT EXISTS idx_restr_plan_dt ON plan_restriction(plan_id, dt);
