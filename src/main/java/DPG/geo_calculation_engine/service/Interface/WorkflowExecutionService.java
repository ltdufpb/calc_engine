package DPG.geo_calculation_engine.service.Interface;

import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Service responsible for executing defined workflows.
 */
public interface WorkflowExecutionService {

  /**
   * Executes a workflow identified by its name.
   *
   * @param workflowName The unique name of the workflow to execute.
   * @param parameters A map containing all parameters required by the workflow's spatial functions.
   *                  Keys must match exactly the parameter names defined in the spatial functions.
   * @return A Mono emitting a map containing the results of the workflow execution.
   *         The keys of the map will be the task aliases, and the values their respective results.
   *         Returns Mono.error if the workflow is not found, not active, or if any task fails.
   */
  Mono<Map<String, Object>> executeWorkflow(
          String workflowName,
          Map<String, Object> parameters
  );
}