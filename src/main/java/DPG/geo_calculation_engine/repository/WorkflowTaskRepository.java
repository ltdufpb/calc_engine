package DPG.geo_calculation_engine.repository;

import DPG.geo_calculation_engine.model.WorkflowTask;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository interface for {@link WorkflowTask} entities.
 * Provides reactive CRUD operations (save, findById, findAll, deleteById, etc.)
 * via {@link ReactiveCrudRepository} and custom finder methods for accessing
 * individual task definitions within a workflow, stored in the 'workflow_task' table
 * within the 'engine_configuration' schema.
 */
public interface WorkflowTaskRepository extends ReactiveCrudRepository<WorkflowTask, Long> {

    /**
     * Finds all tasks associated with a specific workflow ID.
     * Retrieves all nodes belonging to a particular workflow graph.
     *
     * @param workflowId The unique ID of the workflow whose tasks are to be retrieved.
     * @return A {@link Flux} emitting all {@link WorkflowTask} entities for the specified workflow,
     * or an empty Flux if the workflow has no tasks or does not exist.
     */
    Flux<WorkflowTask> findByWorkflowId(Long workflowId);

    /**
     * Finds a single specific task within a given workflow by its unique alias for that workflow.
     * Assumes the combination of workflowId and taskAlias is unique.
     *
     * @param workflowId The unique ID of the workflow to search within.
     * @param taskAlias  The unique alias of the task within the specified workflow.
     * @return A {@link Mono} emitting the found {@link WorkflowTask}, or an empty Mono
     * if no task with the given alias exists within the specified workflow.
     */
    Mono<WorkflowTask> findByWorkflowIdAndTaskAlias(Long workflowId, String taskAlias);
}