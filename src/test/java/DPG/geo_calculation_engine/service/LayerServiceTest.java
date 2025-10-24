package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.model.Layer;
import DPG.geo_calculation_engine.repository.LayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LayerService}.
 */
@ExtendWith(MockitoExtension.class)
class LayerServiceTest {

    @Mock
    private LayerRepository layerRepository;

    @InjectMocks
    private LayerService layerService;

    private Layer sampleLayer1;
    private Layer sampleLayer2;

    @BeforeEach
    void setUp() {
        sampleLayer1 = new Layer();
        sampleLayer1.setId(1L);
        sampleLayer1.setName("Protected_Area");
        sampleLayer1.setGeometryType("POLYGON");
        sampleLayer1.setGroupName("Environmental");
        sampleLayer1.setAllowOverlap(false);
        sampleLayer1.setGenerateBuffer(true);
        sampleLayer1.setBufferSize(50.0);
        sampleLayer1.setCalculateArea(true);
        sampleLayer1.setCreatedAt(LocalDateTime.now());
        sampleLayer1.setActive(true);

        sampleLayer2 = new Layer();
        sampleLayer2.setId(2L);
        sampleLayer2.setName("Urban_Area");
        sampleLayer2.setGeometryType("POLYGON");
        sampleLayer2.setGroupName("City_Planning");
        sampleLayer2.setAllowOverlap(true);
        sampleLayer2.setActive(true);
        sampleLayer2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createLayer_shouldSaveAndReturnLayer() {
        Layer layerToSave = new Layer();
        layerToSave.setName("New_Test_Layer");
        Layer savedLayerMock = new Layer();
        savedLayerMock.setId(3L);
        savedLayerMock.setName("New_Test_Layer");

        when(layerRepository.save(any(Layer.class))).thenReturn(Mono.just(savedLayerMock));

        Mono<Layer> resultMono = layerService.createLayer(layerToSave);

        StepVerifier.create(resultMono)
                .expectNextMatches(createdLayer ->
                        createdLayer.getId() != null &&
                                createdLayer.getId().equals(3L) &&
                                createdLayer.getName().equals("New_Test_Layer")
                )
                .verifyComplete();

        verify(layerRepository, times(1)).save(layerToSave);
    }

    @Test
    void getLayerById_whenLayerExists_shouldReturnLayer() {
        when(layerRepository.findById(1L)).thenReturn(Mono.just(sampleLayer1));
        Mono<Layer> resultMono = layerService.getLayerById(1L);
        StepVerifier.create(resultMono)
                .expectNext(sampleLayer1)
                .verifyComplete();
        verify(layerRepository, times(1)).findById(1L);
    }

    @Test
    void getLayerById_whenLayerDoesNotExist_shouldReturnEmpty() {
        when(layerRepository.findById(99L)).thenReturn(Mono.empty());
        Mono<Layer> resultMono = layerService.getLayerById(99L);
        StepVerifier.create(resultMono)
                .verifyComplete();
        verify(layerRepository, times(1)).findById(99L);
    }

    @Test
    void getAllLayers_whenLayersExist_shouldReturnLayers() {
        when(layerRepository.findAll()).thenReturn(Flux.just(sampleLayer1, sampleLayer2));
        Flux<Layer> resultFlux = layerService.getAllLayers();
        StepVerifier.create(resultFlux)
                .expectNext(sampleLayer1)
                .expectNext(sampleLayer2)
                .verifyComplete();
        verify(layerRepository, times(1)).findAll();
    }

    @Test
    void getAllLayers_whenNoLayersExist_shouldReturnEmptyFlux() {
        when(layerRepository.findAll()).thenReturn(Flux.empty());
        Flux<Layer> resultFlux = layerService.getAllLayers();
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
        verify(layerRepository, times(1)).findAll();
    }

    @Test
    void getLayersByGroupName_whenLayersExist_shouldReturnLayers() {
        String groupName = "Environmental";
        when(layerRepository.findByGroupName(groupName)).thenReturn(Flux.just(sampleLayer1));
        Flux<Layer> resultFlux = layerService.getLayersByGroupName(groupName);
        StepVerifier.create(resultFlux)
                .expectNext(sampleLayer1)
                .verifyComplete();
        verify(layerRepository, times(1)).findByGroupName(groupName);
    }

    @Test
    void getLayersByGroupName_whenNoLayersExist_shouldReturnEmptyFlux() {
        String groupName = "NonExistentGroup";
        when(layerRepository.findByGroupName(groupName)).thenReturn(Flux.empty());
        Flux<Layer> resultFlux = layerService.getLayersByGroupName(groupName);
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
        verify(layerRepository, times(1)).findByGroupName(groupName);
    }

    @Test
    void getLayersThatAllowOverlap_whenLayersExist_shouldReturnLayers() {
        when(layerRepository.findByAllowOverlapTrue()).thenReturn(Flux.just(sampleLayer2));
        Flux<Layer> resultFlux = layerService.getLayersThatAllowOverlap();
        StepVerifier.create(resultFlux)
                .expectNext(sampleLayer2)
                .verifyComplete();
        verify(layerRepository, times(1)).findByAllowOverlapTrue();
    }

    @Test
    void getLayersThatAllowOverlap_whenNoLayersExist_shouldReturnEmptyFlux() {
        when(layerRepository.findByAllowOverlapTrue()).thenReturn(Flux.empty());
        Flux<Layer> resultFlux = layerService.getLayersThatAllowOverlap();
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
        verify(layerRepository, times(1)).findByAllowOverlapTrue();
    }

    @Test
    void deleteLayer_shouldCallRepositoryDelete() {
        Long layerIdToDelete = 1L;
        when(layerRepository.deleteById(layerIdToDelete)).thenReturn(Mono.empty());
        Mono<Void> resultMono = layerService.deleteLayer(layerIdToDelete);
        StepVerifier.create(resultMono)
                .verifyComplete();
        verify(layerRepository, times(1)).deleteById(layerIdToDelete);
    }
}