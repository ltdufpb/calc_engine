package DPG.geo_calculation_engine.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a Workflow definition entity, mapping to the 'workflow' table
 * in the 'engine_configuration' schema. Defines a named sequence or graph
 * of tasks (spatial functions) to be executed.
 */
@Data
@Table("engine_configuration.workflow")
public class Workflow {

    /**
     * Unique identifier for the workflow definition record.
     * Maps to the 'id' primary key column (BIGSERIAL).
     */
    @Id
    private Long id;

    /**
     * Unique, human-readable name identifying this workflow.
     * (e.g., "Property_Area_Buffer_Validation"). Used to trigger execution.
     * Cannot be blank. Maps to the 'name' column (which has a UNIQUE constraint).
     */
    @NotBlank(message = "Workflow name cannot be blank")
    @Column("name")
    private String name;

    /**
     * Optional description providing more details about the purpose or steps
     * involved in this workflow.
     * Maps to the 'description' TEXT column.
     */
    @Column("description")
    private String description;

    /**
     * Flag indicating if this workflow definition is currently active and available
     * for execution. Defaults to true.
     * Maps to the 'active' column.
     */
    @Column("active")
    private Boolean active = true;

    /**
     * Timestamp indicating when this workflow definition record was created.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'created_at' column.
     * @see org.springframework.data.annotation.CreatedDate
     * @see DPG.geo_calculation_engine.config.R2DBCConfig relevant auditing configuration
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating when this workflow definition record was last updated.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'updated_at' column.
     * @see org.springframework.data.annotation.LastModifiedDate
     * @see DPG.geo_calculation_engine.config.R2DBCConfig relevant auditing configuration
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}