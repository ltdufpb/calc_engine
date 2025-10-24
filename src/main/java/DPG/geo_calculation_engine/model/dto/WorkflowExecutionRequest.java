package DPG.geo_calculation_engine.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

/**
 * Represents a workflow execution request.
 * The parameters must exactly match those defined in the 'parameters' field
 * of the spatial function associated with the workflow.
 */
@Data 
public class WorkflowExecutionRequest {

    @NotNull(message = "Parameters cannot be null")
    private Map<String, Object> parameters;
}