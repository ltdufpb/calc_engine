package DPG.geo_calculation_engine.service;

import DPG.geo_calculation_engine.exception.InvalidWKTException;
import DPG.geo_calculation_engine.model.SpatialFunction;
import DPG.geo_calculation_engine.service.Interface.GeometryCalculationService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for executing single spatial functions.
 * This service handles the direct execution of SQL-defined spatial functions,
 * including parameter processing, WKT validation, and database interaction.
 */
@Slf4j
@Service
public class GeometryCalculationServiceImpl implements GeometryCalculationService {

    private final DatabaseClient calcDatabaseClient;
    private final ObjectMapper objectMapper;
    private final WKTWriter wktWriter;
    private final WKTReader wktReader;
    private final GeoJsonReader geoJsonReader;

    /**
     * Constructs a new {@code GeometryCalculationServiceImpl}.
     *
     * @param calcDatabaseClient The {@link DatabaseClient} for the calculations database.
     */
    public GeometryCalculationServiceImpl(
            @Qualifier("calculationsDatabaseClient") DatabaseClient calcDatabaseClient
    ) {
        this.calcDatabaseClient = calcDatabaseClient;
        this.objectMapper = new ObjectMapper();
        this.wktWriter = new WKTWriter();
        this.wktReader = new WKTReader();
        this.geoJsonReader = new GeoJsonReader();
    }

    /**
     * {@inheritDoc}
     * This implementation processes parameters, validates WKT, prepares and executes
     * the SQL definition from the {@link SpatialFunction} against the calculation database.
     * It expects geometric inputs to be in WKT format or convertible from GeoJSON Map.
     */
    @Override
    public Mono<Object> executeSpatialFunction(SpatialFunction spatialFunction, Map<String, Object> originalFunctionParameters) {
        final String functionName = spatialFunction.getName();
        final int functionVersion = spatialFunction.getVersion();
        final String sqlDefinition = spatialFunction.getSqlDefinition();
        log.info("Executing spatial function '{}' v{}", functionName, functionVersion);

        Map<String, Object> processedParameters = new HashMap<>(
                originalFunctionParameters != null ? originalFunctionParameters : Collections.emptyMap()
        );

        String[] paramNamesFromFuncDef = spatialFunction.getParameters() != null && !spatialFunction.getParameters().isBlank() ?
                spatialFunction.getParameters().split(",") : new String[0];

        for (int i = 0; i < paramNamesFromFuncDef.length; i++) {
            paramNamesFromFuncDef[i] = paramNamesFromFuncDef[i].trim();
        }

        // Processo de validação e conversão dos parâmetros
        for (String pNameFromFuncDef : paramNamesFromFuncDef) {
            Object paramValue = processedParameters.get(pNameFromFuncDef);

            if (paramValue == null) {
                log.error("Missing required parameter '{}' for function '{}' v{}",
                        pNameFromFuncDef, functionName, functionVersion);
                return Mono.error(new IllegalArgumentException(String.format(
                        "Missing required parameter: %s for function %s v%s",
                        pNameFromFuncDef, functionName, functionVersion
                )));
            }

            // Special processing for GeoJSON parameters
            if (paramValue instanceof Map && pNameFromFuncDef.toLowerCase().contains("geojson")) {
                try {
                    log.debug("Converting GeoJSON Map to string for parameter '{}'", pNameFromFuncDef);
                    String geoJsonString = objectMapper.writeValueAsString(paramValue);
                    processedParameters.put(pNameFromFuncDef, geoJsonString);
                    paramValue = geoJsonString;
                } catch (JsonProcessingException e) {
                    String errorMsg = String.format("Error converting GeoJSON Map to string for parameter '%s' in function '%s' v%d: %s",
                            pNameFromFuncDef, functionName, functionVersion, e.getMessage());
                    log.error(errorMsg, e);
                    return Mono.error(new IllegalArgumentException(errorMsg, e));
                }
            }

            if (paramValue instanceof Map && pNameFromFuncDef.toLowerCase().contains("wkt")) {
                try {
                    Map<?,?> geoJsonMap = (Map<?,?>) paramValue;
                    log.warn("Parameter '{}' for function '{}' was expected to be WKT but received a Map. Attempting GeoJSON Map to WKT conversion.", pNameFromFuncDef, functionName);
                    String geoJsonString = objectMapper.writeValueAsString(geoJsonMap);
                    Geometry geometry = geoJsonReader.read(geoJsonString);
                    if (geometry == null) {
                        throw new InvalidWKTException("Could not parse GeoJSON map for parameter: " + pNameFromFuncDef);
                    }
                    String wktForDb = wktWriter.write(geometry);
                    processedParameters.put(pNameFromFuncDef, wktForDb);
                    paramValue = wktForDb;
                } catch (JsonProcessingException | org.locationtech.jts.io.ParseException | InvalidWKTException e) {
                    String errorMsg = String.format("Invalid GeoJSON (when WKT was expected) for parameter '%s' in function '%s' v%d: %s",
                            pNameFromFuncDef, functionName, functionVersion, e.getMessage());
                    log.error(errorMsg, e);
                    return Mono.error(new InvalidWKTException(errorMsg, e));
                }
            }

            if (pNameFromFuncDef.toLowerCase().contains("wkt") && paramValue instanceof String wktString) {
                try {
                    validateWKTWithJTS(wktString);
                } catch (InvalidWKTException e) {
                    log.error("Invalid WKT for parameter '{}' in function '{}' v{}: {}",
                            pNameFromFuncDef, functionName, functionVersion, e.getMessage());
                    return Mono.error(e);
                }
            }
        }

        // Preparação do SQL com placeholders
        int placeholderIndex = 1;
        String finalSql = sqlDefinition;
        for (int i = 0; i < paramNamesFromFuncDef.length; i++) {
            finalSql = finalSql.replaceFirst("\\?", "\\$" + placeholderIndex++);
        }
        log.debug("Prepared SQL for function '{}': {}", functionName, finalSql);

        DatabaseClient.GenericExecuteSpec executeSpec = calcDatabaseClient.sql(finalSql);

        // Binding dos parâmetros com validação de null
        for (int i = 0; i < paramNamesFromFuncDef.length; i++) {
            String pName = paramNamesFromFuncDef[i];
            Object pValue = processedParameters.get(pName);
            
            // Validação adicional para evitar null pointer
            if (pValue == null) {
                log.error("Parameter '{}' became null during processing for function '{}' v{}",
                        pName, functionName, functionVersion);
                return Mono.error(new IllegalStateException(String.format(
                        "Parameter '%s' became null during processing for function '%s' v%d",
                        pName, functionName, functionVersion
                )));
            }
            
            log.debug("Binding param ${}: {} = (type: {})",
                    (i + 1), pName, pValue.getClass().getSimpleName());
            try {
                executeSpec = executeSpec.bind("$" + (i + 1), pValue);
            } catch (Exception e) {
                log.error("Error binding parameter '{}' (value: '{}') for function '{}' v{}: {}",
                        pName, pValue, functionName, functionVersion, e.getMessage(), e);
                return Mono.error(new IllegalStateException("Error binding parameter: " + pName, e));
            }
        }

        log.debug("Executing database query for function '{}'", functionName);
        String sqlForLog = finalSql;

        return executeSpec.fetch()
                .one()
                .map(row -> row.values().iterator().next())
                .doOnSuccess(rawResult -> {
                    String resultPreview = "null";
                    String resultType = "null";
                    if (rawResult != null) {
                        resultType = rawResult.getClass().getSimpleName();
                        String rawResultStr = rawResult.toString();
                        resultPreview = rawResultStr.substring(0, Math.min(rawResultStr.length(), 100));
                    }
                    log.info("Function '{}' v{} executed successfully. Raw result type: {}, Preview: [{}].",
                            functionName, functionVersion, resultType, resultPreview);
                })
                .doOnError(DataAccessException.class, e ->
                        log.error("DB execution error for function '{}' v{}: SQL='{}' - Error: {}",
                                functionName, functionVersion, sqlForLog, e.getMessage(), e))
                .doOnError(e -> !(e instanceof DataAccessException), e ->
                        log.error("Unexpected error during database execution for function '{}' v{}",
                                functionName, functionVersion, e));
    }

