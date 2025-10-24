package DPG.geo_calculation_engine.repository;

import DPG.geo_calculation_engine.model.Workflow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository interface for {@link Workflow} entities.
 * Provides reactive CRUD operations (save, findById, findAll, deleteById, etc.)
 * via {@link ReactiveCrudRepository} and custom finder methods for accessing
 * workflow definitions stored in the 'workflow' table
 * within the 'engine_configuration' schema.
 */
public interface WorkflowRepository extends ReactiveCrudRepository<Workflow, Long> {

    /**
     * Finds a single active workflow definition by its unique name.
     * Assumes the 'name' column has a unique constraint.
     * Only returns a workflow if it exists with the given name AND its 'active' flag is true.
     *
     * @param name The unique name of the workflow to find.
     * @return A {@link Mono} emitting the found active {@link Workflow}, or an empty Mono
     * if no workflow exists with the given name or if the found workflow is inactive.
     */
    Mono<Workflow> findByNameAndActiveTrue(String name);
}