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
 * Represents a dependency relationship between two tasks within the same workflow,
 * mapping to the 'task_dependency' table in the 'engine_configuration' schema.
 * Defines the directed edge in the workflow's Directed Acyclic Graph (DAG) and
 * specifies how data flows from the source task's output to the target task's input.
 */
@Data
@Table("engine_configuration.task_dependency")
public class TaskDependency {

    /**
     * Unique identifier for the task dependency record.
     * Maps to the 'id' primary key column (BIGSERIAL).
     */
    @Id
    private Long id;

    /**
     * Identifier of the Workflow to which this dependency belongs.
     * Foreign key referencing the 'workflow' table. Cannot be null.
     * Maps to the 'workflow_id' column.
     */
    @NotNull(message = "Workflow ID cannot be null")
    @Column("workflow_id")
    private Long workflowId;

    /**
     * Identifier of the source task (the prerequisite task).
     * This is the task whose output is required by the target task.
     * Foreign key referencing the 'workflow_task' table. Cannot be null.
     * Maps to the 'source_task_id' column.
     */
    @NotNull(message = "Source Task ID cannot be null")
    @Column("source_task_id")
    private Long sourceTaskId;

    /**
     * Identifier of the target task (the dependent task).
     * This is the task that requires an output from the source task as one of its inputs.
     * Foreign key referencing the 'workflow_task' table. Cannot be null.
     * Maps to the 'target_task_id' column.
     */
    @NotNull(message = "Target Task ID cannot be null")
    @Column("target_task_id")
    private Long targetTaskId;

    /**
     * The alias or name identifying the specific output value from the source task
     * that should be used as input for the target task.
     * Defaults to "result", assuming the source task produces a single, primary output.
     * This field allows for future extension where tasks might produce multiple named outputs.
     * Maps to the 'source_output_alias' column (which also defaults to 'result' in the DB schema).
     */
    @Column("source_output_alias")
    private String sourceOutputAlias = "result";

    /**
     * The name of the input parameter required by the *target* task's associated spatial function
     * (as defined in spatial_function.parameters) that will receive the output value
     * specified by 'source_output_alias' from the source task.
     * Cannot be blank.
     * Maps to the 'target_input_parameter' column.
     */
    @NotBlank(message = "Target input parameter cannot be blank")
    @Column("target_input_parameter")
    private String targetInputParameter;

    /**
     * Timestamp indicating when this task dependency record was created.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'created_at' column.
     * @see org.springframework.data.annotation.CreatedDate
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}