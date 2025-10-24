package DPG.geo_calculation_engine.controller;

import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.service.SpatialFunctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for managing Spatial Function definitions.
 * Provides endpoints for creating, retrieving, and deleting spatial function versions.
 */
@Slf4j
@RestController
@RequestMapping("/api/spatial-functions")
@RequiredArgsConstructor
public class SpatialFunctionController {

    private final SpatialFunctionService spatialFunctionService;

    /**
     * Creates a new Spatial Function definition (or a new version of an existing one).
     * Expects a valid SpatialFunction object in the request body.
     * The combination of name and version should be unique.
     *
     * @param spatialFunction The SpatialFunction object from the request body, validated by @Valid.
     * @return A Mono emitting a ResponseEntity containing the created SpatialFunction with HTTP status 200 (OK).
     * Validation errors result in 400 Bad Request (handled by GlobalExceptionHandler).
     * Database errors (like unique constraint violation) result in 500 Internal Server Error.
     */
    @PostMapping
    public Mono<ResponseEntity<SpatialFunction>> createSpatialFunction(@Valid @RequestBody SpatialFunction spatialFunction) {
        log.info("Received request to create spatial function: {} v{}", spatialFunction.getName(), spatialFunction.getVersion());
        return spatialFunctionService.createSpatialFunction(spatialFunction)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a specific Spatial Function definition by its unique ID.
     *
     * @param id The ID of the spatial function version to retrieve (from the path variable).
     * @return A Mono emitting a ResponseEntity containing the found SpatialFunction with HTTP status 200 (OK),
     * or a 404 Not Found if no function exists with the given ID.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SpatialFunction>> getSpatialFunctionById(@PathVariable Long id) {
        log.debug("Received request to get spatial function by ID: {}", id);
        return spatialFunctionService.getSpatialFunctionById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all configured Spatial Function definitions (all versions).
     *
     * @return A Flux emitting all SpatialFunction objects. Returns an empty stream if none exist.
     */
    @GetMapping
    public Flux<SpatialFunction> getAllSpatialFunctions() {
        log.debug("Received request to get all spatial functions");
        return spatialFunctionService.getAllSpatialFunctions();
    }

    /**
     * Retrieves all *active* Spatial Function definitions.
     * Typically, only one version of a function (by name) should be active at a time.
     *
     * @return A Flux emitting active SpatialFunction objects. Returns an empty stream if none are active.
     */
    @GetMapping("/active")
    public Flux<SpatialFunction> getActiveFunctions() {
        log.debug("Received request to get active spatial functions");
        return spatialFunctionService.getActiveFunctions();
    }

    /**
     * Retrieves a specific version of a Spatial Function by its logical name and version number.
     *
     * @param name The logical name of the spatial function (from the path variable).
     * @param version The version number of the spatial function (from the path variable).
     * @return A Mono emitting a ResponseEntity containing the found SpatialFunction with HTTP status 200 (OK),
     * or a 404 Not Found if no function exists with the given name and version.
     */
    @GetMapping("/{name}/{version}")
    public Mono<ResponseEntity<SpatialFunction>> getFunctionByNameAndVersion(
            @PathVariable String name,
            @PathVariable int version) {
        log.debug("Received request to get spatial function by name '{}' and version {}", name, version);
        return spatialFunctionService.getFunctionByNameAndVersion(name, version)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a specific Spatial Function definition by its unique ID.
     *
     * @param id The ID of the spatial function version to delete (from the path variable).
     * @return A Mono emitting a ResponseEntity with HTTP status 204 (No Content) upon successful deletion.
     * Note: May return 204 even if the ID didn't exist, depending on the service implementation.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSpatialFunction(@PathVariable Long id) {
        log.warn("Received request to delete spatial function with ID: {}", id);
        return spatialFunctionService.deleteSpatialFunction(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}