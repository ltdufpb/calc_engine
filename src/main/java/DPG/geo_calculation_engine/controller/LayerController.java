package DPG.geo_calculation_engine.controller;


import DPG.geo_calculation_engine.service.LayerService;
import DPG.geo_calculation_engine.model.Layer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * REST Controller for managing Layer configurations.
 * Provides endpoints for creating, retrieving, and deleting layer definitions.
 */
@Slf4j
@RestController
@RequestMapping("/api/layers")
@RequiredArgsConstructor
public class LayerController {

    private final LayerService layerService;

    /**
     * Creates a new Layer configuration.
     * Expects a valid Layer object in the request body.
     *
     * @param layer The Layer object sent in the request body, validated by @Valid.
     * @return A Mono emitting a ResponseEntity containing the created Layer with HTTP status 200 (OK).
     * Validation errors will result in a 400 Bad Request handled by GlobalExceptionHandler.
     */
    @PostMapping
    public Mono<ResponseEntity<Layer>> createLayer(@Valid @RequestBody Layer layer) {
        log.info("Received request to create layer: {}", layer.getName());
        return layerService.createLayer(layer)
                .map(ResponseEntity::ok);

    }

    /**
     * Retrieves a specific Layer configuration by its unique ID.
     *
     * @param id The ID of the layer to retrieve (from the path variable).
     * @return A Mono emitting a ResponseEntity containing the found Layer with HTTP status 200 (OK),
     * or a 404 Not Found if no layer exists with the given ID.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Layer>> getLayerById(@PathVariable Long id) {
        log.debug("Received request to get layer by ID: {}", id);
        return layerService.getLayerById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all configured Layers.
     *
     * @return A Flux emitting all Layer objects. Returns an empty stream if no layers exist.
     */
    @GetMapping
    public Flux<Layer> getAllLayers() {
        log.debug("Received request to get all layers");
        return layerService.getAllLayers();
    }

    /**
     * Retrieves all Layers belonging to a specific group.
     *
     * @param groupName The name of the group to filter by (from the path variable).
     * @return A Flux emitting Layer objects matching the specified group name.
     * Returns an empty stream if no layers match the group.
     */
    @GetMapping("/group/{groupName}")
    public Flux<Layer> getLayersByGroupName(@PathVariable String groupName) {
        log.debug("Received request to get layers by group: {}", groupName);
        return layerService.getLayersByGroupName(groupName);
    }

    /**
     * Retrieves all Layers that are configured to allow overlapping.
     * Checks the 'allowOverlap' flag in the layer configuration.
     *
     * @return A Flux emitting Layer objects where 'allowOverlap' is true.
     * Returns an empty stream if no such layers exist.
     */
    @GetMapping("/overlap")
    public Flux<Layer> getLayersThatAllowOverlap() {
        log.debug("Received request to get layers that allow overlap");
        return layerService.getLayersThatAllowOverlap();
    }

    /**
     * Deletes a Layer configuration by its unique ID.
     *
     * @param id The ID of the layer to delete (from the path variable).
     * @return A Mono emitting a ResponseEntity with HTTP status 204 (No Content) upon successful deletion.
     * Note: Based on ReactiveCrudRepository's deleteById, this might return 204 even if the ID didn't exist.
     * Consider modifying the service/repository to return a signal for actual deletion if 404 is desired for non-existent IDs.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteLayer(@PathVariable Long id) {
        log.warn("Received request to delete layer with ID: {}", id);
        return layerService.deleteLayer(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}