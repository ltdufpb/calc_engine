-- Privileges postgis_calculator calculator_engine
GRANT USAGE, CREATE ON SCHEMA public TO calculator_engine;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO calculator_engine;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO calculator_engine;