    /**
     * Validates a Well-Known Text (WKT) string using JTS.
     *
     * @param wkt The WKT string to validate.
     * @throws InvalidWKTException if the WKT is null, blank, cannot be parsed,
     * or if the resulting JTS geometry is null.
     * Optionally, it can log warnings for topologically invalid geometries.
     */
    private void validateWKTWithJTS(String wkt) throws InvalidWKTException {
        if (wkt == null || wkt.isBlank()) {
            log.warn("validateWKTWithJTS called with null or blank WKT string.");
            throw new InvalidWKTException("WKT string cannot be null or blank for validation.");
        }
        Geometry geometry;
        try {
            String wktPreview = wkt.substring(0, Math.min(wkt.length(), 150));
            log.debug("Attempting to parse WKT with JTS (first 150 chars): [{}]", wktPreview);
            geometry = this.wktReader.read(wkt);

            if (geometry == null) {
                log.warn("JTS WKTReader returned null for input string (potentially not WKT): {}", wktPreview);
                throw new InvalidWKTException("Input string could not be parsed as valid WKT by JTS WKTReader (returned null geometry). Input was: " + wkt.substring(0, Math.min(wkt.length(), 200)));
            }

            log.debug("JTS parsed WKT successfully for validation. Geometry type: {}", geometry.getGeometryType());

            if (!geometry.isValid()) {
                log.warn("WKT geometry is topologically invalid according to JTS isValid(): {}", wkt.substring(0, Math.min(wkt.length(), 200)));
            }
        } catch (ParseException e) {
            String errorMsg = String.format("Invalid WKT format: %s. Input was: %s",
                    e.getMessage(), wkt.substring(0, Math.min(wkt.length(), 200)));
            log.warn(errorMsg, e);
            throw new InvalidWKTException(errorMsg, e);
        }
    }
}