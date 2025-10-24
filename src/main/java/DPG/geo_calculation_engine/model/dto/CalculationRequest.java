package DPG.geo_calculation_engine.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * Data Transfer Object (DTO) representing the request body for executing
 * a spatial calculation or potentially initiating a workflow that starts with a calculation.
 */
@Data
public class CalculationRequest {

    /**
     * Identifier for the target layer (e.g., its unique name).
     * This provides context for the operation, potentially influencing parameter selection
     * or validation rules, although the core execution relies on the 'operation' and 'parameters'.
     * Cannot be blank.
     */
    @NotBlank(message = "Layer code cannot be blank")
    private String layerCode;

    /**
     * The logical name of the spatial function (operation) to be executed.
     * This name should correspond to an active 'name' in the 'spatial_function' table.
     * Cannot be blank.
     */
    @NotBlank(message = "Operation cannot be blank")
    private String operation;

    /**
     * A map containing the actual input parameters required by the specified 'operation'.
     * Keys represent the parameter names expected by the spatial function
     * (as defined in the 'parameters' column of the 'spatial_function' table, e.g., "wkt", "buffer_size").
     * Values represent the corresponding parameter values.
     * The map itself cannot be null, but it can be empty if the operation requires no parameters
     * beyond context implicitly derived from the 'layerCode' (though currently parameters like 'wkt' are expected here).
     */
    @NotNull(message = "Parameters map cannot be null")
    private Map<String, Object> parameters;
}