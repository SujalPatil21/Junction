-- ============================================================
--  V2__seed_baseline_data.sql
--  Flyway Migration — Initial Baseline / Seed Data
--  Mirrors src/db/seedDevData.ts
-- ============================================================

-- 1. Seed Restaurant R1
INSERT INTO restaurants (
    id, name, zone, cuisine, address,
    total_capacity, available_covers, expected_reservations,
    price_range, lat, lng, is_active
) VALUES (
    'R1', 'Trishna', 'ZONE_A', 'Coastal Seafood', 'Kala Ghoda, Fort, Mumbai',
    80, 20, 45, '₹₹₹', 18.9282, 72.8318, true
) ON CONFLICT (id) DO NOTHING;

-- 2. Seed Dev Organizer Profile
INSERT INTO profiles (
    id, clerk_user_id, email, name, role
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'clerk_dev_organizer_001',
    'organizer@junction.local',
    'City Command Lead',
    'ORGANIZER'
) ON CONFLICT (clerk_user_id) DO NOTHING;

-- 3. Seed Dev Restaurant Partner Profile
INSERT INTO profiles (
    id, clerk_user_id, email, name, role
) VALUES (
    'a0000000-0000-0000-0000-000000000002',
    'clerk_dev_partner_001',
    'manager@trishna.local',
    'Trishna Operations Manager',
    'RESTAURANT_PARTNER'
) ON CONFLICT (clerk_user_id) DO NOTHING;

-- 4. Seed Restaurant Membership
INSERT INTO restaurant_memberships (
    id, profile_id, restaurant_id, role
) VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'R1',
    'MANAGER'
) ON CONFLICT (profile_id, restaurant_id) DO NOTHING;

-- 5. Seed Initial Inventory Log
INSERT INTO restaurant_inventory (
    id, restaurant_id, reported_by_profile_id,
    available_tables, available_covers, expected_covers, out_of_order_tables, notes
) VALUES (
    'c0000000-0000-0000-0000-000000000001',
    'R1',
    'a0000000-0000-0000-0000-000000000002',
    5, 20, 45, 0, 'Initial baseline capacity'
) ON CONFLICT (id) DO NOTHING;

