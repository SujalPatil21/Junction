-- ============================================================
--  V1__create_junction_schema.sql
--  Flyway Migration — JUNCTION Initial Schema
--
--  Source of truth: src/db/schema.ts (Drizzle ORM definitions)
--  Verified column names as of 2026-11-06.
--
--  IMPORTANT:
--  - This migration creates the identical schema to what Drizzle ORM manages.
--  - Column names are EXACT matches to schema.ts pgColumn("column_name") calls.
--  - Do NOT run against a live production DB without first running:
--      mvn flyway:baseline (to record existing schema as V1 baseline)
--  - Hibernate ddl-auto: validate will verify this schema on startup.
-- ============================================================

-- ── Enable UUID generation (PostgreSQL 13+: gen_random_uuid() is built-in) ──
-- No extension needed for gen_random_uuid() in PostgreSQL 13+.

-- ── Custom Enums ──────────────────────────────────────────────────────────────

CREATE TYPE user_role AS ENUM (
    'ORGANIZER',
    'RESTAURANT_PARTNER',
    'ATTENDEE'
);

CREATE TYPE membership_role AS ENUM (
    'OWNER',
    'MANAGER',
    'STAFF'
);

-- ── 1. PROFILES ───────────────────────────────────────────────────────────────
-- Linked directly to Clerk user IDs. One profile per Clerk user.
-- clerk_user_id is the external identity anchor — never changes.

CREATE TABLE profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clerk_user_id   TEXT        NOT NULL UNIQUE,
    name            TEXT,
    email           TEXT        NOT NULL,
    role            user_role   NOT NULL DEFAULT 'ATTENDEE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_clerk_user_id ON profiles (clerk_user_id);
CREATE INDEX idx_profiles_role          ON profiles (role);

COMMENT ON TABLE  profiles              IS 'Junction user profiles, linked to Clerk identity provider';
COMMENT ON COLUMN profiles.clerk_user_id IS 'Clerk user_* ID — primary external identity reference';
COMMENT ON COLUMN profiles.role          IS 'Junction domain role (ORGANIZER | RESTAURANT_PARTNER | ATTENDEE)';

-- ── 2. RESTAURANTS ────────────────────────────────────────────────────────────
-- Hospitality partner venues participating in the Junction orchestration.

