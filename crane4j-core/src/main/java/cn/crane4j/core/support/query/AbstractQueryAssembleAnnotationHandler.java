package cn.crane4j.core.support.query;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AbstractInternalProviderAssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A base implementation of {@link AssembleAnnotationHandler}
 * for supporting assemble annotation which is based on ORM.
 *
 * @param <A> annotation type
 * @param <R> orm repository type
 * @author huangchengxing
 * @see QueryRepository
 * @see QueryDefinition
 * @since 2.9.0
 */
@Slf4j
public abstract class AbstractQueryAssembleAnnotationHandler<A extends Annotation, R>
    extends AbstractInternalProviderAssembleAnnotationHandler<A> {

    protected static final String NAMESPACE_TEMPLATE = "@{}#{}#{}#{}#{}";
    protected final Map<String, QueryRepository<R>> ormRepositoryMap;
    private final MethodInvokerContainerCreator methodInvokerContainerCreator;
    @Setter
    @Nullable
    private RepositoryTargetProvider<R> repositoryTargetProvider;

    /**
     * Create an {@link AbstractQueryAssembleAnnotationHandler} instance.
     *
     * @param annotationType                 annotation type
     * @param annotationFinder               annotation finder
     * @param operationComparator            operation comparator
     * @param globalConfiguration            global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     * @param methodInvokerContainerCreator method invoker container creator
     */
    protected AbstractQueryAssembleAnnotationHandler(
        Class<A> annotationType, AnnotationFinder annotationFinder,
        @NonNull Comparator<KeyTriggerOperation> operationComparator, Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager, MethodInvokerContainerCreator methodInvokerContainerCreator) {
        super(annotationType, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
        this.methodInvokerContainerCreator = methodInvokerContainerCreator;
        this.ormRepositoryMap = new HashMap<>();
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param standardAnnotation standard annotation
     * @param namespace          namespace
     * @return {@link Container} instant
     */
    @NonNull
    @Override
    protected final Container<Object> createContainer(
        StandardAssembleAnnotation<A> standardAnnotation, String namespace) {
        OrmAssembleAnnotation<A> annotation = (OrmAssembleAnnotation<A>) standardAnnotation;
        QueryRepository<R> repository = getRepository(annotation);
        Asserts.isNotNull(repository, "cannot find repository for annotation {}", annotation);
        MethodInvoker invoker = createMethodInvoker(annotation, repository);
        QueryDefinition definition = annotation.getQueryDefinition();
        MethodInvokerContainerCreator.MethodInvokerContainerCreation containerCreation = MethodInvokerContainerCreator.MethodInvokerContainerCreation.builder()
            .target(repository.getTarget())
            .methodInvoker(invoker)
            .mappingType(definition.getMappingType())
            .namespace(namespace)
            .resultType(repository.getEntityType())
            .resultKey(StringUtils.emptyToDefault(definition.getConditionColumn(), repository.getPrimaryKeyProperty()))
            .duplicateStrategy(definition.getDuplicateStrategy())
            .build();
        return methodInvokerContainerCreator.createContainer(containerCreation);
    }

    private MethodInvoker createMethodInvoker(
        OrmAssembleAnnotation<A> annotation, QueryRepository<R> repository) {
        // determine select columns
        QueryDefinition queryDefinition = annotation.getQueryDefinition();
        Set<String> selectColumns = queryDefinition.getSelectColumns()
            .stream()
            .map(repository::resolveToColumn)
            .collect(Collectors.toSet());

        // determine condition column, e.g.: userId -> id, name -> name
        String conditionColumn = StringUtils.emptyToDefault(
            queryDefinition.getConditionColumn(), repository.getPrimaryKeyProperty()
        );
        conditionColumn = repository.resolveToColumn(conditionColumn);

        // create invoker
        return createMethodInvoker(annotation, repository, selectColumns, conditionColumn);
    }

    /**
     * Create a method invoker for query.
     *
     * @param annotation annotation
     * @param repository repository
     * @param selectColumns select columns
     * @param conditionColumn condition column
     * @return method invoker
     * @implNote if the datasource is specified, wrap the invoker to switch datasource before invoking.
     */
    @NonNull
    protected abstract MethodInvoker createMethodInvoker(
        OrmAssembleAnnotation<A> annotation, QueryRepository<R> repository, @Nullable Set<String> selectColumns, String conditionColumn);

    /**
     * Register repository.
     *
     * @param id id
     * @param repository repository
     * @return old repository target
     * @see #getRepository
     */
    public synchronized R registerRepository(String id, @NonNull R repository)  {
        Asserts.isNotNull(repository, "repository cannot be null");
        QueryRepository<R> repo = createRepository(id, repository);
        return Optional.ofNullable(ormRepositoryMap.put(id, repo))
            .map(QueryRepository::getTarget)
            .orElse(null);
    }

    /**
     * Create repository.
     *
     * @param id id
     * @param repository repository
     * @return repo
     */
    @NonNull
    protected abstract QueryRepository<R> createRepository(String id, @NonNull R repository);

    /**
     * Get repository by given annotation.
     *
     * @param standardAnnotation standard annotation
     * @return repository
     * @see #registerRepository
     */
    protected synchronized QueryRepository<R> getRepository(OrmAssembleAnnotation<A> standardAnnotation) {
        QueryDefinition queryDefinition = standardAnnotation.getQueryDefinition();
        String repositoryId = queryDefinition.getRepositoryId();
        QueryRepository<R> result = ormRepositoryMap.get(repositoryId);
        if (Objects.nonNull(result) || Objects.isNull(repositoryTargetProvider)) {
            return result;
        }
        // lazy load if possible
        R target = repositoryTargetProvider.get(queryDefinition);
        if (Objects.nonNull(target)) {
            registerRepository(repositoryId, target);
        }
        return ormRepositoryMap.get(repositoryId);
    }

    /**
     * Determine namespace by given annotation.
     *
     * @param standardAnnotation standard annotation
     * @return namespace
     */
    @Override
    protected String determineNamespace(
        StandardAssembleAnnotation<A> standardAnnotation) {
        OrmAssembleAnnotation<A> annotation = (OrmAssembleAnnotation<A>) standardAnnotation;
        QueryDefinition queryDefinition = annotation.getQueryDefinition();
        String defInfo = StringUtils.format(NAMESPACE_TEMPLATE,
            queryDefinition.getDatasource(), queryDefinition.getMappingType(),
            queryDefinition.getSelectColumns(), queryDefinition.getRepositoryId(),
            queryDefinition.getConditionColumn()
        );
        return StringUtils.md5DigestAsHex(defInfo);
    }

    /**
     * Get {@link StandardAssembleAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAssembleAnnotation} instance
     */
    @Override
    protected abstract OrmAssembleAnnotation<A> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, A annotation);

    /**
     * Orm assemble annotation.
     *
     * @param <A> annotation type
     * @author huangchengxing
     */
    @Getter
    @SuperBuilder
    protected static class OrmAssembleAnnotation<A extends Annotation>
        extends StandardAssembleAnnotationAdapter<A> implements StandardAssembleAnnotation<A> {
        private final QueryDefinition queryDefinition;
    }
}
