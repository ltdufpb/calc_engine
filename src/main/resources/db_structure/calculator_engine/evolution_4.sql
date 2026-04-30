DO $$
DECLARE
    workflow_id_var bigint;
    sf_id_1 bigint;
    sf_id_2 bigint;
    sf_id_3 bigint;
    sf_id_4 bigint;
    sf_id_5 bigint;
    sf_id_6 bigint;
    sf_id_7 bigint;
    sf_id_8 bigint;
    sf_id_9 bigint;
    wt_id_1 bigint;
    wt_id_2 bigint;
    wt_id_3 bigint;
    wt_id_4 bigint;
    wt_id_5 bigint;
    wt_id_6 bigint;
    wt_id_7 bigint;
    wt_id_8 bigint;
    wt_id_9 bigint;
    td_id_1 bigint;
    td_id_2 bigint;
    td_id_3 bigint;
    td_id_4 bigint;
    td_id_5 bigint;
    td_id_6 bigint;
    td_id_7 bigint;
    td_id_8 bigint;
BEGIN
    -- WORKFLOW
    INSERT INTO engine_configuration.workflow
    (id, "name", description, active, created_at, updated_at)
    VALUES(nextval('engine_configuration.workflow_id_seq'), 'CAR_DPG', 'CAR DPG calculation tree', true, NOW(), NOW())
    RETURNING id INTO workflow_id_var;

    -- SPATIAL_FUNCTIONS
    sf_id_1 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_2 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_3 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_4 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_5 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_6 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_7 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_8 := nextval('engine_configuration.spatial_function_id_seq');
    sf_id_9 := nextval('engine_configuration.spatial_function_id_seq');

    INSERT INTO engine_configuration.spatial_function
    (id, "name", "version", sql_definition, active, created_at, parameters)
    VALUES
    (sf_id_1, 'rural_property', 1, 'SELECT geometry_bases.rural_property($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_2, 'headquarter', 1, 'SELECT geometry_bases.headquarter($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_3, 'native_vegetation', 1, 'SELECT geometry_bases.native_vegetation($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_4, 'consolidated_area', 1, 'SELECT geometry_bases.consolidated_area($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_5, 'rivers_up_to_10m', 1, 'SELECT geometry_bases.rivers_up_to_10m($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_6, 'rivers_wider_than_10m', 1, 'SELECT geometry_bases.rivers_wider_than_10m($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_7, 'legal_reserve', 1, 'SELECT geometry_bases.legal_reserve($1::jsonb)', true, NOW(), 'geojson'),
    (sf_id_8, 'ppa_up_to_10m', 1, 'SELECT geometry_bases.ppa_up_to_10m($1::jsonb,30)', true, NOW(), 'geojson'),
    (sf_id_9, 'ppa_wider_than_10m', 1, 'SELECT geometry_bases.ppa_wider_than_10m($1::jsonb)', true, NOW(), 'geojson');

    -- WORKFLOW_TASK
    wt_id_1 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_2 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_3 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_4 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_5 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_6 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_7 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_8 := nextval('engine_configuration.workflow_task_id_seq');
    wt_id_9 := nextval('engine_configuration.workflow_task_id_seq');

    INSERT INTO engine_configuration.workflow_task
    (id, workflow_id, spatial_function_id, task_alias, description, created_at, include_in_output)
    VALUES
    (wt_id_1, workflow_id_var, sf_id_1, 'RURAL_PROPERTY', 'RURAL_PROPERTY', NOW(), false),
    (wt_id_2, workflow_id_var, sf_id_2, 'HEADQUARTER', 'HEADQUARTER', NOW(), false),
    (wt_id_3, workflow_id_var, sf_id_3, 'NATIVE_VEGETATION', 'NATIVE_VEGETATION', NOW(), false),
    (wt_id_4, workflow_id_var, sf_id_4, 'CONSOLIDATED_AREA', 'CONSOLIDATED_AREA', NOW(), false),
    (wt_id_5, workflow_id_var, sf_id_5, 'RIVERS_UP_TO_10M', 'RIVERS_UP_TO_10M', NOW(), false),
    (wt_id_6, workflow_id_var, sf_id_6, 'RIVERS_WIDER_THAN_10M', 'RIVERS_WIDER_THAN_10M', NOW(), false),
    (wt_id_7, workflow_id_var, sf_id_7, 'LEGAL_RESERVE', 'LEGAL_RESERVE', NOW(), false),
    (wt_id_8, workflow_id_var, sf_id_8, 'PPA_UP_TO_10M', 'PPA_UP_TO_10M', NOW(), false),
    (wt_id_9, workflow_id_var, sf_id_9, 'PPA_WIDER_THAN_10M', 'PPA_WIDER_THAN_10M', NOW(), true);

    -- TASK_DEPENDENCY
    td_id_1 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_2 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_3 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_4 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_5 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_6 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_7 := nextval('engine_configuration.task_dependency_id_seq');
    td_id_8 := nextval('engine_configuration.task_dependency_id_seq');

    INSERT INTO engine_configuration.task_dependency
    (id, workflow_id, source_task_id, target_task_id, source_output_alias, target_input_parameter, created_at)
    VALUES
    (td_id_1, workflow_id_var, wt_id_1, wt_id_2, 'geojson', 'geojson', NOW()),
    (td_id_2, workflow_id_var, wt_id_2, wt_id_3, 'geojson', 'geojson', NOW()),
    (td_id_3, workflow_id_var, wt_id_3, wt_id_4, 'geojson', 'geojson', NOW()),
    (td_id_4, workflow_id_var, wt_id_4, wt_id_5, 'geojson', 'geojson', NOW()),
    (td_id_5, workflow_id_var, wt_id_5, wt_id_6, 'geojson', 'geojson', NOW()),
    (td_id_6, workflow_id_var, wt_id_6, wt_id_7, 'geojson', 'geojson', NOW()),
    (td_id_7, workflow_id_var, wt_id_7, wt_id_8, 'geojson', 'geojson', NOW()),
    (td_id_8, workflow_id_var, wt_id_8, wt_id_9, 'geojson', 'geojson', NOW());
END $$;
