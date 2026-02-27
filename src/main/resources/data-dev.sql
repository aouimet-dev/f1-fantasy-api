-- data-dev.sql
-- Simple development seed data for `members` and `member_teams`
-- Creates 3 members, each with 3 teams (9 member_team rows)

BEGIN;

-- Clear existing data (development only)
-- remove dependent results first
DELETE FROM standings_history;
DELETE FROM standings;
DELETE FROM race_results;
DELETE FROM member_teams;
DELETE FROM members;
DELETE FROM races;

-- Insert members
INSERT INTO members (id, name, email, created_at, updated_at) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Alice Example', 'alice@example.com', NOW(), NOW()),
  ('22222222-2222-2222-2222-222222222222', 'Bob Example', 'bob@example.com', NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333333', 'Charlie Example', 'charlie@example.com', NOW(), NOW());

-- Insert races (3 sample races)
INSERT INTO races (id, race_number, race_name, circuit_name, country, race_date, is_completed, created_at) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 1, 'Australian Grand Prix', 'Albert Park', 'Australia', '2026-03-21', false, NOW()),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 2, 'Bahrain Grand Prix', 'Bahrain International Circuit', 'Bahrain', '2026-03-28', false, NOW()),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 3, 'Saudi Arabian Grand Prix', 'Jeddah Corniche Circuit', 'Saudi Arabia', '2026-04-04', false, NOW());

-- Insert member teams (3 teams per member)
INSERT INTO member_teams (id, member_id, team_name, team_order, created_at) VALUES
  ('44444444-4444-4444-4444-444444444441', '11111111-1111-1111-1111-111111111111', 'Alice Team 1', 1, NOW()),
  ('44444444-4444-4444-4444-444444444442', '11111111-1111-1111-1111-111111111111', 'Alice Team 2', 2, NOW()),
  ('44444444-4444-4444-4444-444444444443', '11111111-1111-1111-1111-111111111111', 'Alice Team 3', 3, NOW()),
  ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'Bob Team 1', 1, NOW()),
  ('44444444-4444-4444-4444-444444444445', '22222222-2222-2222-2222-222222222222', 'Bob Team 2', 2, NOW()),
  ('44444444-4444-4444-4444-444444444446', '22222222-2222-2222-2222-222222222222', 'Bob Team 3', 3, NOW()),
  ('44444444-4444-4444-4444-444444444447', '33333333-3333-3333-3333-333333333333', 'Charlie Team 1', 1, NOW()),
  ('44444444-4444-4444-4444-444444444448', '33333333-3333-3333-3333-333333333333', 'Charlie Team 2', 2, NOW()),
  ('44444444-4444-4444-4444-444444444449', '33333333-3333-3333-3333-333333333333', 'Charlie Team 3', 3, NOW());

COMMIT;

-- Notes:
-- 1) This script assumes the columns `created_at`/`updated_at` and names used here exist.
-- 2) If your DB uses sequences for `id` (Postgres), you may need to reset them after running.
