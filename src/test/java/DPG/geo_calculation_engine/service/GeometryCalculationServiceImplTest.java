package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.exception.InvalidWKTException;
import DPG.geo_calculation_engine.model.SpatialFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GeometryCalculationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GeometryCalculationServiceImplTest {

    @Mock
    private DatabaseClient mockCalcDatabaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec mockExecuteSpec;

    @Mock
    private FetchSpec<Map<String, Object>> mockFetchSpecMap;

    @InjectMocks
    private GeometryCalculationServiceImpl geometryCalculationService;

    private SpatialFunction sampleAreaFunction;
    private SpatialFunction sampleBufferFunction;
    private SpatialFunction sampleBufferV2Function;
    private SpatialFunction sampleBooleanFunction;

    private final String validWKT_Polygon = "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))";
    private final String validWKT_Point = "POINT(5 5)";

    /**
     * Sets up mock data and configurations before each test.
     */
    @BeforeEach
    void setUp() {
        sampleAreaFunction = new SpatialFunction();
        sampleAreaFunction.setId(1L);
        sampleAreaFunction.setName("calculate_area_test");
        sampleAreaFunction.setVersion(1);
        sampleAreaFunction.setSqlDefinition("SELECT public.ST_Area(public.ST_GeomFromText($1))");
        sampleAreaFunction.setParameters("wkt");
        sampleAreaFunction.setActive(true);

        sampleBufferFunction = new SpatialFunction();
        sampleBufferFunction.setId(2L);
        sampleBufferFunction.setName("generate_buffer_test");
        sampleBufferFunction.setVersion(1);
        sampleBufferFunction.setSqlDefinition("SELECT public.ST_AsText(public.ST_Buffer(public.ST_GeomFromText($1), $2))");
        sampleBufferFunction.setParameters("wkt,buffer_size");
        sampleBufferFunction.setActive(true);

        sampleBufferV2Function = new SpatialFunction();
        sampleBufferV2Function.setId(3L);
        sampleBufferV2Function.setName("generate_buffer_v2_test");
        sampleBufferV2Function.setVersion(2);
        sampleBufferV2Function.setSqlDefinition("SELECT public.ST_AsText(public.ST_Buffer(public.ST_GeomFromText($1), $2, $3))");
        sampleBufferV2Function.setParameters("wkt,buffer_size,options");
        sampleBufferV2Function.setActive(true);

        sampleBooleanFunction = new SpatialFunction();
        sampleBooleanFunction.setId(4L);
        sampleBooleanFunction.setName("is_within_test");
        sampleBooleanFunction.setVersion(1);
        sampleBooleanFunction.setSqlDefinition("SELECT public.ST_Within(public.ST_GeomFromText($1), public.ST_GeomFromText($2))");
        sampleBooleanFunction.setParameters("wkt1,wkt2");
        sampleBooleanFunction.setActive(true);

        lenient().when(mockCalcDatabaseClient.sql(anyString())).thenReturn(mockExecuteSpec);
        lenient().when(mockExecuteSpec.bind(anyString(), any())).thenReturn(mockExecuteSpec);
        lenient().when(mockExecuteSpec.fetch()).thenReturn(mockFetchSpecMap);
        lenient().when(mockFetchSpecMap.one()).thenReturn(Mono.empty());
    }

    /**
     * Tests successful execution of a spatial function with valid parameters.
     * Expects the service to return the correct calculated result.
     */
    @Test
    void executeSpatialFunction_whenValidFunctionAndParameters_shouldReturnResult() {
        Map<String, Object> params = Map.of("wkt", validWKT_Polygon);
        Double expectedArea = 100.0;
        Map<String, Object> mockRowMap = Collections.singletonMap("st_area", expectedArea);
        String expectedSqlWithPlaceholder = sampleAreaFunction.getSqlDefinition().replace("?", "$1");
        when(mockCalcDatabaseClient.sql(eq(expectedSqlWithPlaceholder))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.just(mockRowMap));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectNext(expectedArea)
                .verifyComplete();

        verify(mockCalcDatabaseClient).sql(eq(expectedSqlWithPlaceholder));
        verify(mockExecuteSpec).bind(eq("$1"), eq(validWKT_Polygon));
    }

    /**
     * Tests execution when a required parameter is missing.
     * Expects an {@link IllegalArgumentException} to be thrown before database interaction.
     */
    @Test
    void executeSpatialFunction_whenMissingParameter_shouldReturnIllegalArgumentException() {
        Map<String, Object> params = Collections.emptyMap();
        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Missing required parameter: wkt"))
                .verify();

        verify(mockCalcDatabaseClient, never()).sql(anyString());
    }

    /**
     * Tests execution with an invalid WKT string.
     * Expects an {@link InvalidWKTException} due to WKT validation failure.
     */
    @Test
    void executeSpatialFunction_whenInvalidWKT_shouldReturnInvalidWKTException() {
        Map<String, Object> params = Map.of("wkt", "INVALID_WKT_STRING");
        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectError(InvalidWKTException.class)
                .verify();
        verify(mockCalcDatabaseClient, never()).sql(anyString());
    }

    /**
     * Tests the scenario where a database access error occurs during function execution.
     * Expects a {@link DataAccessException}.
     */
    @Test
    void executeSpatialFunction_whenDatabaseError_shouldReturnError() {
        Map<String, Object> params = Map.of("wkt", validWKT_Polygon);
        String finalSql = sampleAreaFunction.getSqlDefinition().replace("?", "$1");

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.error(new DataAccessException("Simulated DB Error") {}));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectError(DataAccessException.class)
                .verify();
    }

    /**
     * Tests successful execution of a buffer function.
     * Expects a WKT string representing the buffered geometry.
     */
    @Test
    void executeSpatialFunction_whenValidBufferFunctionAndParameters_shouldReturnWKTString() {
        Map<String, Object> params = Map.of("wkt", validWKT_Polygon, "buffer_size", 5.0);
        String expectedBufferedWKT = "POLYGON((...some buffered coordinates...))";
        String finalSql = sampleBufferFunction.getSqlDefinition().replaceFirst("\\?", "\\$1").replaceFirst("\\?", "\\$2");
        Map<String, Object> mockRowMap = Collections.singletonMap("st_astext", expectedBufferedWKT);

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$2"), eq(5.0))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.just(mockRowMap));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleBufferFunction, params);

        StepVerifier.create(resultMono)
                .expectNext(expectedBufferedWKT)
                .verifyComplete();

        verify(mockCalcDatabaseClient).sql(eq(finalSql));
    }

    /**
     * Tests a buffer function with multiple parameters, including options.
     * Expects correct parameter binding and a WKT string result.
     */
    @Test
    void executeSpatialFunction_whenBufferV2_shouldBindAllParametersAndReturnWKT() {
        Map<String, Object> params = Map.of(
                "wkt", validWKT_Polygon,
                "buffer_size", 10.0,
                "options", "quad_segs=4"
        );
        String expectedResultWKT = "POLYGON((...v2 buffered coordinates...))";
        String finalSql = sampleBufferV2Function.getSqlDefinition()
                .replaceFirst("\\?", "\\$1")
                .replaceFirst("\\?", "\\$2")
                .replaceFirst("\\?", "\\$3");
        Map<String, Object> mockRowMap = Collections.singletonMap("st_astext", expectedResultWKT);

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$2"), eq(10.0))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$3"), eq("quad_segs=4"))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.just(mockRowMap));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleBufferV2Function, params);

        StepVerifier.create(resultMono)
                .expectNext(expectedResultWKT)
                .verifyComplete();

        verify(mockCalcDatabaseClient).sql(eq(finalSql));
    }

    /**
     * Tests a spatial function that returns a boolean result.
     * Expects the service to return the correct boolean value.
     */
    @Test
    void executeSpatialFunction_whenBooleanResult_shouldReturnBoolean() {
        Map<String, Object> params = Map.of(
                "wkt1", validWKT_Point,
                "wkt2", validWKT_Polygon
        );
        Boolean expectedResult = true;
        String finalSql = sampleBooleanFunction.getSqlDefinition()
                .replaceFirst("\\?", "\\$1")
                .replaceFirst("\\?", "\\$2");
        Map<String, Object> mockRowMap = Collections.singletonMap("st_within", expectedResult);

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Point))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$2"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.just(mockRowMap));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleBooleanFunction, params);

        StepVerifier.create(resultMono)
                .expectNext(expectedResult)
                .verifyComplete();

        verify(mockCalcDatabaseClient).sql(eq(finalSql));
    }

    /**
     * Tests the scenario where an error occurs during parameter binding.
     * Expects an {@link IllegalStateException}.
     */
    @Test
    void executeSpatialFunction_whenBindError_shouldReturnIllegalStateException() {
        Map<String, Object> params = Map.of("wkt", validWKT_Polygon);
        String finalSql = sampleAreaFunction.getSqlDefinition().replaceFirst("\\?", "\\$1");

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon)))
                .thenThrow(new RuntimeException("Simulated bind failure"));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().contains("Error binding parameter: wkt") &&
                                throwable.getCause() != null &&
                                throwable.getCause().getMessage().equals("Simulated bind failure")
                )
                .verify();

        verify(mockCalcDatabaseClient).sql(eq(finalSql));
        verify(mockExecuteSpec).bind(eq("$1"), eq(validWKT_Polygon));
    }

    /**
     * Tests the scenario where the database fetch operation for a single row returns no result.
     * R2DBC's .one() method is expected to signal a {@link NoSuchElementException} in such cases.
     */
    @Test
    void executeSpatialFunction_whenFetchOneReturnsEmpty_shouldSignalError() {
        Map<String, Object> params = Map.of("wkt", validWKT_Polygon);
        String finalSql = sampleAreaFunction.getSqlDefinition().replaceFirst("\\?", "\\$1");
        String expectedErrorMessage = "Simulating no rows from DB";

        when(mockCalcDatabaseClient.sql(eq(finalSql))).thenReturn(mockExecuteSpec);
        when(mockExecuteSpec.bind(eq("$1"), eq(validWKT_Polygon))).thenReturn(mockExecuteSpec);
        when(mockFetchSpecMap.one()).thenReturn(Mono.error(new NoSuchElementException(expectedErrorMessage)));

        Mono<Object> resultMono = geometryCalculationService.executeSpatialFunction(sampleAreaFunction, params);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof NoSuchElementException &&
                                expectedErrorMessage.equals(throwable.getMessage())
                )
                .verify();

        verify(mockCalcDatabaseClient).sql(eq(finalSql));
        verify(mockExecuteSpec).bind(eq("$1"), eq(validWKT_Polygon));
        verify(mockExecuteSpec).fetch();
        verify(mockFetchSpecMap).one();
    }
}