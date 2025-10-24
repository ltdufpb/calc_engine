package DPG.geo_calculation_engine.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Represents a Spatial Function definition entity, mapping to the 'spatial_function' table
 * in the 'engine_configuration' schema. Stores the SQL code and metadata for reusable
 * geospatial operations, often leveraging PostGIS functions. Allows for versioning.
 */
@Data
@Table("engine_configuration.spatial_function")
public class SpatialFunction {

    /**
     * Unique identifier for this specific version of the spatial function.
     * Maps to the 'id' primary key column (BIGSERIAL).
     */
    @Id
    private Long id;

    /**
     * The logical name of the spatial function (e.g., "calculate_area", "generate_buffer").
     * Used in combination with 'version' for uniqueness and identification in workflows/operations.
     * Cannot be blank.
     * Maps to the 'name' column.
     */
    @NotBlank(message = "Name cannot be blank")
    @Column("name")
    private String name;

    /**
     * The version number for this implementation of the spatial function.
     * Allows multiple versions of a function with the same logical 'name'.
     * Must be 1 or greater. Cannot be null.
     * Maps to the 'version' column.
     */
    @NotNull(message = "Version cannot be null")
    @Min(value = 1, message = "Version must be at least 1")
    @Column("version")
    private Integer version;

    /**
     * The actual SQL query string that defines the function's execution logic.
     * Typically includes PostGIS functions and uses R2DBC/JDBC placeholders ($1, $2, ...)
     * for input parameters, corresponding positionally to the 'parameters' field.
     * Example: "SELECT public.ST_Area(public.ST_GeomFromText($1))"
     * Cannot be blank.
     * Maps to the 'sql_definition' TEXT column.
     */
    @NotBlank(message = "SQL definition cannot be blank")
    @Column("sql_definition")
    private String sqlDefinition;

    /**
     * Flag indicating whether this specific version of the function is currently
     * active and should be used by default when requested by name.
     * Typically, only one version per 'name' should be active.
     * Maps to the 'active' column.
     */
    @Column("active")
    private Boolean active;

    /**
     * Timestamp indicating when this spatial function version record was created.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'created_at' column.
     * @see org.springframework.data.annotation.CreatedDate
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * A comma-separated, ordered list of the names for the input parameters expected
     * by the 'sqlDefinition'. The order corresponds to the $1, $2, ... placeholders.
     * Example: "wkt", "wkt,buffer_size", "point_wkt,polygon_wkt,options"
     * Can be null or empty if the function requires no input parameters via the execution map.
     * Maps to the 'parameters' TEXT column. Parsing is handled by the application logic.
     */
    @Column("parameters")
    private String parameters;
}