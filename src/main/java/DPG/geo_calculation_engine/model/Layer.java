package DPG.geo_calculation_engine.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Represents a Layer configuration entity, mapping to the 'layer' table
 * in the 'engine_configuration' schema. Defines properties and rules
 * associated with a specific type or category of geographic data.
 */
@Data
@Table("engine_configuration.layer")
public class Layer {

    /**
     * Unique identifier for the layer configuration record.
     * Maps to the 'id' primary key column (BIGSERIAL).
     */
    @Id
    private Long id;

    /**
     * Unique descriptive name for the layer (e.g., "Property", "Preservation_Area").
     * Maps to the 'name' column. Should correspond to the 'layerCode' used in requests.
     */
    @Column("name")
    private String name;

    /**
     * The expected or primary geometry type for this layer.
     * (e.g., "POLYGON", "LINESTRING", "POINT").
     * Maps to the 'geometry_type' column.
     */
    @Column("geometry_type")
    private String geometryType;

    /**
     * A logical grouping name for the layer, used for categorization.
     * (e.g., "Environmental", "Land_Registry", "Infrastructure").
     * Maps to the 'group_name' column.
     */
    @Column("group_name")
    private String groupName;

    /**
     * Flag indicating whether geometries in this layer are generally allowed
     * to overlap with geometries in other layers, subject to specific restrictions.
     * Maps to the 'allow_overlap' column.
     */
    @Column("allow_overlap")
    private Boolean allowOverlap;

    /**
     * Stores a JSON array as a String, containing the IDs of other layers
     * that geometries from *this* layer are *not* allowed to overlap with.
     * Example: "[1, 5, 12]"
     * Maps to the 'overlap_restrictions' TEXT column. Parsing is handled by the application logic.
     */
    @Column("overlap_restrictions")
    private String overlapRestrictions;

    /**
     * Flag indicating if a buffer calculation is typically relevant or
     * required for geometries associated with this layer type.
     * Maps to the 'generate_buffer' column.
     */
    @Column("generate_buffer")
    private Boolean generateBuffer;

    /**
     * The default distance or size to be used for buffer operations
     * when 'generateBuffer' is true. The units depend on the Spatial Reference System (SRID)
     * used during the actual calculation. Can be null if not applicable.
     * Maps to the 'buffer_size' DOUBLE PRECISION column.
     */
    @Column("buffer_size")
    private Double bufferSize;

    /**
     * Flag indicating if area calculation is typically relevant or
     * required for geometries associated with this layer type (usually polygons).
     * Maps to the 'calculate_area' column.
     */
    @Column("calculate_area")
    private Boolean calculateArea;

    /**
     * Timestamp indicating when this layer configuration record was created.
     * Can be automatically populated by Spring Data R2DBC if auditing is enabled (@EnableR2dbcAuditing).
     * Maps to the 'created_at' column.
     * @see org.springframework.data.annotation.CreatedDate
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Flag indicating if this layer configuration is currently active and usable
     * in workflows or calculations.
     * Maps to the 'active' column.
     */
    @Column("active")
    private Boolean active;
}