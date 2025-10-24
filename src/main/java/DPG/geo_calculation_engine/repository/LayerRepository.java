package DPG.geo_calculation_engine.repository;

import DPG.geo_calculation_engine.model.Layer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository interface for {@link Layer} entities.
 * Provides reactive CRUD operations (save, findById, findAll, deleteById, etc.)
 * via {@link ReactiveCrudRepository} and custom finder methods.
 * Interacts with the 'layer' table in the 'engine_configuration' schema.
 */
public interface LayerRepository extends ReactiveCrudRepository<Layer, Long> {

    /**
     * Finds all layers belonging to a specific group name.
     *
     * @param groupName The name of the group to search for.
     * @return A {@link Flux} emitting all {@link Layer} entities matching the group name,
     * or an empty Flux if none are found.
     */
    Flux<Layer> findByGroupName(String groupName);

    /**
     * Finds all layers where the 'allowOverlap' flag is set to true.
     *
     * @return A {@link Flux} emitting all {@link Layer} entities that allow overlapping,
     * or an empty Flux if none are found.
     */
    Flux<Layer> findByAllowOverlapTrue();

    /**
     * Finds a single layer by its unique name.
     * Assumes the 'name' column has a unique constraint or behaviorally unique.
     *
     * @param name The unique name of the layer to find.
     * @return A {@link Mono} emitting the found {@link Layer}, or an empty Mono if no layer
     * with the given name exists.
     */
    Mono<Layer> findByName(String name);

    /**
     * Finds a single layer by its unique name, but only if it is currently active (active = true).
     * Assumes the 'name' column is unique.
     *
     * @param name The unique name of the layer to find.
     * @return A {@link Mono} emitting the found active {@link Layer}, or an empty Mono if no layer
     * with the given name exists or if the found layer is not active.
     */
    Mono<Layer> findByNameAndActiveTrue(String name);
}