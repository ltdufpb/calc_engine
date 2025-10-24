package DPG.geo_calculation_engine.service.Interface;

import DPG.geo_calculation_engine.model.SpatialFunction;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Service responsible for executing a single spatial function.
 */
public interface GeometryCalculationService {

    /**
     * Executes a specific spatial function with the given parameters.
     * Handles validation (like WKT) and database execution against the calculation database.
     *
     * @param spatialFunction The SpatialFunction object containing definition and parameter info.
     * @param parameters A map of parameter names and their values for the function.
     * @return A Mono emitting the result of the function execution (type depends on the function, returned as Object).
     */
    Mono<Object> executeSpatialFunction(SpatialFunction spatialFunction, Map<String, Object> parameters);

}