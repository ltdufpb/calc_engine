package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.repository.SpatialFunctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer implementation for managing Spatial Function definitions.
 * Handles business logic related to creating, retrieving, and deleting SpatialFunction entities,
 * interacting with the {@link SpatialFunctionRepository}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpatialFunctionService {

    private final SpatialFunctionRepository spatialFunctionRepository;

    /**
     * Creates and persists a new Spatial Function definition (or a new version).
     *
     * @param spatialFunction The {@link SpatialFunction} object to create (ID should typically be null).
     * 'name' and 'version' combination should be unique based on DB constraints.
     * @return A {@link Mono} emitting the saved {@link SpatialFunction} entity, including its generated ID.
     * Emits an error if saving fails (e.g., unique constraint violation).
     */
    public Mono<SpatialFunction> createSpatialFunction(SpatialFunction spatialFunction) {
        log.info("Creating spatial function: {} v{}", spatialFunction.getName(), spatialFunction.getVersion());
        return spatialFunctionRepository.save(spatialFunction);
    }

    /**
     * Retrieves a specific Spatial Function definition by its unique ID.
     *
     * @param id The unique ID of the Spatial Function version to retrieve.
     * @return A {@link Mono} emitting the found {@link SpatialFunction}, or an empty Mono
     * if no function version exists with the given ID.
     */
    public Mono<SpatialFunction> getSpatialFunctionById(Long id) {
        log.info("Fetching spatial function with ID: {}", id);
        return spatialFunctionRepository.findById(id);
    }

    /**
     * Retrieves all stored Spatial Function definitions, including all versions of all functions.
     *
     * @return A {@link Flux} emitting all {@link SpatialFunction} entities, or an empty Flux if none exist.
     */
    public Flux<SpatialFunction> getAllSpatialFunctions() {
        log.info("Fetching all spatial functions (all versions)");
        return spatialFunctionRepository.findAll();
    }

    /**
     * Retrieves all Spatial Function versions that are currently marked as active (active = true).
     *
     * @return A {@link Flux} emitting all active {@link SpatialFunction} entities,
     * or an empty Flux if no functions are currently active.
     */
    public Flux<SpatialFunction> getActiveFunctions() {
        log.info("Fetching active spatial functions");
        return spatialFunctionRepository.findByActiveTrue();
    }

    /**
     * Retrieves a specific version of a Spatial Function by its logical name and version number.
     *
     * @param name The logical name of the spatial function.
     * @param version The specific version number to find.
     * @return A {@link Mono} emitting the found {@link SpatialFunction} for the given name and version,
     * or an empty Mono if no matching record exists.
     */
    public Mono<SpatialFunction> getFunctionByNameAndVersion(String name, int version) {
        log.info("Fetching spatial function by name: {} and version: {}", name, version);
        return spatialFunctionRepository.findByNameAndVersion(name, version);
    }

    /**
     * Deletes a specific Spatial Function definition by its unique ID.
     *
     * @param id The unique ID of the Spatial Function version to delete.
     * @return A {@link Mono<Void>} that completes when the deletion operation finishes.
     * May complete successfully even if the ID didn't exist. Emits an error if deletion fails.
     */
    public Mono<Void> deleteSpatialFunction(Long id) {
        log.info("Deleting spatial function with ID: {}", id);
        return spatialFunctionRepository.deleteById(id);
    }

}