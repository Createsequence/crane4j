package cn.crane4j.extension.jpa;

import cn.crane4j.annotation.AssembleJpa;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.query.AbstractQueryAssembleAnnotationHandler;
import cn.crane4j.core.support.query.QueryDefinition;
import cn.crane4j.core.support.query.QueryRepository;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.repository.Repository;

import javax.persistence.EntityManager;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotation handler for {@link AssembleJpa}.
 *
 * @author huangchengxing
 */
public class AssembleJpaAnnotationHandler extends AbstractQueryAssembleAnnotationHandler<AssembleJpa, Repository<?, ?>> {

    private final EntityManager entityManager;

    /**
     * Create an {@link AbstractQueryAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder               annotation finder
     * @param globalConfiguration            global configuration
     * @param methodInvokerContainerCreator  method invoker container creator
     */
    public AssembleJpaAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        MethodInvokerContainerCreator methodInvokerContainerCreator, EntityManager entityManager) {
        super(AssembleJpa.class, annotationFinder, Crane4jGlobalSorter.comparator(),
            globalConfiguration, globalConfiguration, methodInvokerContainerCreator);
        this.entityManager = entityManager;
    }

    /**
     * Create a method invoker for query.
     *
     * @param annotation      annotation
     * @param repository      repository
     * @param selectColumns   select columns
     * @param conditionColumn condition column
     * @return method invoker
     * @implNote if the datasource is specified, wrap the invoker to switch datasource before invoking.
     */
    @NonNull
    @Override
    protected MethodInvoker createMethodInvoker(
        OrmAssembleAnnotation<AssembleJpa> annotation, QueryRepository<Repository<?, ?>> repository,
        @Nullable Set<String> selectColumns, String conditionColumn) {
        JpaQueryRepository repo = (JpaQueryRepository) repository;
        String select = CollectionUtils.isEmpty(selectColumns) ?
            "*" : String.join(", ", selectColumns);
        String table = repo.getTableName();
        // TODO special handling for QueryByExampleExecutor?
        return new JpaNativeQuery<>(select, table, conditionColumn, repository.getEntityType());
    }

    /**
     * Create repository.
     *
     * @param id id
     * @param repository repository
     * @return repo
     */
    @NonNull
    @Override
    protected JpaQueryRepository createRepository(String id, @NonNull Repository<?, ?> repository) {
        return new JpaQueryRepository(repository, annotationFinder);
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
    protected OrmAssembleAnnotation<AssembleJpa> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleJpa annotation) {
        QueryDefinition queryDefinition = new QueryDefinition.Impl(
            null, annotation.mappingType(), annotation.repository(),
            CollectionUtils.newCollection(HashSet::new, annotation.selects()), annotation.where(),
            annotation.duplicateStrategy()
        );
        return OrmAssembleAnnotation.<AssembleJpa>builder()
            .queryDefinition(queryDefinition)
            .annotatedElement(element)
            .annotation(annotation)
            .id(annotation.id())
            .key(annotation.key())
            .keyResolver(annotation.keyResolver())
            .keyDesc(annotation.keyDesc())
            .sort(annotation.sort())
            .groups(annotation.groups())
            .keyType(annotation.keyType())
            .handler(annotation.handler())
            .handlerType(annotation.handlerType())
            .mappingTemplates(annotation.propTemplates())
            .props(annotation.props())
            .prop(annotation.prop())
            .propertyMappingStrategy(annotation.propertyMappingStrategy())
            .build();
    }

    @ToString(onlyExplicitlyIncluded = true)
    private class JpaNativeQuery<T> implements MethodInvoker {
        private static final String SQL = "select {} from {} where {}";
        @ToString.Include
        private final String sqlTemplate;
        private final Class<T> entityType;

        public JpaNativeQuery(String selectColumns, String table, String conditionColumn, Class<T> entityType) {
            this.entityType = entityType;
            this.sqlTemplate = StringUtils.format(
                SQL, selectColumns, table, conditionColumn
            ) + " in (?0)";
        }

        @Override
        public Object invoke(Object target, Object... args) {
            Collection<?> keys = CollectionUtils.adaptObjectToCollection(args[0]);
            return keys.isEmpty() ? Collections.emptyList() : doQuery(keys);
        }

        private Object doQuery(Collection<?> keys) {
            return entityManager.createNativeQuery(sqlTemplate, entityType)
                .setParameter(0, keys)
                .getResultList();
        }
    }
}
