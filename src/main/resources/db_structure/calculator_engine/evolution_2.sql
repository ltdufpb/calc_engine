-- Table to define Workflows
CREATE TABLE engine_configuration.workflow (
    id BIGSERIAL PRIMARY KEY,                   -- Unique identifier for the workflow
    name VARCHAR(255) NOT NULL UNIQUE,        -- Unique and descriptive name for the workflow (e.g., 'PropertyAreaValidation')
    description TEXT,                           -- Optional description of the workflow's purpose
    active BOOLEAN DEFAULT TRUE,                -- Indicates if the workflow can be executed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- To track updates to the workflow definition
);

-- Add comments to the columns of the 'workflow' table
COMMENT ON TABLE engine_configuration.workflow IS 'Defines workflows composed of multiple spatial functions.';
COMMENT ON COLUMN engine_configuration.workflow.name IS 'Unique name to identify the workflow.';
COMMENT ON COLUMN engine_configuration.workflow.active IS 'Flag to enable or disable the execution of this workflow.';

-- Grant SELECT and UPDATE on the specific workflow table
GRANT REFERENCES, UPDATE, SELECT, INSERT ON TABLE engine_configuration.workflow TO calculator_engine;

-- Table to define Tasks (nodes) within each Workflow
CREATE TABLE engine_configuration.workflow_task (
    id BIGSERIAL PRIMARY KEY,                   -- Unique identifier for the task within the workflow
    workflow_id BIGINT NOT NULL,                -- Foreign key referencing the workflow table
    spatial_function_id BIGINT NOT NULL,        -- Foreign key referencing the spatial_function table (the function to be executed)
    task_alias VARCHAR(100) NOT NULL,           -- Unique alias for this task WITHIN the workflow (e.g., 'initial_buffer', 'final_area_calculation')
    description TEXT,                           -- Optional description of this task's role in the workflow
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ensures that each task alias is unique within the same workflow
    UNIQUE (workflow_id, task_alias),

    -- Foreign keys
    CONSTRAINT fk_workflow FOREIGN KEY (workflow_id)
        REFERENCES engine_configuration.workflow (id) ON DELETE CASCADE, -- If the workflow is deleted, delete its tasks
    CONSTRAINT fk_spatial_function FOREIGN KEY (spatial_function_id)
        REFERENCES engine_configuration.spatial_function (id) ON DELETE RESTRICT -- Prevents deleting a function if it is used by a task
);

-- Add comments to the columns of the 'workflow_task' table
COMMENT ON TABLE engine_configuration.workflow_task IS 'Defines each step (task/node) within a workflow, associating a spatial function.';
COMMENT ON COLUMN engine_configuration.workflow_task.workflow_id IS 'References the workflow to which this task belongs.';
COMMENT ON COLUMN engine_configuration.workflow_task.spatial_function_id IS 'References the spatial function to be executed by this task.';
COMMENT ON COLUMN engine_configuration.workflow_task.task_alias IS 'Unique alias of the task within the workflow, used to define dependencies.';

-- Grant SELECT and UPDATE on the specific workflow_task table
GRANT REFERENCES, UPDATE, SELECT, INSERT ON TABLE engine_configuration.workflow_task TO calculator_engine;

-- Table to define Dependencies (edges) between Tasks in a Workflow
CREATE TABLE engine_configuration.task_dependency (
    id BIGSERIAL PRIMARY KEY,                   -- Unique identifier for the dependency
    workflow_id BIGINT NOT NULL,                -- Foreign key referencing the workflow table (facilitates queries)
    source_task_id BIGINT NOT NULL,             -- Foreign key to workflow_task (the task that PRODUCES the data - prerequisite)
    target_task_id BIGINT NOT NULL,             -- Foreign key to workflow_task (the task that CONSUMES the data - dependent)
    -- Parameter Mapping: how the output of the 'source_task' becomes input for the 'target_task'
    source_output_alias VARCHAR(100) DEFAULT 'result', -- Name/Alias of the OUTPUT parameter from the source_task (simplified as 'result' by default)
    target_input_parameter VARCHAR(100) NOT NULL,   -- Name of the INPUT parameter in the target_task (must match a name in spatial_function.parameters)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ensures that an input parameter of a task is not mapped more than once in the same workflow
    UNIQUE (workflow_id, target_task_id, target_input_parameter),

    -- Foreign keys
    CONSTRAINT fk_dependency_workflow FOREIGN KEY (workflow_id)
        REFERENCES engine_configuration.workflow (id) ON DELETE CASCADE,
    CONSTRAINT fk_source_task FOREIGN KEY (source_task_id)
        REFERENCES engine_configuration.workflow_task (id) ON DELETE CASCADE, -- If the source task is deleted, delete the dependency
    CONSTRAINT fk_target_task FOREIGN KEY (target_task_id)
        REFERENCES engine_configuration.workflow_task (id) ON DELETE CASCADE -- If the target task is deleted, delete the dependency
);

-- Add comments to the columns of the 'task_dependency' table
COMMENT ON TABLE engine_configuration.task_dependency IS 'Defines dependencies between tasks within a workflow and how data flows (input/output mapping).';
COMMENT ON COLUMN engine_configuration.task_dependency.source_task_id IS 'ID of the task that provides the data (prerequisite).';
COMMENT ON COLUMN engine_configuration.task_dependency.target_task_id IS 'ID of the task that receives the data (dependent).';
COMMENT ON COLUMN engine_configuration.task_dependency.source_output_alias IS 'Name/alias of the output value from the source task to be used (default: "result").';
COMMENT ON COLUMN engine_configuration.task_dependency.target_input_parameter IS 'Name of the input parameter in the target task that will receive the value.';

-- Grant SELECT and UPDATE on the specific task_dependency table
GRANT REFERENCES, UPDATE, SELECT, INSERT ON TABLE engine_configuration.task_dependency TO calculator_engine;

-- Indexes to optimize queries (optional, but recommended)
CREATE INDEX idx_workflow_task_workflow ON engine_configuration.workflow_task(workflow_id);
CREATE INDEX idx_task_dependency_workflow ON engine_configuration.task_dependency(workflow_id);
CREATE INDEX idx_task_dependency_source ON engine_configuration.task_dependency(source_task_id);
CREATE INDEX idx_task_dependency_target ON engine_configuration.task_dependency(target_task_id);

-- Grant USAGE on the sequences associated with the SERIAL/BIGSERIAL columns
-- This is necessary for inserts to use the auto-generated IDs correctly.
GRANT USAGE ON SEQUENCE engine_configuration.workflow_id_seq TO calculator_engine;
GRANT USAGE ON SEQUENCE engine_configuration.workflow_task_id_seq TO calculator_engine;
GRANT USAGE ON SEQUENCE engine_configuration.task_dependency_id_seq TO calculator_engine;




--##-- Rollback

-- Drop the table with the most dependencies first
--DROP TABLE IF EXISTS engine_configuration.task_dependency;

-- Drop the table that depends on 'workflow'
--DROP TABLE IF EXISTS engine_configuration.workflow_task;

-- Drop the main workflow table
--DROP TABLE IF EXISTS engine_configuration.workflow;