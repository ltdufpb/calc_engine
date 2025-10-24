package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.model.Layer;
import DPG.geo_calculation_engine.repository.LayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer implementation for managing Layer configurations.
 * Handles business logic related to creating, retrieving, and deleting Layer entities,
 * interacting with the {@link LayerRepository}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LayerService {

    private final LayerRepository layerRepository;

    /**
     * Creates and persists a new Layer configuration.
     *
     * @param layer The {@link Layer} object to create (ID should typically be null).
     * @return A {@link Mono} emitting the saved {@link Layer} entity, including its generated ID.
     */
    public Mono<Layer> createLayer(Layer layer) {
        log.info("Creating layer with name: {}", layer.getName());
        return layerRepository.save(layer);
    }

    /**
     * Retrieves a specific Layer configuration by its unique ID.
     *
     * @param id The unique ID of the Layer to retrieve.
     * @return A {@link Mono} emitting the found {@link Layer}, or an empty Mono if no layer
     * with the given ID exists.
     */
    public Mono<Layer> getLayerById(Long id) {
        log.info("Fetching layer with ID: {}", id);
        return layerRepository.findById(id);
    }

    /**
     * Retrieves all Layer configurations currently stored.
     *
     * @return A {@link Flux} emitting all {@link Layer} entities, or an empty Flux if none exist.
     */
    public Flux<Layer> getAllLayers() {
        log.info("Fetching all layers");
        return layerRepository.findAll();
    }

    /**
     * Retrieves all Layer configurations belonging to a specific group name.
     *
     * @param groupName The group name to filter layers by.
     * @return A {@link Flux} emitting all {@link Layer} entities matching the specified group name,
     * or an empty Flux if none are found.
     */
    public Flux<Layer> getLayersByGroupName(String groupName) {
        log.info("Fetching layers with groupName: {}", groupName);
        return layerRepository.findByGroupName(groupName);
    }

    /**
     * Retrieves all Layer configurations where the 'allowOverlap' flag is set to true.
     *
     * @return A {@link Flux} emitting all {@link Layer} entities that allow overlap,
     * or an empty Flux if none are configured that way.
     */
    public Flux<Layer> getLayersThatAllowOverlap() {
        log.info("Fetching layers that allow overlap");
        return layerRepository.findByAllowOverlapTrue();
    }

    /**
     * Deletes a Layer configuration by its unique ID.
     *
     * @param id The unique ID of the Layer to delete.
     * @return A {@link Mono<Void>} that completes when the deletion operation finishes
     * via the repository. It might complete successfully even if the ID didn't exist,
     * depending on the underlying R2DBC driver/database behavior for deleteById.
     * It will emit an error if the deletion fails for other reasons (e.g., database error).
     */
    public Mono<Void> deleteLayer(Long id) {
        log.info("Deleting layer with ID: {}", id);
        return layerRepository.deleteById(id);
    }
}