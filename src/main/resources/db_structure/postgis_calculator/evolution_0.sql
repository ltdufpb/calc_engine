-- Privileges and permitions postgis_calculator
GRANT ALL PRIVILEGES ON DATABASE postgis_calculator TO postgis_calculator;
GRANT CREATE, CONNECT, TEMPORARY ON DATABASE postgis_calculator TO postgis_calculator;
GRANT USAGE, CREATE ON SCHEMA public TO postgis_calculator;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgis_calculator;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgis_calculator;

-- Connect on postgis_calculator and execute sql: (\c postgis_calculator)
CREATE EXTENSION postgis;

