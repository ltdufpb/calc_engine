-- New database
CREATE SCHEMA IF NOT EXISTS engine_configuration;

-- Grant on the specific engine_configuration schema
GRANT CREATE, USAGE ON SCHEMA engine_configuration TO calculator_engine;

-- New table spatial_function define functions that are registered in postgis_calculator
CREATE TABLE engine_configuration.spatial_function (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version INT NOT NULL,
    sql_definition TEXT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parameters TEXT,
    UNIQUE (name, version)
);

-- Add comments to the columns of the 'spatial_function' table
COMMENT ON COLUMN engine_configuration.spatial_function.id IS 'Unique identifier for this specific version of the spatial function.';
COMMENT ON COLUMN engine_configuration.spatial_function.name IS 'Logical name of the spatial function (used for referencing in operations/workflows).';
COMMENT ON COLUMN engine_configuration.spatial_function.version IS 'Version number for the spatial function (allowing multiple implementations of the same logical function).';
COMMENT ON COLUMN engine_configuration.spatial_function.sql_definition IS 'The raw SQL code (often using IN database postgis_calculator) to be executed. Uses placeholders like $1, $2 for parameters.';
COMMENT ON COLUMN engine_configuration.spatial_function.active IS 'Flag indicating if this specific version of the function is the currently active one to be used by default.';
COMMENT ON COLUMN engine_configuration.spatial_function.created_at IS 'Timestamp when this function version record was created.';
COMMENT ON COLUMN engine_configuration.spatial_function.parameters IS 'Comma-separated, ordered list of parameter names expected by the sql_definition (e.g., ''wkt'', ''wkt,buffer_distance'').';

-- Grant UPDATE, SELECT and INSERT on the specific spatial_function table
GRANT REFERENCES, UPDATE, SELECT, INSERT ON TABLE engine_configuration.spatial_function TO calculator_engine;

-- New table engine_configuration define layer configurations for calculate in engine calculator
CREATE TABLE engine_configuration.layer (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    geometry_type VARCHAR(50) NOT NULL,
    group_name VARCHAR(255),
    allow_overlap BOOLEAN DEFAULT TRUE,
    overlap_restrictions TEXT,
    generate_buffer BOOLEAN DEFAULT FALSE,
    buffer_size DOUBLE PRECISION,
    calculate_area BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Add comments to the columns of the 'layer' table
COMMENT ON COLUMN engine_configuration.layer.id IS 'Unique identifier for the layer.';
COMMENT ON COLUMN engine_configuration.layer.name IS 'Unique descriptive name of the layer (e.g., ''Property'', ''Protected_Area'').';
COMMENT ON COLUMN engine_configuration.layer.geometry_type IS 'Expected geometry type for this layer (e.g., ''POLYGON'', ''LINESTRING'', ''POINT'').';
COMMENT ON COLUMN engine_configuration.layer.group_name IS 'Logical group the layer belongs to (e.g., ''Environmental'', ''Land_Registry'').';
COMMENT ON COLUMN engine_configuration.layer.allow_overlap IS 'Flag indicating if geometries in this layer generally allow overlapping with others.';
COMMENT ON COLUMN engine_configuration.layer.overlap_restrictions IS 'JSON array (stored as TEXT) containing IDs of layers that geometries from this layer *cannot* overlap.';
COMMENT ON COLUMN engine_configuration.layer.generate_buffer IS 'Flag indicating if a buffer calculation is typically associated with this layer.';
COMMENT ON COLUMN engine_configuration.layer.buffer_size IS 'Default buffer distance/size to be used if generate_buffer is true (units depend on SRID).';
COMMENT ON COLUMN engine_configuration.layer.calculate_area IS 'Flag indicating if area calculation is typically associated with this layer.';
COMMENT ON COLUMN engine_configuration.layer.created_at IS 'Timestamp when the layer record was created.';
COMMENT ON COLUMN engine_configuration.layer.active IS 'Flag indicating if the layer definition is currently active and usable.';

-- Grant USAGE, UPDATE, SELECT and INSERT on the specific layer table
GRANT REFERENCES, UPDATE, SELECT, INSERT ON TABLE engine_configuration.layer TO calculator_engine;
GRANT USAGE ON SEQUENCE engine_configuration.layer_id_seq TO calculator_engine;



GRANT USAGE ON SEQUENCE engine_configuration.spatial_function_id_seq TO calculator_engine;




--##-- Rollback

--Drop the main spatial_function table
--DROP TABLE IF EXISTS spatial_function;

--Drop the main layer table
--DROP TABLE IF EXISTS layer;

--Drop the main engine_configuration schema
--DROP SCHEMA engine_configuration CASCADE;