CREATE TABLE restaurants (
    id                      TEXT        PRIMARY KEY,  -- e.g. "R1", "R2", "R3"
    name                    TEXT        NOT NULL,
    zone                    TEXT        NOT NULL,     -- e.g. "ZONE_A", "ZONE_B"
    cuisine                 TEXT,
    address                 TEXT,
    total_capacity          INTEGER     NOT NULL DEFAULT 100,
    available_covers        INTEGER     NOT NULL DEFAULT 50,
    expected_reservations   INTEGER     NOT NULL DEFAULT 0,
    price_range             TEXT                 DEFAULT '₹₹',
    lat                     DOUBLE PRECISION,
    lng                     DOUBLE PRECISION,
    is_active               BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_restaurants_zone      ON restaurants (zone);
CREATE INDEX idx_restaurants_is_active ON restaurants (is_active);

COMMENT ON TABLE  restaurants           IS 'Hospitality partner restaurants in the Junction platform';
COMMENT ON COLUMN restaurants.id        IS 'Business-assigned ID (e.g. R1, R2). Text PK matching TypeScript source.';
COMMENT ON COLUMN restaurants.zone      IS 'Operational zone identifier (e.g. ZONE_A, ZONE_B, ZONE_WANKHEDE)';

-- ── 3. RESTAURANT MEMBERSHIPS ─────────────────────────────────────────────────
-- Scoped access control — which profiles can manage which restaurants.
-- UNIQUE constraint prevents duplicate memberships.

CREATE TABLE restaurant_memberships (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id      UUID            NOT NULL REFERENCES profiles(id)     ON DELETE CASCADE,
    restaurant_id   TEXT            NOT NULL REFERENCES restaurants(id)  ON DELETE CASCADE,
    role            membership_role NOT NULL DEFAULT 'STAFF',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_restaurant_membership UNIQUE (profile_id, restaurant_id)
);

CREATE INDEX idx_restaurant_memberships_profile_id    ON restaurant_memberships (profile_id);
CREATE INDEX idx_restaurant_memberships_restaurant_id ON restaurant_memberships (restaurant_id);

COMMENT ON TABLE  restaurant_memberships IS 'Maps profiles to restaurants with scoped membership roles';
COMMENT ON COLUMN restaurant_memberships.role IS 'Membership role (OWNER | MANAGER | STAFF)';

-- ── 4. RESTAURANT INVENTORY ───────────────────────────────────────────────────
-- Live telemetry and cover updates submitted by restaurant partners.
-- reported_by_profile_id SET NULL on profile deletion (preserves history).

CREATE TABLE restaurant_inventory (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id           TEXT        NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    reported_by_profile_id  UUID        REFERENCES profiles(id)             ON DELETE SET NULL,
    available_tables        INTEGER     NOT NULL DEFAULT 0,
    available_covers        INTEGER     NOT NULL DEFAULT 0,
    expected_covers         INTEGER     NOT NULL DEFAULT 0,
    out_of_order_tables     INTEGER     NOT NULL DEFAULT 0,
    notes                   TEXT,
    recorded_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_restaurant_inventory_restaurant_id ON restaurant_inventory (restaurant_id);
CREATE INDEX idx_restaurant_inventory_recorded_at   ON restaurant_inventory (recorded_at DESC);

COMMENT ON TABLE  restaurant_inventory IS 'Live inventory telemetry from restaurant partners';
COMMENT ON COLUMN restaurant_inventory.reported_by_profile_id IS 'Profile who submitted — SET NULL on profile deletion to preserve audit trail';
COMMENT ON COLUMN restaurant_inventory.recorded_at IS 'Wall clock time of the inventory report';

-- ── 5. AUDIT EVENTS ───────────────────────────────────────────────────────────
-- IMMUTABLE security and decision audit trail. Never update or delete rows.
-- column: new_state_json (NOT newState_json — verified against schema.ts L87)

CREATE TABLE audit_events (
    id                  TEXT        PRIMARY KEY,
    actor_id            TEXT        NOT NULL,
    actor_role          TEXT        NOT NULL,
    category            TEXT        NOT NULL,
    target_entity_type  TEXT        NOT NULL,
    target_entity_id    TEXT        NOT NULL,
    change_summary      TEXT        NOT NULL,
    rationale           TEXT,
    previous_state_json TEXT,
    new_state_json      TEXT,  -- column name verified: schema.ts L87 newStateJson: text("new_state_json")
    is_simulated_scenario BOOLEAN   NOT NULL DEFAULT TRUE,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_actor_id          ON audit_events (actor_id);
CREATE INDEX idx_audit_events_target_entity_id  ON audit_events (target_entity_id);
CREATE INDEX idx_audit_events_category          ON audit_events (category);
CREATE INDEX idx_audit_events_timestamp         ON audit_events (timestamp DESC);

COMMENT ON TABLE  audit_events IS 'Immutable audit trail for all Junction operational decisions. Never update or delete.';
COMMENT ON COLUMN audit_events.new_state_json IS 'JSON snapshot of entity state after the change';
COMMENT ON COLUMN audit_events.is_simulated_scenario IS 'TRUE for simulation/prototype events; FALSE for real operational events';

-- ── 6. OPERATIONAL INTERVENTIONS ─────────────────────────────────────────────
-- Stateful recommendation lifecycle (PROPOSED → APPROVED/REJECTED → EXECUTED).

CREATE TABLE operational_interventions (
    id                                  TEXT            PRIMARY KEY,
    type                                TEXT            NOT NULL,
    title                               TEXT            NOT NULL,
    description                         TEXT            NOT NULL,
    target_zone_id                      TEXT            NOT NULL,
    status                              TEXT            NOT NULL DEFAULT 'PROPOSED',
    urgency                             TEXT            NOT NULL DEFAULT 'MEDIUM',
    requires_approval                   BOOLEAN         NOT NULL DEFAULT TRUE,
    approval_role_required              TEXT,
    rationale                           TEXT            NOT NULL,
    confidence_score                    DOUBLE PRECISION NOT NULL DEFAULT 0.8,
    expected_pressure_reduction_percent INTEGER         NOT NULL DEFAULT 0,
    proposed_at                         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expires_at                          TIMESTAMPTZ     NOT NULL,
    approved_at                         TIMESTAMPTZ,
    approved_by_user_id                 TEXT,
    rejection_reason                    TEXT
);

CREATE INDEX idx_operational_interventions_status        ON operational_interventions (status);
CREATE INDEX idx_operational_interventions_target_zone   ON operational_interventions (target_zone_id);
CREATE INDEX idx_operational_interventions_proposed_at   ON operational_interventions (proposed_at DESC);
CREATE INDEX idx_operational_interventions_expires_at    ON operational_interventions (expires_at);

COMMENT ON TABLE  operational_interventions IS 'Recommendation lifecycle — PROPOSED → APPROVED/REJECTED → EXECUTED';
COMMENT ON COLUMN operational_interventions.status IS 'Lifecycle state: PROPOSED | APPROVED | REJECTED | EXECUTED';
COMMENT ON COLUMN operational_interventions.confidence_score IS 'AI confidence (0.0–1.0) from sensor fusion and forecasting engines';
COMMENT ON COLUMN operational_interventions.expires_at IS 'Recommendation expires if not actioned by this timestamp';

