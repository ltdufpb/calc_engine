package DPG.geo_calculation_engine;

import DPG.geo_calculation_engine.controller.SpatialFunctionController;
import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.service.SpatialFunctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;

class SpatialFunctionControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private SpatialFunctionService spatialFunctionService;

    @InjectMocks
    private SpatialFunctionController spatialFunctionController;

    private SpatialFunction spatialFunction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(spatialFunctionController).build();

        spatialFunction = new SpatialFunction();
        spatialFunction.setId(1L);
        spatialFunction.setName("TestFunction");
        spatialFunction.setVersion(1);
        spatialFunction.setSqlDefinition("SELECT ST_Buffer(geom, 10)");
        spatialFunction.setActive(true);
        spatialFunction.setCreatedAt(LocalDateTime.parse("2025-05-27T18:07:01.929175800"));
        spatialFunction.setParameters(null);
    }

    @Test
    void testCreateSpatialFunction() {
        when(spatialFunctionService.createSpatialFunction(any(SpatialFunction.class))).thenReturn(Mono.just(spatialFunction));

        webTestClient.post().uri("/api/spatial-functions")
                .bodyValue(spatialFunction)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SpatialFunction.class)
                .value(responseFunc -> {
                    assert responseFunc.getId().equals(spatialFunction.getId());
                    assert responseFunc.getName().equals(spatialFunction.getName());
                });

        verify(spatialFunctionService, times(1)).createSpatialFunction(any(SpatialFunction.class));
    }

    @Test
    void testGetSpatialFunctionById() {
        when(spatialFunctionService.getSpatialFunctionById(1L)).thenReturn(Mono.just(spatialFunction));

        webTestClient.get().uri("/api/spatial-functions/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SpatialFunction.class)
                .value(responseFunc -> {
                    assert responseFunc.getId().equals(spatialFunction.getId());
                    assert responseFunc.getName().equals(spatialFunction.getName());
                });

        verify(spatialFunctionService, times(1)).getSpatialFunctionById(1L);
    }

    @Test
    void testDeleteSpatialFunction() {
        when(spatialFunctionService.deleteSpatialFunction(1L)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/spatial-functions/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(spatialFunctionService, times(1)).deleteSpatialFunction(1L);
    }
}