-- Evolution 3: Add output configuration to workflow tasks
-- Allows configuring which task results should be included in the final API response

-- Add column to control which tasks should be included in output
ALTER TABLE engine_configuration.workflow_task
ADD COLUMN include_in_output BOOLEAN DEFAULT TRUE;

-- Add comment explaining the new column
COMMENT ON COLUMN engine_configuration.workflow_task.include_in_output IS 'Controls whether this task result should be included in the final API response. All tasks are executed regardless of this setting to maintain dependencies.';

-- Create index for performance when filtering output results
CREATE INDEX idx_workflow_task_include_output ON engine_configuration.workflow_task(workflow_id, include_in_output);

-- Grant necessary permissions
GRANT SELECT ON engine_configuration.workflow_task TO calculator_engine;
