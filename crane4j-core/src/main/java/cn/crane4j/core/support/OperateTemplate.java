package cn.crane4j.core.support;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Helper classes used to simplify filling operations.
 *
 * @author huangchengxing
 * @see Crane4jTemplate
 */
@RequiredArgsConstructor
public class OperateTemplate {

    /**
     * Default configuration parser will be used when no parser is specified.
     */
    protected final BeanOperationParser defaultParser;

    /**
     * Default operation actuator, which will be used when no actuator is specified.
     */
    private final BeanOperationExecutor defaultExecutor;

    /**
     * Type parser. <br/>
     * If the type of object to be operated is not specified, obtain the type through the parser.
     */
    private final TypeResolver typeResolver;

    /**
     * If the operation belongs to any group in {@code groups}, execute the operation.
     *
     * @param target target
     * @param groups groups
     * @see Grouped#anyMatch
     */
    public void executeIfMatchAnyGroups(Object target, String... groups) {
        execute(target, Grouped.anyMatch(groups));
    }

    /**
     * If the operation does not belong to any group in {@code groups}, execute the operation.
     *
     * @param target target
     * @param groups groups
     * @see Grouped#anyMatch 
     */
    public void executeIfNoneMatchAnyGroups(Object target, String... groups) {
        execute(target, Grouped.anyMatch(groups).negate());
    }

    /**
     * If the operation belongs to all groups in {@code groups}, execute the operation.
     *
     * @param target target
     * @param groups groups
     * @see Grouped#allMatch 
     */
    public void executeIfMatchAllGroups(Object target, String... groups) {
        execute(target, Grouped.allMatch(groups));
    }

    /**
     * Execute the fill operation.
     *
     * @param target target
     */
    public void execute(Object target) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, defaultExecutor, Grouped.alwaysMatch()
        );
    }

    /**
     * Execute the fill operation.
     *
     * @param target target
     * @param beanOperations bean operations
     */
    public void execute(Object target, BeanOperations beanOperations) {
        if (Objects.nonNull(target) && Objects.nonNull(beanOperations)) {
            defaultExecutor.execute(
                CollectionUtils.adaptObjectToCollection(target),
                beanOperations, Grouped.alwaysMatch()
            );
        }
    }

    /**
     * Execute the fill operation.
     *
     * @param target target
     * @param beanOperations bean operations
     */
    public void execute(Object target, BeanOperations beanOperations, Predicate<? super KeyTriggerOperation> filter) {
        if (Objects.nonNull(target) && Objects.nonNull(beanOperations)) {
            defaultExecutor.execute(
                CollectionUtils.adaptObjectToCollection(target),
                beanOperations, filter
            );
        }
    }

    /**
     * Execute the fill operation.
     *
     * @param target target
     * @param filter filter
     */
    public void execute(Object target, Predicate<? super KeyTriggerOperation> filter) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, defaultExecutor, filter
        );
    }

    /**
     * Execute the fill operation.
     *
     * @param target target
     * @param executor executor
     * @param filter filter
     */
    public void execute(
        Object target, BeanOperationExecutor executor, Predicate<? super KeyTriggerOperation> filter) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, executor, filter
        );
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param targetType target type
     */
    public void execute(Collection<?> targets, Class<?> targetType) {
        execute(targets, targetType, defaultParser, defaultExecutor, Grouped.alwaysMatch());
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param parser parser
     * @param executor executor
     * @param filter filter
     */
    public void execute(
        Collection<?> targets, Class<?> targetType, BeanOperationParser parser,
        BeanOperationExecutor executor, Predicate<? super KeyTriggerOperation> filter) {
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }
        Objects.requireNonNull(targetType, "targetType must not null");
        Objects.requireNonNull(parser, "parser must not null");
        Objects.requireNonNull(executor, "executor must not null");
        BeanOperations beanOperations = parser.parse(targetType);
        executor.execute(targets, beanOperations, filter);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> resolveType(Object target) {
        Class<?> type = typeResolver.resolve(target);
        return Objects.isNull(type) ? null : (Class<T>)type;
    }
}
