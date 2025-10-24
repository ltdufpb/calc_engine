package DPG.geo_calculation_engine.repository;

import DPG.geo_calculation_engine.model.SpatialFunction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository interface for {@link SpatialFunction} entities.
 * Provides reactive CRUD operations (save, findById, findAll, deleteById, etc.)
 * via {@link ReactiveCrudRepository} and custom finder methods for accessing
 * spatial function definitions stored in the 'spatial_function' table
 * within the 'engine_configuration' schema.
 */
public interface SpatialFunctionRepository extends ReactiveCrudRepository<SpatialFunction, Long> {

    /**
     * Finds all defined versions of a spatial function by its logical name.
     *
     * @param name The logical name of the spatial function to find.
     * @return A {@link Flux} emitting all {@link SpatialFunction} entities matching the name,
     * potentially including multiple versions (both active and inactive). Returns an empty Flux if none are found.
     */
    Flux<SpatialFunction> findByName(String name);

    /**
     * Finds a specific version of a spatial function by its logical name and version number.
     * The combination of name and version should be unique.
     *
     * @param name The logical name of the spatial function.
     * @param version The specific version number to find.
     * @return A {@link Mono} emitting the found {@link SpatialFunction} for the given name and version,
     * or an empty Mono if no matching record exists.
     */
    Mono<SpatialFunction> findByNameAndVersion(String name, Integer version);

    /**
     * Finds all spatial function versions across all function names that are currently marked as active (active = true).
     *
     * @return A {@link Flux} emitting all active {@link SpatialFunction} entities,
     * or an empty Flux if no functions are currently active.
     */
    Flux<SpatialFunction> findByActiveTrue();

    /**
     * Finds the single active version (active = true) for a spatial function specified by its logical name.
     * Assumes that for any given function name, only one version should be active at a time.
     *
     * @param name The logical name of the spatial function whose active version is sought.
     * @return A {@link Mono} emitting the active {@link SpatialFunction} for the given name,
     * or an empty Mono if no function with that name exists or if none of its versions are active.
     */
    Mono<SpatialFunction> findByNameAndActiveTrue(String name);
}