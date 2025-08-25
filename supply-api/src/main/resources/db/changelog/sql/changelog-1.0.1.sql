-- changeset habib.machpud:alter-table-hotel-loc-contact
-- Location & Contact Info
ALTER TABLE hotel
ADD COLUMN address TEXT,
ADD COLUMN city TEXT,
ADD COLUMN country_code CHAR(2), -- ISO 3166-1 alpha-2
ADD COLUMN location POINT, -- Built-in PostgreSQL point type (longitude, latitude)
ADD COLUMN phone TEXT,
ADD COLUMN email TEXT,
ADD COLUMN website TEXT,
ADD COLUMN star_rating DECIMAL(2,1) CHECK (star_rating >= 0 AND star_rating <= 5),
ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'IDR', -- Hotel's base currency
ADD COLUMN check_in_time TIME DEFAULT '14:00:00',
ADD COLUMN check_out_time TIME DEFAULT '12:00:00',
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_hotel_location ON hotel USING gist(location);
CREATE INDEX idx_hotel_active ON hotel(active) WHERE active = TRUE;

-- changeset habib.machpud:alter-table-room_type-physical
-- Physical Attributes
ALTER TABLE room_type
ADD COLUMN description TEXT,
ADD COLUMN size_sqm DECIMAL(6,2), -- Room size in square meters
ADD COLUMN bed_type TEXT, -- 'SINGLE', 'DOUBLE', 'QUEEN', 'KING', 'TWIN'
ADD COLUMN bed_count INTEGER DEFAULT 1,
ADD COLUMN bathroom_type TEXT, -- 'PRIVATE', 'SHARED'
ADD COLUMN amenities TEXT[], -- Array: ['wifi', 'ac', 'tv', 'minibar', 'balcony']
ADD COLUMN max_adults INTEGER NOT NULL DEFAULT 2,
ADD COLUMN max_children INTEGER NOT NULL DEFAULT 2,
ADD COLUMN images TEXT[], -- Array of image URLs
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_roomtype_amenities ON room_type USING gin(amenities);
CREATE INDEX idx_roomtype_active ON room_type(active) WHERE active = TRUE;

-- changeset habib.machpud:alter-table-plan-commercial
-- Commercial Details
ALTER TABLE plan
ADD COLUMN description TEXT,
ADD COLUMN cancellation_policy TEXT, -- 'FLEXIBLE', 'MODERATE', 'STRICT', 'NON_REFUNDABLE'
ADD COLUMN payment_type TEXT NOT NULL DEFAULT 'PREPAID', -- 'PREPAID', 'PAY_AT_HOTEL'
ADD COLUMN included_services TEXT[], -- ['breakfast', 'wifi', 'parking', 'airport_transfer']
ADD COLUMN terms_conditions TEXT,
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_plan_active ON plan(active) WHERE active = TRUE;
CREATE INDEX idx_plan_services ON plan USING gin(included_services);

-- changeset habib.machpud:add-table-hotel_amenity
-- Hotel Amenities
CREATE TABLE hotel_amenity (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT NOT NULL REFERENCES hotel(id) ON DELETE CASCADE,
  amenity_type TEXT NOT NULL, -- 'POOL', 'GYM', 'SPA', 'RESTAURANT', 'WIFI', 'PARKING'
  amenity_name TEXT NOT NULL,
  description TEXT,
  is_free BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_hotel_amenity_hotel ON hotel_amenity(hotel_id);
CREATE INDEX idx_hotel_amenity_type ON hotel_amenity(amenity_type);

-- changeset habib.machpud:add-table-hotel_contact
CREATE TABLE hotel_contact (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT NOT NULL REFERENCES hotel(id) ON DELETE CASCADE,
  contact_type TEXT NOT NULL, -- 'RESERVATIONS', 'FRONT_DESK', 'MANAGEMENT'
  name TEXT,
  phone TEXT,
  email TEXT,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_hotel_contact_hotel ON hotel_contact(hotel_id);
CREATE UNIQUE INDEX idx_hotel_contact_primary ON hotel_contact(hotel_id, contact_type) WHERE is_primary = TRUE;

-- changeset habib.machpud:alter-table-area
ALTER TABLE area
ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Add update triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_hotel_updated_at BEFORE UPDATE ON hotel
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_room_type_updated_at BEFORE UPDATE ON room_type
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plan_updated_at BEFORE UPDATE ON plan
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- changeset habib.machpud:add-table-system_config
CREATE TABLE system_config (
  id BIGSERIAL PRIMARY KEY,
  config_key TEXT NOT NULL UNIQUE,
  config_value TEXT NOT NULL,
  description TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO system_config (config_key, config_value, description) VALUES
('inventory.default_allotment', '10', 'Default room allotment when not specified'),
('inventory.max_advance_days', '365', 'Maximum days in advance for inventory'),
('rates.default_currency', 'IDR', 'System default currency'),
('partitions.auto_create', 'true', 'Auto-create missing partitions'),
('partitions.retention_months', '24', 'Months to retain historical data');

-- changeset habib.machpud:add-table-reservation
-- Booking/reservation placeholder (for future)
CREATE TABLE reservation (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT NOT NULL REFERENCES hotel(id),
  room_type_id BIGINT NOT NULL REFERENCES room_type(id),
  plan_id BIGINT NOT NULL REFERENCES plan(id),
  checkin_date DATE NOT NULL,
  checkout_date DATE NOT NULL,
  guest_count INTEGER NOT NULL,
  status TEXT NOT NULL DEFAULT 'CONFIRMED', -- 'PENDING', 'CONFIRMED', 'CANCELLED'
  total_amount_minor BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  guest_name TEXT NOT NULL,
  guest_email TEXT,
  guest_phone TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservation_hotel_dates ON reservation(hotel_id, checkin_date, checkout_date);
CREATE INDEX idx_reservation_status ON reservation(status);

-- changeset habib.machpud:add-index-hotel-search_optimize
-- Hotel search optimization
CREATE INDEX idx_hotel_city_active ON hotel(city, active) WHERE active = TRUE;
CREATE INDEX idx_hotel_rank_active ON hotel(rank DESC, active) WHERE active = TRUE;

-- changeset habib.machpud:add-index-hotel-full_text
-- Full-text search on hotel names (if needed)
CREATE INDEX idx_hotel_name_fts ON hotel USING gin(to_tsvector('english', name));

-- changeset habib.machpud:add-index-room_type-capacity
-- Room type search by capacity
CREATE INDEX idx_roomtype_capacity ON room_type(capacity_max, active) WHERE active = TRUE;

-- changeset habib.machpud:alter-table-plan_restriction
-- hour-level rules
ALTER TABLE plan_restriction
ADD COLUMN min_advance_hours INT CHECK (min_advance_hours IS NULL OR min_advance_hours >= 0),
ADD COLUMN max_advance_hours INT CHECK (max_advance_hours IS NULL OR max_advance_hours >= 0),
ADD COLUMN booking_cutoff_time TIME;