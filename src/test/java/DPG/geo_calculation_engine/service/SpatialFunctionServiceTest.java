package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.repository.SpatialFunctionRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpatialFunctionService}.
 */
@ExtendWith(MockitoExtension.class)
class SpatialFunctionServiceTest {

    @Mock
    private SpatialFunctionRepository spatialFunctionRepository;

    @InjectMocks
    private SpatialFunctionService spatialFunctionService;

    private SpatialFunction sampleFunc1V1;
    private SpatialFunction sampleFunc1V2Active;
    private SpatialFunction sampleFunc2V1;

    @BeforeEach
    void setUp() {
        sampleFunc1V1 = new SpatialFunction();
        sampleFunc1V1.setId(1L);
        sampleFunc1V1.setName("calculate_area");
        sampleFunc1V1.setVersion(1);
        sampleFunc1V1.setSqlDefinition("SELECT ST_Area($1)");
        sampleFunc1V1.setParameters("wkt");
        sampleFunc1V1.setActive(false);
        sampleFunc1V1.setCreatedAt(LocalDateTime.now());

        sampleFunc1V2Active = new SpatialFunction();
        sampleFunc1V2Active.setId(2L);
        sampleFunc1V2Active.setName("calculate_area");
        sampleFunc1V2Active.setVersion(2);
        sampleFunc1V2Active.setSqlDefinition("SELECT ST_Area(ST_GeomFromText($1))");
        sampleFunc1V2Active.setParameters("wkt");
        sampleFunc1V2Active.setActive(true);
        sampleFunc1V2Active.setCreatedAt(LocalDateTime.now());

        sampleFunc2V1 = new SpatialFunction();
        sampleFunc2V1.setId(3L);
        sampleFunc2V1.setName("calculate_buffer");
        sampleFunc2V1.setVersion(1);
        sampleFunc2V1.setSqlDefinition("SELECT ST_Buffer(ST_GeomFromText($1), $2)");
        sampleFunc2V1.setParameters("wkt,distance");
        sampleFunc2V1.setActive(true);
        sampleFunc2V1.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createSpatialFunction_shouldSaveAndReturnFunction() {
        SpatialFunction funcToSave = new SpatialFunction();
        funcToSave.setName("new_function");
        funcToSave.setVersion(1);
        SpatialFunction savedFuncMock = new SpatialFunction();
        savedFuncMock.setId(4L);
        savedFuncMock.setName("new_function");
        savedFuncMock.setVersion(1);

        when(spatialFunctionRepository.save(any(SpatialFunction.class))).thenReturn(Mono.just(savedFuncMock));

        Mono<SpatialFunction> resultMono = spatialFunctionService.createSpatialFunction(funcToSave);

        StepVerifier.create(resultMono)
                .expectNextMatches(createdFunc ->
                        createdFunc.getId() != null &&
                                createdFunc.getName().equals("new_function") &&
                                createdFunc.getVersion().equals(1)
                )
                .verifyComplete();

        verify(spatialFunctionRepository, times(1)).save(funcToSave);
    }

    @Test
    void getSpatialFunctionById_whenFunctionExists_shouldReturnFunction() {
        when(spatialFunctionRepository.findById(1L)).thenReturn(Mono.just(sampleFunc1V1));
        Mono<SpatialFunction> resultMono = spatialFunctionService.getSpatialFunctionById(1L);
        StepVerifier.create(resultMono)
                .expectNext(sampleFunc1V1)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findById(1L);
    }

    @Test
    void getSpatialFunctionById_whenFunctionDoesNotExist_shouldReturnEmpty() {
        when(spatialFunctionRepository.findById(anyLong())).thenReturn(Mono.empty());
        Mono<SpatialFunction> resultMono = spatialFunctionService.getSpatialFunctionById(99L);
        StepVerifier.create(resultMono)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findById(99L);
    }

    @Test
    void getAllSpatialFunctions_whenFunctionsExist_shouldReturnFunctions() {
        when(spatialFunctionRepository.findAll()).thenReturn(Flux.just(sampleFunc1V1, sampleFunc1V2Active, sampleFunc2V1));
        Flux<SpatialFunction> resultFlux = spatialFunctionService.getAllSpatialFunctions();
        StepVerifier.create(resultFlux)
                .expectNextCount(3)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findAll();
    }

    @Test
    void getAllSpatialFunctions_whenNoFunctionsExist_shouldReturnEmptyFlux() {
        when(spatialFunctionRepository.findAll()).thenReturn(Flux.empty());
        Flux<SpatialFunction> resultFlux = spatialFunctionService.getAllSpatialFunctions();
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findAll();
    }

    @Test
    void getActiveFunctions_whenActiveFunctionsExist_shouldReturnFunctions() {
        when(spatialFunctionRepository.findByActiveTrue()).thenReturn(Flux.just(sampleFunc1V2Active, sampleFunc2V1));
        Flux<SpatialFunction> resultFlux = spatialFunctionService.getActiveFunctions();
        StepVerifier.create(resultFlux)
                .expectNext(sampleFunc1V2Active)
                .expectNext(sampleFunc2V1)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getActiveFunctions_whenNoActiveFunctionsExist_shouldReturnEmptyFlux() {
        when(spatialFunctionRepository.findByActiveTrue()).thenReturn(Flux.empty());
        Flux<SpatialFunction> resultFlux = spatialFunctionService.getActiveFunctions();
        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getFunctionByNameAndVersion_whenExists_shouldReturnFunction() {
        String name = "calculate_area";
        Integer version = 2;
        when(spatialFunctionRepository.findByNameAndVersion(name, version)).thenReturn(Mono.just(sampleFunc1V2Active));

        Mono<SpatialFunction> resultMono = spatialFunctionService.getFunctionByNameAndVersion(name, version);

        StepVerifier.create(resultMono)
                .expectNext(sampleFunc1V2Active)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findByNameAndVersion(name, version);
    }

    @Test
    void getFunctionByNameAndVersion_whenDoesNotExist_shouldReturnEmpty() {
        String name = "non_existent_function";
        Integer version = 1;
        when(spatialFunctionRepository.findByNameAndVersion(name, version)).thenReturn(Mono.empty());

        Mono<SpatialFunction> resultMono = spatialFunctionService.getFunctionByNameAndVersion(name, version);

        StepVerifier.create(resultMono)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).findByNameAndVersion(name, version);
    }

    @Test
    void deleteSpatialFunction_shouldCallRepositoryDelete() {
        Long functionIdToDelete = 1L;
        when(spatialFunctionRepository.deleteById(functionIdToDelete)).thenReturn(Mono.empty());

        Mono<Void> resultMono = spatialFunctionService.deleteSpatialFunction(functionIdToDelete);

        StepVerifier.create(resultMono)
                .verifyComplete();
        verify(spatialFunctionRepository, times(1)).deleteById(functionIdToDelete);
    }
}