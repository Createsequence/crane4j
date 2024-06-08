package cn.crane4j.extension.query;

import cn.crane4j.annotation.AssembleDb;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>A {@link AbstractQueryAssembleAnnotationHandler} implementation for {@link AssembleDb}.
 *
 * @author huangchengxing
 */
public class AssembleDbAnnotationHandler
    extends AbstractQueryAssembleAnnotationHandler<AssembleDb, QueryDefinition> {

    private static final RepositoryTargetProvider<QueryDefinition> REPOSITORY_PROVIDER = def -> def;
    private final DataSourceProvider dataSourceProvider;

    /**
     * Create an {@link AbstractQueryAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder               annotation finder
     * @param globalConfiguration            global configuration
     * @param methodInvokerContainerCreator  method invoker container creator
     * @param dataSourceProvider data source provider
     */
    public AssembleDbAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        MethodInvokerContainerCreator methodInvokerContainerCreator, DataSourceProvider dataSourceProvider) {
        super(
            AssembleDb.class, annotationFinder, Crane4jGlobalSorter.comparator(),
            globalConfiguration, globalConfiguration, methodInvokerContainerCreator
        );
        this.dataSourceProvider = dataSourceProvider;
        super.setRepositoryTargetProvider(REPOSITORY_PROVIDER);
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
        OrmAssembleAnnotation<AssembleDb> annotation, QueryRepository<QueryDefinition> repository,
        Set<String> selectColumns, String conditionColumn) {
        String sql = "select {} from {} where {} in (?)";
        boolean isSelectAll = CollectionUtils.isEmpty(selectColumns)
            || (selectColumns.size() == 1 && selectColumns.contains(conditionColumn));
        sql = CharSequenceUtil.format(sql,
            isSelectAll ? "*" : String.join(", ", selectColumns),
            annotation.getQueryDefinition().getRepositoryId(),
            conditionColumn
        );
        return new DataSourceQuery(annotation.getQueryDefinition().getDatasource(), sql);
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
    protected OrmAssembleAnnotation<AssembleDb> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleDb annotation) {
        QueryDefinition queryDefinition = new QueryDefinition.Impl(
            annotation.datasource(), annotation.mappingType(), annotation.fromTable(),
            CollectionUtils.newCollection(HashSet::new, annotation.selectColumns()), annotation.whereColumn(),
            annotation.duplicateStrategy()
        );
        return OrmAssembleAnnotation.<AssembleDb>builder()
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

    @RequiredArgsConstructor
    protected class DataSourceQuery implements MethodInvoker {

        private final String datasource;
        private final String sql;
        @SneakyThrows
        @SuppressWarnings("java:S2095")
        @Override
        public Object invoke(Object target, Object... args) {
            Connection connection = dataSourceProvider.getConnection(datasource);
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Collection<?> keys = CollectionUtils.adaptObjectToCollection(args[0]);
            if (keys.isEmpty()) {
                return Collections.emptyList();
            }
            String keyStr = keys.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            preparedStatement.setObject(1, keyStr);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resolveQueryResults(resultSet);
        }

        @NonNull
        private List<Map<String, Object>> resolveQueryResults(ResultSet resultSet) throws SQLException {
            List<Map<String, Object>> results = new ArrayList<>();
            while (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                Map<String, Object> row = new HashMap<>(8);
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    row.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
                }
                results.add(row);
            }
            return results;
        }
    }

    // ========== unsupported methods ==========

    @Override
    public void setRepositoryTargetProvider(
        @Nullable RepositoryTargetProvider<QueryDefinition> repositoryTargetProvider)
        throws UnsupportedOperationException {
        throw new UnsupportedOperationException("RepositoryTargetProvider is not supported to set!");
    }

    @Override
    public void registerRepository(String id, @NonNull QueryDefinition queryDefinition) {
        Asserts.isNotEmpty(queryDefinition.getConditionColumn(), "condition column cannot be empty!");
        ormRepositoryMap.put(id, new DataSourceRepository(queryDefinition));
    }

    @SuppressWarnings("java:S6548")
    @Getter
    @RequiredArgsConstructor
    private static class DataSourceRepository implements QueryRepository<QueryDefinition> {
        private final QueryDefinition target;
        @Override
        public String getPrimaryKeyProperty() {
            return target.getConditionColumn();
        }
        @Override
        public String resolveToColumn(String propertyOrColumn) {
            return propertyOrColumn;
        }
        @Override
        public Class<?> getEntityType() {
            return Map.class;
        }
    }
}
