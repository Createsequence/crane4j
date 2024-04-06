package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.EmptyContainer;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ObjectUtils;
import cn.crane4j.core.util.TimerUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>This class serves as the top-level template class
 * and defines the key steps required by most {@link AssembleOperationHandler}:<br />
 * Object processing phase:
 * <ul>
 *     <li>
 *         {@link #collectToEntities}: Expands the target objects to be operated on from the {@link AssembleExecution}
 *         and wraps them as {@link Target} objects, which will be used for subsequent processing;
 *     </li>
 *     <li>
 *         {@link #introspectForEntities}: If the {@link Container} for the current operation is not specified,
 *         directly use the target objects as the data source for field mapping;
 *     </li>
 * </ul>
 * If the {@link Container} for the current operation is specified, it enters the data source preparation phase:
 * <ul>
 *     <li>
 *         {@link #getSourcesFromContainer}: Obtains the required data sources based on the objects to be processed;
 *     </li>
 *     <li>
 *         {@link #getTheAssociatedSource}: Retrieves the associated data source object
 *         corresponding to the key value of the object to be processed from the data sources;
 *     </li>
 * </ul>
 * Finally, if the object has an associated data source object,
 * the {@link #completeMapping} method is called to perform property mapping between them.
 *
 * <p>The implementation logic of this template class is based on
 * the encapsulation of {@link Target}, which may introduce unnecessary performance overhead.
 *
 * @author huangchengxing
 */
@Setter
@Slf4j
public abstract class AbstractAssembleOperationHandler implements AssembleOperationHandler {

    /**
     * whether ignore null key.
     */
    protected boolean ignoreNullKey = false;

    /**
     * Perform assembly operation.
     *
     * @param container  container
     * @param executions operations to be performed
     */
    @Override
    public void process(Container<?> container, Collection<AssembleExecution> executions) {
        TimerUtil.getExecutionTime(
            log.isDebugEnabled(),
            time -> log.debug("operation of container [{}] completed in {} ms", container.getNamespace(), time),
            () -> doProcess(container, executions)
        );
    }

    private void doProcess(Container<?> container, Collection<AssembleExecution> executions) {
        Collection<Target> targets = collectToEntities(executions);
        if (container instanceof EmptyContainer || Objects.isNull(container)) {
            introspectForEntities(targets);
            return;
        }
        Map<Object, Object> sources = getSourcesFromContainer(container, targets);
        if (CollectionUtils.isEmpty(sources)) {
            return;
        }
        for (Target target : targets) {
            Object source = getTheAssociatedSource(target, sources);
            if (ObjectUtils.isNotEmpty(source)) {
                completeMapping(source, target);
            }
        }
    }

    /**
     * Split the {@link AssembleExecution} into pending objects and wrap it as {@link Target}.
     *
     * @param executions executions
     * @return {@link Target}
     */
    private Collection<Target> collectToEntities(Collection<AssembleExecution> executions) {
        List<Target> targets = new ArrayList<>();
        for (AssembleExecution execution : executions) {
            AssembleOperation operation = execution.getOperation();
            KeyResolver resolver = determineKeyResolver(operation);
            execution.getTargets().stream()
                .map(t -> createTarget(execution, t, resolver.resolve(t, operation)))
                .filter(Objects::nonNull)
                .filter(t -> !ignoreNullKey || Objects.nonNull(t.getKey()))
                .forEach(targets::add);
        }
        return targets;
    }

    /**
     * When the container is {@link EmptyContainer}, introspect the object to be processed.
     *
     * @param targets targets
     */
    protected void introspectForEntities(Collection<Target> targets) {
        for (Target target : targets) {
            completeMapping(target.getOrigin(), target);
        }
    }

    /**
     * Obtain the corresponding data source object from the data source container based on the entity's key value.
     *
     * @param container container
     * @param targets targets
     * @return source objects
     */
    @SuppressWarnings("unchecked")
    protected Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<Target> targets) {
        Set<Object> keys = targets.stream()
            .map(Target::getKey)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return (Map<Object, Object>)((Container<Object>)container).get(keys);
    }

    /**
     * Get the data source object associated with the target object.
     *
     * @param target target
     * @param sources sources
     * @return data source object associated with the target object
     */
    protected Object getTheAssociatedSource(Target target, Map<Object, Object> sources) {
        return sources.get(target.getKey());
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    protected abstract void completeMapping(Object source, Target target);

    /**
     * Create a {@link Target} instance.
     *
     * @param execution execution
     * @param origin origin
     * @param keyValue keyValue
     * @return {@link Target}
     */
    @Nullable
    protected Target createTarget(AssembleExecution execution, Object origin, Object keyValue) {
        return new Target(execution, origin, keyValue);
    }

    /**
     * Target object to be processed.
     */
    @Getter
    @AllArgsConstructor
    protected static class Target {

        /**
         * execution
         */
        private final AssembleExecution execution;

        /**
         * objects to be processed
         */
        protected Object origin;

        /**
         * value of key property
         */
        private Object key;
    }
}
