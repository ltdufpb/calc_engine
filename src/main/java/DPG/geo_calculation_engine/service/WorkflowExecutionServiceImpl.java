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
import DPG.geo_calculation_engine.service.Interface.WorkflowExecutionService;
import DPG.geo_calculation_engine.service.Interface.GeometryCalculationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the WorkflowExecutionService interface.
 * Handles the execution of workflows by coordinating task dependencies
 * and parameter mapping for spatial functions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final SpatialFunctionRepository spatialFunctionRepository;
    private final GeometryCalculationService geometryCalculationService;

    @Override
    public Mono<Map<String, Object>> executeWorkflow(
            String workflowName,
            Map<String, Object> parameters) {

        log.info("Attempting to execute workflow: {} with parameters.", workflowName);

        final Map<String, Object> safeParameters = Optional.ofNullable(parameters).orElse(Collections.emptyMap());

        return workflowRepository.findByNameAndActiveTrue(workflowName)
                .switchIfEmpty(Mono.error(new WorkflowNotFoundException("Active workflow not found: " + workflowName)))
                .flatMap(workflow -> loadAndExecuteDag(workflow, safeParameters));
    }

    /**
     * Loads and executes the workflow's Directed Acyclic Graph (DAG).
     * Handles task dependencies and parameter mapping.
     */
    private Mono<Map<String, Object>> loadAndExecuteDag(
            Workflow workflow,
            Map<String, Object> parameters) {

        Mono<List<WorkflowTask>> tasksMono = workflowTaskRepository.findByWorkflowId(workflow.getId()).collectList();
        Mono<List<TaskDependency>> dependenciesMono = taskDependencyRepository.findByWorkflowId(workflow.getId()).collectList();

        return Mono.zip(tasksMono, dependenciesMono)
                .flatMap(tuple -> {
                    List<WorkflowTask> tasks = tuple.getT1();
                    List<TaskDependency> dependencies = tuple.getT2();
                    log.info("Loaded {} tasks and {} dependencies for workflow '{}'", tasks.size(), dependencies.size(), workflow.getName());

                    if (tasks.isEmpty()) {
                        log.warn("Workflow '{}' has no tasks defined. Returning empty result.", workflow.getName());
                        return Mono.just(Collections.<String, Object>emptyMap());
                    }

                    Map<Long, Mono<Object>> taskResultsCache = new ConcurrentHashMap<>();
                    Map<Long, WorkflowTask> taskMap = tasks.stream().collect(Collectors.toMap(WorkflowTask::getId, task -> task));
                    Map<Long, List<TaskDependency>> dependenciesMap = dependencies.stream().collect(Collectors.groupingBy(TaskDependency::getTargetTaskId));

                    // Execute all tasks (including those not included in output) to maintain dependencies
                    List<Mono<Tuple2<String, Object>>> allTaskResultMonos = tasks.stream()
                            .map(task -> getTaskResultMono(task.getId(), taskMap, dependenciesMap, parameters, taskResultsCache)
                                    .map(result -> Tuples.of(task.getTaskAlias(), result))
                                    .doOnError(e -> log.error("Error preparing result Mono for task alias '{}' (Task ID {}): {}",
                                            task.getTaskAlias(), task.getId(), e.getMessage(), e))
                            )
                            .collect(Collectors.toList());

                    // Filter tasks to include only those marked for output
                    List<WorkflowTask> tasksToIncludeInOutput = tasks.stream()
                            .filter(task -> Boolean.TRUE.equals(task.getIncludeInOutput()))
                            .collect(Collectors.toList());

                    log.info("Executing {} total tasks but will return only {} tasks in response for workflow '{}'", 
                            tasks.size(), tasksToIncludeInOutput.size(), workflow.getName());

                    log.info("Attempting final aggregation for {} tasks in workflow '{}'...", allTaskResultMonos.size(), workflow.getName());

                    return Mono.zip(allTaskResultMonos, resultsArray -> {
                        Map<String, Object> allResults = new ConcurrentHashMap<>();
                        
                        // First, collect all results (for internal processing)
                        for (Object resultTuple : resultsArray) {
                            if (resultTuple instanceof Tuple2) {
                                @SuppressWarnings("unchecked")
                                Tuple2<String, Object> typedTuple = (Tuple2<String, Object>) resultTuple;
                                allResults.put(typedTuple.getT1(), typedTuple.getT2());
                            } else {
                                log.warn("Unexpected result type in aggregation array: {}. Original array element: {}",
                                        resultTuple != null ? resultTuple.getClass().getName() : "null", resultTuple);
                            }
                        }
                        
                        // Filter results to include only tasks marked for output
                        Map<String, Object> filteredResults = new ConcurrentHashMap<>();
                        Set<String> outputTaskAliases = tasksToIncludeInOutput.stream()
                                .map(WorkflowTask::getTaskAlias)
                                .collect(Collectors.toSet());
                        
                        for (Map.Entry<String, Object> entry : allResults.entrySet()) {
                            if (outputTaskAliases.contains(entry.getKey())) {
                                filteredResults.put(entry.getKey(), entry.getValue());
                            }
                        }
                        
                        log.info("Workflow '{}' execution completed successfully. Executed {} tasks, returning {} in response.", 
                                workflow.getName(), allResults.size(), filteredResults.size());
                        
                        if (log.isDebugEnabled()) {
                            log.debug("Tasks executed but hidden from output: {}", 
                                    allResults.keySet().stream()
                                            .filter(alias -> !outputTaskAliases.contains(alias))
                                            .collect(Collectors.joining(", ")));
                            log.debug("Tasks included in output: {}", String.join(", ", filteredResults.keySet()));
                        }
                        
                        return filteredResults;
                    });
                });
    }

    private Mono<Object> getTaskResultMono(
            Long taskId,
            Map<Long, WorkflowTask> taskMap,
            Map<Long, List<TaskDependency>> dependenciesMap,
            Map<String, Object> parameters,
            Map<Long, Mono<Object>> taskResultsCache) {

        // Verificar se já existe no cache primeiro
        Mono<Object> cachedResult = taskResultsCache.get(taskId);
        if (cachedResult != null) {
            return cachedResult;
        }

        // Criar o Mono de computação
        Mono<Object> computationMono = computeTaskResult(taskId, taskMap, dependenciesMap, parameters, taskResultsCache);
        
        // Tentar adicionar ao cache de forma thread-safe
        Mono<Object> existingMono = taskResultsCache.putIfAbsent(taskId, computationMono);
        if (existingMono != null) {
            // Outro thread já adicionou, usar o existente
            return existingMono;
        }
        
        return computationMono;
    }

    private Mono<Object> computeTaskResult(
            Long taskId,
            Map<Long, WorkflowTask> taskMap,
            Map<Long, List<TaskDependency>> dependenciesMap,
            Map<String, Object> parameters,
            Map<Long, Mono<Object>> taskResultsCache) {

        log.debug("Computing Task ID: {}", taskId);
        WorkflowTask currentTask = taskMap.get(taskId);
        if (currentTask == null) {
            log.error("Task definition not found in internal map for ID: {}", taskId);
            return Mono.error(new IllegalStateException("Task definition not found for ID: " + taskId));
        }

        List<TaskDependency> prerequisites = dependenciesMap.getOrDefault(taskId, Collections.emptyList());
        log.debug("Task ID: {} ('{}') has {} prerequisites.", taskId, currentTask.getTaskAlias(), prerequisites.size());

        List<Mono<Map.Entry<Long, Object>>> prerequisiteResultMonos;
        if (prerequisites.isEmpty()) {
            prerequisiteResultMonos = Collections.singletonList(Mono.just(createDummyEntry()));
        } else {
            prerequisiteResultMonos = prerequisites.stream()
                    .map(dep -> {
                        log.debug("Task '{}' (ID {}) getting prerequisite Mono for Source Task ID: {}", currentTask.getTaskAlias(), taskId, dep.getSourceTaskId());
                        return getTaskResultMono(dep.getSourceTaskId(), taskMap, dependenciesMap, parameters, taskResultsCache)
                                .map(result -> (Map.Entry<Long,Object>)Map.entry(dep.getSourceTaskId(), result));
                    })
                    .collect(Collectors.toList());
        }

        return Mono.zip(prerequisiteResultMonos, prerequisiteResultsArray -> {
                    Map<Long, Object> prerequisiteResults = new ConcurrentHashMap<>();
                    if (!prerequisites.isEmpty()) {
                        for (Object entryObj : prerequisiteResultsArray) {
                            if (entryObj instanceof Map.Entry<?, ?> entry) {
                                if (!entry.getKey().equals(-1L)) {
                                    prerequisiteResults.put((Long) entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                    return prerequisiteResults;
                })
                .flatMap(prerequisiteResults ->
                        spatialFunctionRepository.findById(currentTask.getSpatialFunctionId())
                                .switchIfEmpty(Mono.error(new IllegalStateException("Spatial function definition not found for ID: " + currentTask.getSpatialFunctionId())))
                                .flatMap(spatialFunction -> {
                                    Map<String, Object> functionParameters = buildFunctionParameters(
                                            currentTask, spatialFunction, prerequisites, prerequisiteResults, parameters
                                    );
                                    return geometryCalculationService.executeSpatialFunction(spatialFunction, functionParameters);
                                })
                )
                .doOnError(e -> log.error("Error during computation for Task '{}' (ID {}): {}",
                        currentTask.getTaskAlias(), taskId, e.getMessage(), e))
                .cache();
    }

    private static Map.Entry<Long, Object> createDummyEntry() {
        return Map.entry(-1L, new Object());
    }

    private Map<String, Object> buildFunctionParameters(
            WorkflowTask currentTask,
            SpatialFunction spatialFunction,
            List<TaskDependency> prerequisitesForCurrentTask,
            Map<Long, Object> prerequisiteResults,
            Map<String, Object> parameters) {

        Map<String, Object> functionParams = new ConcurrentHashMap<>();
        String[] expectedParams = spatialFunction.getParameters() != null && !spatialFunction.getParameters().isBlank() ?
                spatialFunction.getParameters().split(",") : new String[0];

        log.debug("Building parameters for task '{}' (Function {}). Expected by function: {}",
                currentTask.getTaskAlias(), spatialFunction.getName(), List.of(expectedParams));

        for (TaskDependency dependency : prerequisitesForCurrentTask) {
            String targetParamName = dependency.getTargetInputParameter().trim();
            Object sourceTaskResult = prerequisiteResults.get(dependency.getSourceTaskId());

            if (sourceTaskResult != null) {
                log.debug("Mapping dependency for task '{}': Input '{}' <- Task ID {} (output alias '{}')",
                        currentTask.getTaskAlias(), targetParamName, dependency.getSourceTaskId(), dependency.getSourceOutputAlias());
                functionParams.put(targetParamName, sourceTaskResult);
            } else {
                log.warn("Result for prerequisite Source Task ID {} not found in prerequisiteResults for task '{}', parameter '{}'. Dependency might be unmet or task errored.",
                        dependency.getSourceTaskId(), currentTask.getTaskAlias(), targetParamName);
            }
        }

        for (String expectedParam : expectedParams) {
            String trimmedParam = expectedParam.trim();
            if (!functionParams.containsKey(trimmedParam) && parameters.containsKey(trimmedParam)) {
                log.debug("Mapping initial parameter for task '{}': Input '{}'", currentTask.getTaskAlias(), trimmedParam);
                functionParams.put(trimmedParam, parameters.get(trimmedParam));
            }
        }

        for (String expectedParam : expectedParams) {
            String trimmedParam = expectedParam.trim();
            if (!functionParams.containsKey(trimmedParam)) {
                if (trimmedParam.equalsIgnoreCase("wkt")) {
                    String errorMsg = String.format("Parameter mapping failed for task '%s' (Function '%s'): Expected parameter '%s' (as primary WKT) but main geometry was not provided or was invalid.",
                            currentTask.getTaskAlias(), spatialFunction.getName(), trimmedParam);
                    log.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                } else {
                    String errorMsg = String.format("Parameter mapping failed for task '%s' (Function '%s'): Expected parameter '%s' was not found from dependencies or initial parameters.",
                            currentTask.getTaskAlias(), spatialFunction.getName(), trimmedParam);
                    log.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                }
            }
        }

        log.debug("Final parameters built for task '{}' (Function: {}): Parameters sent: {}",
                currentTask.getTaskAlias(), spatialFunction.getName(), functionParams.keySet());
        return functionParams;
    }
}