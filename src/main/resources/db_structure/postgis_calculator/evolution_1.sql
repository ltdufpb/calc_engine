-- DDL

-- New Schema geometry_bases for new bases of the funcitons add in postgis_calculator
CREATE SCHEMA IF NOT EXISTS geometry_bases;

-- Permissions
GRANT USAGE, CREATE ON SCHEMA geometry_bases TO postgis_calculator;
ALTER DEFAULT PRIVILEGES IN SCHEMA geometry_bases GRANT ALL ON TABLES TO postgis_calculator;
ALTER DEFAULT PRIVILEGES IN SCHEMA geometry_bases GRANT ALL ON SEQUENCES TO postgis_calculator;



-- Rollback

--Drop the main geometry_bases schema
--DROP SCHEMA geometry_bases CASCADE;
