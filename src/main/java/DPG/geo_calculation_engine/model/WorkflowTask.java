package DPG.geo_calculation_engine.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a single task (a node) within a defined workflow, mapping to the
 * 'workflow_task' table in the 'engine_configuration' schema.
 * Each task links a step in a specific workflow to the actual spatial function
 * that should be executed for that step.
 */
@Data
@Table("engine_configuration.workflow_task")
public class WorkflowTask {

    /**
     * Unique identifier for this specific task instance within a workflow definition.
     * Maps to the 'id' primary key column (BIGSERIAL).
     */
    @Id
    private Long id;

    /**
     * Identifier of the Workflow to which this task belongs.
     * Foreign key referencing the 'workflow' table. Cannot be null.
     * Maps to the 'workflow_id' column.
     */
    @NotNull(message = "Workflow ID cannot be null")
    @Column("workflow_id")
    private Long workflowId;

    /**
     * Identifier of the Spatial Function that will be executed by this task.
     * Foreign key referencing the 'spatial_function' table. Cannot be null.
     * Maps to the 'spatial_function_id' column.
     */
    @NotNull(message = "Spatial Function ID cannot be null")
    @Column("spatial_function_id")
    private Long spatialFunctionId;

    /**
     * A unique alias or name for this task *within its parent workflow*.
     * Used for identifying the task when defining dependencies and potentially
     * as a key for results in the workflow output map.
     * (e.g., "Calculate_Initial_Area", "Buffer_Step_1").
     * Cannot be blank. Maps to the 'task_alias' column (which has a UNIQUE constraint on workflow_id, task_alias).
     */
    @NotBlank(message = "Task alias cannot be blank")
    @Column("task_alias")
    private String taskAlias;

    /**
     * Optional description providing context about this specific task's role
     * within the workflow.
     * Maps to the 'description' TEXT column.
     */
    @Column("description")
    private String description;

    /**
     * Controls whether this task's result should be included in the final API response.
     * When false, the task is still executed (to maintain dependencies) but its result
     * is filtered out from the final response map.
     * Defaults to true for backward compatibility.
     * Maps to the 'include_in_output' BOOLEAN column.
     */
    @Column("include_in_output")
    private Boolean includeInOutput = true;

    /**
     * Timestamp indicating when this workflow task record was created.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'created_at' column.
     * @see org.springframework.data.annotation.CreatedDate
     * @see DPG.geo_calculation_engine.config.R2DBCConfig relevant auditing configuration
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}