package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.exception.WorkflowNotFoundException;
import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.model.TaskDependency;
import DPG.geo_calculation_engine.model.Workflow;
import DPG.geo_calculation_engine.model.WorkflowTask;
import DPG.geo_calculation_engine.repository.SpatialFunctionRepository;
import DPG.geo_calculation_engine.repository.TaskDependencyRepository;
import DPG.geo_calculation_engine.repository.WorkflowRepository;
import DPG.geo_calculation_engine.repository.WorkflowTaskRepository;
import DPG.geo_calculation_engine.service.Interface.GeometryCalculationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Unit tests for {@link WorkflowExecutionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceImplTest {

    @Mock
    private WorkflowRepository mockWorkflowRepository;
    @Mock
    private WorkflowTaskRepository mockWorkflowTaskRepository;
    @Mock
    private TaskDependencyRepository mockTaskDependencyRepository;
    @Mock
    private SpatialFunctionRepository mockSpatialFunctionRepository;
    @Mock
    private GeometryCalculationService mockGeometryCalculationService;

    @InjectMocks
    private WorkflowExecutionServiceImpl workflowExecutionService;

    private Workflow sampleWorkflow;
    private SpatialFunction areaFunction;
    private SpatialFunction bufferFunction;
    private WorkflowTask taskArea;
    private WorkflowTask taskBuffer;
    private WorkflowTask taskAreaOfBuffer;
    private TaskDependency bufferToAreaDependency;
    private Map<String, Object> initialParameters;

    @BeforeEach
    void setUp() {
        sampleWorkflow = new Workflow();
        sampleWorkflow.setId(1L);
        sampleWorkflow.setName("Property_Validation_Test");
        sampleWorkflow.setActive(true);
        sampleWorkflow.setCreatedAt(LocalDateTime.now());
        sampleWorkflow.setUpdatedAt(LocalDateTime.now());

        areaFunction = new SpatialFunction();
        areaFunction.setId(10L);
        areaFunction.setName("calculate_polygon_area");
        areaFunction.setVersion(1);
        areaFunction.setSqlDefinition("SELECT public.ST_Area(public.ST_GeomFromText($1))");
        areaFunction.setParameters("wkt");
        areaFunction.setActive(true);

        bufferFunction = new SpatialFunction();
        bufferFunction.setId(11L);
        bufferFunction.setName("generate_geoprocessing_buffer");
        bufferFunction.setVersion(1);
        bufferFunction.setSqlDefinition("SELECT public.ST_AsText(public.ST_Buffer(public.ST_GeomFromText($1), $2, $3))");
        bufferFunction.setParameters("wkt,buffer_size,options");
        bufferFunction.setActive(true);

        taskArea = new WorkflowTask();
        taskArea.setId(1L);
        taskArea.setWorkflowId(sampleWorkflow.getId());
        taskArea.setSpatialFunctionId(areaFunction.getId());
        taskArea.setTaskAlias("Calculate_Original_Area");

        taskBuffer = new WorkflowTask();
        taskBuffer.setId(2L);
        taskBuffer.setWorkflowId(sampleWorkflow.getId());
        taskBuffer.setSpatialFunctionId(bufferFunction.getId());
        taskBuffer.setTaskAlias("Generate_Property_Buffer");

        taskAreaOfBuffer = new WorkflowTask();
        taskAreaOfBuffer.setId(3L);
        taskAreaOfBuffer.setWorkflowId(sampleWorkflow.getId());
        taskAreaOfBuffer.setSpatialFunctionId(areaFunction.getId());
        taskAreaOfBuffer.setTaskAlias("Calculate_Buffered_Area");

        bufferToAreaDependency = new TaskDependency();
        bufferToAreaDependency.setId(1L);
        bufferToAreaDependency.setWorkflowId(sampleWorkflow.getId());
        bufferToAreaDependency.setSourceTaskId(taskBuffer.getId());
        bufferToAreaDependency.setTargetTaskId(taskAreaOfBuffer.getId());
        bufferToAreaDependency.setSourceOutputAlias("result");
        bufferToAreaDependency.setTargetInputParameter("wkt");

        initialParameters = Map.of(
                "wkt", "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))",
                "buffer_size", 5.0,
                "options", "quad_segs=8"
        );
    }

    @Test
    void executeWorkflow_whenWorkflowNotFound_shouldReturnWorkflowNotFoundException() {
        String workflowName = "NonExistentWorkflow";
        when(mockWorkflowRepository.findByNameAndActiveTrue(workflowName)).thenReturn(Mono.empty());
        Mono<Map<String, Object>> resultMono = workflowExecutionService.executeWorkflow(workflowName, initialParameters);

        StepVerifier.create(resultMono)
                .expectError(WorkflowNotFoundException.class)
                .verify();

        verify(mockWorkflowRepository).findByNameAndActiveTrue(workflowName);
        verifyNoInteractions(mockWorkflowTaskRepository, mockTaskDependencyRepository, mockGeometryCalculationService);
    }

    @Test
    void executeWorkflow_whenWorkflowHasNoTasks_shouldReturnEmptyMap() {
        when(mockWorkflowRepository.findByNameAndActiveTrue(sampleWorkflow.getName())).thenReturn(Mono.just(sampleWorkflow));
        when(mockWorkflowTaskRepository.findByWorkflowId(sampleWorkflow.getId())).thenReturn(Flux.empty());
        when(mockTaskDependencyRepository.findByWorkflowId(sampleWorkflow.getId())).thenReturn(Flux.empty());

        Mono<Map<String, Object>> resultMono = workflowExecutionService.executeWorkflow(sampleWorkflow.getName(), initialParameters);

        StepVerifier.create(resultMono)
                .expectNext(Collections.emptyMap())
                .verifyComplete();

        verify(mockWorkflowRepository).findByNameAndActiveTrue(sampleWorkflow.getName());
        verify(mockWorkflowTaskRepository).findByWorkflowId(sampleWorkflow.getId());
        verify(mockTaskDependencyRepository).findByWorkflowId(sampleWorkflow.getId());
        verifyNoInteractions(mockGeometryCalculationService);
    }

    @Test
    void executeWorkflow_withParallelAndSequentialTasks_shouldExecuteCorrectly() {
        when(mockWorkflowRepository.findByNameAndActiveTrue(sampleWorkflow.getName())).thenReturn(Mono.just(sampleWorkflow));
        when(mockWorkflowTaskRepository.findByWorkflowId(sampleWorkflow.getId())).thenReturn(Flux.just(taskArea, taskBuffer, taskAreaOfBuffer));
        when(mockTaskDependencyRepository.findByWorkflowId(sampleWorkflow.getId())).thenReturn(Flux.just(bufferToAreaDependency));
        when(mockSpatialFunctionRepository.findById(areaFunction.getId())).thenReturn(Mono.just(areaFunction));
        when(mockSpatialFunctionRepository.findById(bufferFunction.getId())).thenReturn(Mono.just(bufferFunction));

        Map<String, Object> paramsForAreaTask = Map.of("wkt", initialParameters.get("wkt"));
        when(mockGeometryCalculationService.executeSpatialFunction(eq(areaFunction), eq(paramsForAreaTask)))
                .thenReturn(Mono.just(100.0));

        Map<String, Object> paramsForBufferTask = Map.of(
                "wkt", initialParameters.get("wkt"),
                "buffer_size", initialParameters.get("buffer_size"),
                "options", initialParameters.get("options")
        );
        String bufferedWKTResult = "POLYGON((...buffered_wkt...))";
        when(mockGeometryCalculationService.executeSpatialFunction(eq(bufferFunction), eq(paramsForBufferTask)))
                .thenReturn(Mono.just(bufferedWKTResult));

        Map<String, Object> paramsForAreaOfBufferTask = Map.of("wkt", bufferedWKTResult);
        when(mockGeometryCalculationService.executeSpatialFunction(eq(areaFunction), eq(paramsForAreaOfBufferTask)))
                .thenReturn(Mono.just(300.0));


        Mono<Map<String, Object>> resultMono = workflowExecutionService.executeWorkflow(sampleWorkflow.getName(), initialParameters);

        StepVerifier.create(resultMono)
                .expectNextMatches(results -> {
                    boolean areaOk = results.get("Calculate_Original_Area").equals(100.0);
                    boolean bufferOk = results.get("Generate_Property_Buffer").equals(bufferedWKTResult);
                    boolean areaOfBufferOk = results.get("Calculate_Buffered_Area").equals(300.0);
                    return areaOk && bufferOk && areaOfBufferOk && results.size() == 3;
                })
                .verifyComplete();

        verify(mockWorkflowRepository).findByNameAndActiveTrue(sampleWorkflow.getName());
        verify(mockWorkflowTaskRepository).findByWorkflowId(sampleWorkflow.getId());
        verify(mockTaskDependencyRepository).findByWorkflowId(sampleWorkflow.getId());
        verify(mockSpatialFunctionRepository, times(2)).findById(areaFunction.getId());
        verify(mockSpatialFunctionRepository).findById(bufferFunction.getId());
        verify(mockGeometryCalculationService).executeSpatialFunction(eq(areaFunction), eq(paramsForAreaTask));
        verify(mockGeometryCalculationService).executeSpatialFunction(eq(bufferFunction), eq(paramsForBufferTask));
        verify(mockGeometryCalculationService).executeSpatialFunction(eq(areaFunction), eq(paramsForAreaOfBufferTask));
    }
}