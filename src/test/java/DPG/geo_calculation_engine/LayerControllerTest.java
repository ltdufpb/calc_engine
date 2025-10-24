package DPG.geo_calculation_engine;

import DPG.geo_calculation_engine.controller.LayerController;
import DPG.geo_calculation_engine.model.Layer;
import DPG.geo_calculation_engine.service.LayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;

class LayerControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private LayerService layerService;

    @InjectMocks
    private LayerController layerController;

    private Layer layer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(layerController).build();

        layer = new Layer();
        layer.setId(1L);
        layer.setName("TestLayer");
        layer.setGeometryType("POLYGON");
        layer.setGroupName("TestGroup");
        layer.setAllowOverlap(true);
        layer.setOverlapRestrictions(null);
        layer.setGenerateBuffer(false);
        layer.setBufferSize(0.0);
        layer.setCalculateArea(true);
        layer.setCreatedAt(LocalDateTime.parse("2025-05-27T18:07:00.956275400"));
        layer.setActive(null);
    }

    @Test
    void testCreateLayer() {
        when(layerService.createLayer(any(Layer.class))).thenReturn(Mono.just(layer));

        webTestClient.post().uri("/api/layers")
                .bodyValue(layer)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Layer.class)
                .value(responseLayer -> {
                    assert responseLayer.getId().equals(layer.getId());
                    assert responseLayer.getName().equals(layer.getName());
                });

        verify(layerService, times(1)).createLayer(any(Layer.class));
    }

    @Test
    void testGetLayerById() {
        when(layerService.getLayerById(1L)).thenReturn(Mono.just(layer));

        webTestClient.get().uri("/api/layers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Layer.class)
                .value(responseLayer -> {
                    assert responseLayer.getId().equals(layer.getId());
                    assert responseLayer.getName().equals(layer.getName());
                });

        verify(layerService, times(1)).getLayerById(1L);
    }

    @Test
    void testDeleteLayer() {
        when(layerService.deleteLayer(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/layers/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(layerService, times(1)).deleteLayer(1L);
    }
}