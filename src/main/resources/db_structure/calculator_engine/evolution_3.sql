-- Register the GeoJSON union function
INSERT INTO engine_configuration.spatial_function (
    name,
    version,
    sql_definition,
    active,
    parameters
) VALUES (
    'union',
    1,
    'SELECT public.process_geojson_union($1::text)',
    true,
    'geojson_input'
);


INSERT INTO engine_configuration.workflow_task (
    workflow_id,
    spatial_function_id,
    task_alias,
    description,
    created_at
) VALUES (
	2,
	2,
	'Calculate_union',
	'Calculates the union area',
	now()
);


-- Rollback
-- DELETE FROM engine_configuration.spatial_function WHERE name = 'union';
-- DELETE FROM engine_configuration.workflow_task WHERE task_alias = 'Calculate_union';