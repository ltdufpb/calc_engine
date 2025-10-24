package DPG.geo_calculation_engine.repository;

import DPG.geo_calculation_engine.model.TaskDependency;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository interface for {@link TaskDependency} entities.
 * Provides reactive CRUD operations and custom finder methods for accessing
 * task dependency definitions stored in the 'task_dependency' table
 * within the 'engine_configuration' schema. These dependencies define the
 * execution order and data flow within a workflow.
 */
public interface TaskDependencyRepository extends ReactiveCrudRepository<TaskDependency, Long> {

    /**
     * Finds all task dependencies associated with a specific workflow ID.
     * This retrieves all the dependency links (edges) defined for a given workflow graph.
     *
     * @param workflowId The unique ID of the workflow whose dependencies are to be retrieved.
     * @return A {@link Flux} emitting all {@link TaskDependency} entities for the specified workflow,
     * or an empty Flux if the workflow has no defined dependencies.
     */
    Flux<TaskDependency> findByWorkflowId(Long workflowId);

    /**
     * Finds all task dependencies where the specified task ID is the target (dependent) task.
     * This is useful for finding all the direct prerequisites (source tasks) that a given task depends on.
     *
     * @param targetTaskId The unique ID of the target (dependent) {@link DPG.geo_calculation_engine.model.WorkflowTask}.
     * @return A {@link Flux} emitting all {@link TaskDependency} entities pointing to the specified target task,
     * or an empty Flux if the task has no prerequisites.
     */
    Flux<TaskDependency> findByTargetTaskId(Long targetTaskId);

    /**
     * Finds all task dependencies where the specified task ID is the source (prerequisite) task.
     * This is useful for finding all direct tasks that depend on the output of the given task.
     *
     * @param sourceTaskId The unique ID of the source (prerequisite) {@link DPG.geo_calculation_engine.model.WorkflowTask}.
     * @return A {@link Flux} emitting all {@link TaskDependency} entities originating from the specified source task,
     * or an empty Flux if no other tasks depend on this one.
     */
    Flux<TaskDependency> findBySourceTaskId(Long sourceTaskId);
}