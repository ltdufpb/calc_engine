package DPG.geo_calculation_engine;

import DPG.geo_calculation_engine.controller.WorkflowExecutionController;
import DPG.geo_calculation_engine.model.dto.WorkflowExecutionRequest;
import DPG.geo_calculation_engine.service.Interface.WorkflowExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes unitários para {@link WorkflowExecutionController}.
 */
@ExtendWith(MockitoExtension.class)
class WorkflowExecutionControllerTest {

    @Mock
    private WorkflowExecutionService mockWorkflowExecutionService;

    @InjectMocks
    private WorkflowExecutionController workflowExecutionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(workflowExecutionController)
                .build();
    }

    @Test
    void executeWorkflow_withValidParameters_shouldReturnSuccess() throws Exception {
        // Arrange
        String workflowName = "Buffer";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("wkt", "POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))");
        parameters.put("buffer_size", 0.05);
        parameters.put("options", "quad_segs=8");

        WorkflowExecutionRequest request = new WorkflowExecutionRequest();
        request.setParameters(parameters);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("result", "POLYGON ((...))");

        when(mockWorkflowExecutionService.executeWorkflow(eq(workflowName), eq(parameters)))
                .thenReturn(Mono.just(expectedResult));

        // Act & Assert
        mockMvc.perform(post("/api/workflows/{workflowName}/execute", workflowName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    void executeWorkflow_withNullParameters_shouldReturnBadRequest() throws Exception {
        // Arrange
        String workflowName = "Buffer";
        WorkflowExecutionRequest request = new WorkflowExecutionRequest();
        request.setParameters(null);

        // Act & Assert
        mockMvc.perform(post("/api/workflows/{workflowName}/execute", workflowName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void executeWorkflow_withEmptyParameters_shouldReturnSuccess() throws Exception {
        // Arrange
        String workflowName = "Buffer";
        Map<String, Object> parameters = new HashMap<>();

        WorkflowExecutionRequest request = new WorkflowExecutionRequest();
        request.setParameters(parameters);

        Map<String, Object> expectedResult = new HashMap<>();

        when(mockWorkflowExecutionService.executeWorkflow(eq(workflowName), eq(parameters)))
                .thenReturn(Mono.just(expectedResult));

        // Act & Assert
        mockMvc.perform(post("/api/workflows/{workflowName}/execute", workflowName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
} 