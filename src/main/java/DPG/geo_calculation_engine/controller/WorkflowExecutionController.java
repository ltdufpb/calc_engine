package DPG.geo_calculation_engine.controller;

import DPG.geo_calculation_engine.model.dto.WorkflowExecutionRequest;
import DPG.geo_calculation_engine.service.Interface.WorkflowExecutionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionService workflowExecutionService;
    private final ObjectMapper objectMapper;

    /**
     * Executes a specific workflow with the provided parameters.
     * The parameters must exactly match those defined in the 'parameters' field
     * of the spatial function associated with the workflow.
     *
     * @param workflowName Name of the workflow to execute
     * @param request Request containing the required parameters
     * @return Workflow execution result
     */
    @PostMapping("/{workflowName}/execute")
    public Mono<ResponseEntity<Map<String, Object>>> executeWorkflow(
            @PathVariable String workflowName,
            @Valid @RequestBody WorkflowExecutionRequest request) {

        log.info("Received request to execute workflow: {} with parameter keys: {}",
                workflowName,
                Optional.ofNullable(request.getParameters()).map(Map::keySet).orElse(Collections.emptySet()));

        return workflowExecutionService.executeWorkflow(workflowName, request.getParameters())
                .map(rawResults -> {
                    Map<String, Object> finalResults = new HashMap<>();
                    WKTReader wktReader = new WKTReader();
                    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();

                    for (Map.Entry<String, Object> entry : rawResults.entrySet()) {
                        Object value = entry.getValue();
                        
                        // Try to convert PostgreSQL JSONB result
                        if (value != null && (value.getClass().getName().contains("postgresql") || value.toString().startsWith("{"))) {
                            try {
                                String jsonString;
                                if (value.getClass().getSimpleName().equals("JsonByteArrayInput")) {
                                    // Extract JSON from JsonByteArrayInput by removing the class prefix
                                    String toString = value.toString();
                                    if (toString.startsWith("JsonByteArrayInput{") && toString.endsWith("}")) {
                                        jsonString = toString.substring("JsonByteArrayInput{".length(), toString.length() - 1);
                                        log.debug("Extracted JSON from JsonByteArrayInput: {}", jsonString);
                                    } else {
                                        jsonString = toString;
                                        log.warn("Unexpected JsonByteArrayInput format: {}", toString);
                                    }
                                } else {
                                    jsonString = value.toString();
                                }
                                
                                Map<String, Object> jsonMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                                finalResults.put(entry.getKey(), jsonMap);
                                log.debug("Converted PostgreSQL JSONB result to Map for task '{}'.", entry.getKey());
                                continue;
                            } catch (Exception e) {
                                log.error("Failed to convert PostgreSQL JSONB result for task '{}'. Error: {}", entry.getKey(), e.getMessage(), e);
                                // In case of conversion error, try to return the value as string
                                finalResults.put(entry.getKey(), value.toString());
                            }
                        }

                        // Process WKT
                        if (value instanceof String) {
                            String sValue = (String) value;
                            boolean looksLikeWKT = sValue.toUpperCase().startsWith("POLYGON") ||
                                    sValue.toUpperCase().startsWith("POINT") ||
                                    sValue.toUpperCase().startsWith("LINESTRING") ||
                                    sValue.toUpperCase().startsWith("MULTIPOLYGON") ||
                                    sValue.toUpperCase().startsWith("MULTIPOINT") ||
                                    sValue.toUpperCase().startsWith("MULTILINESTRING") ||
                                    sValue.toUpperCase().startsWith("GEOMETRYCOLLECTION");

                            if (looksLikeWKT) {
                                try {
                                    Geometry geom = wktReader.read(sValue);
                                    String geoJsonString = geoJsonWriter.write(geom);
                                    Map<String, Object> geoJsonObject = objectMapper.readValue(geoJsonString, new TypeReference<Map<String, Object>>() {});
                                    finalResults.put(entry.getKey(), geoJsonObject);
                                    log.debug("Converted WKT output for task '{}' to GeoJSON object for API response.", entry.getKey());
                                } catch (Exception e) {
                                    log.warn("Failed to convert WKT result to GeoJSON object for task output '{}'. Returning raw WKT. Error: {}", entry.getKey(), e.getMessage());
                                    finalResults.put(entry.getKey(), sValue);
                                }
                            } else {
                                finalResults.put(entry.getKey(), sValue);
                            }
                        } else {
                            finalResults.put(entry.getKey(), value);
                        }
                    }
                    log.info("Successfully executed workflow: {}. Returning results (WKT converted to GeoJSON objects) for {} tasks.", workflowName, finalResults.size());
                    return ResponseEntity.ok(finalResults);
                });
    }
}