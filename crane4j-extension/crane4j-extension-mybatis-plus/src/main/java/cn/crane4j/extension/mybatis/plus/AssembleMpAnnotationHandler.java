package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.core.exception.Crane4jException;
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation handler for {@link AssembleMp}.
 *
 * @author huangchengxing
 * @since 2.9.0
 */
@Setter
public class AssembleMpAnnotationHandler
    extends AbstractQueryAssembleAnnotationHandler<AssembleMp, BaseMapper<?>> {

    private static final String[] EMPTY_SELECT_COLUMNS = new String[0];
    private DataSourceSwitcher dataSourceSwitcher = DataSourceSwitcher.DO_NOTING;

    /**
     * Create an {@link AssembleMpAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param methodInvokerContainerCreator method invoker container creator
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        MethodInvokerContainerCreator methodInvokerContainerCreator) {
        super(AssembleMp.class, annotationFinder, Crane4jGlobalSorter.comparator(),
            globalConfiguration, globalConfiguration, methodInvokerContainerCreator
        );
    }

    /**
     * Create a method invoker for query.
     *
     * @param annotation      annotation
     * @param repository      repository
     * @param selectColumns   select columns
     * @param conditionColumn condition column
     * @return method invoker
     */
    @SuppressWarnings("unchecked")
    @Override
    protected @NonNull MethodInvoker createMethodInvoker(
        OrmAssembleAnnotation<AssembleMp> annotation, QueryRepository<BaseMapper<?>> repository,
        Set<String> selectColumns, String conditionColumn) {
        if (CollectionUtils.isNotEmpty(selectColumns)) {
            selectColumns.add(conditionColumn);
        }

        // replace to select sql, e.g.: name -> name AS userName
        BaseMapperQueryRepository repo = (BaseMapperQueryRepository) repository;
        selectColumns = selectColumns.stream()
            .map(c -> resolveSelectColumn(c, repo))
            .collect(Collectors.toSet());

        MethodInvoker query = MapperQuery.builder()
            .baseMapper((BaseMapper<Object>)repository.getTarget())
            .conditionColumn(conditionColumn)
            .selectColumns(CollectionUtils.isEmpty(selectColumns) ? EMPTY_SELECT_COLUMNS : selectColumns.toArray(new String[0]))
            .build();
        String datasource = annotation.getQueryDefinition().getDatasource();
        return StringUtils.isEmpty(datasource) ? query : new DataSourceSwitchedMapperQuery(query, datasource);
    }

    private String resolveSelectColumn(String column, BaseMapperQueryRepository repo) {
        TableFieldInfo fieldInfo = repo.getColumnToFieldInfoMap().get(column);
        return Objects.isNull(fieldInfo) ? column : fieldInfo.getSqlSelect();
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
    protected BaseMapperQueryRepository createRepository(String id, @NonNull BaseMapper<?> repository)  {
        return new BaseMapperQueryRepository(id, repository);
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
    protected OrmAssembleAnnotation<AssembleMp> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleMp annotation) {
        QueryDefinition queryDefinition = new QueryDefinition.Impl(
            annotation.datasource(), annotation.mappingType(), annotation.mapper(),
            CollectionUtils.newCollection(HashSet::new, annotation.selects()), annotation.where(),
            annotation.duplicateStrategy()
        );
        return OrmAssembleAnnotation.<AssembleMp>builder()
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

    /**
     * An interface for switching data source.
     *
     * @author huangchengxing
     */
    public interface DataSourceSwitcher {

        DataSourceSwitcher DO_NOTING = dataSource -> {
            // do nothing
        };

        /**
         * Do something before invoke.
         *
         * @param dataSource data source name
         */
        void beforeInvoke(String dataSource);

        /**
         * Do something after invoke.
         *
         * @param dataSource data source name
         */
        default void afterInvoke(String dataSource) {
            // do nothing
        }
    }

    @RequiredArgsConstructor
    private class DataSourceSwitchedMapperQuery implements MethodInvoker {
        private final MethodInvoker delegate;
        private final String datasource;
        @Override
        public Object invoke(Object target, Object... args) {
            dataSourceSwitcher.beforeInvoke(datasource);
            try {
                return delegate.invoke(target, args);
            } catch (Exception e) {
                throw Crane4jException.wrapIfNecessary(e);
            } finally {
                dataSourceSwitcher.afterInvoke(datasource);
            }
        }
    }

    @Builder
    @RequiredArgsConstructor
    private static class MapperQuery<T> implements MethodInvoker {
        private final BaseMapper<T> baseMapper;
        @NonNull
        private final String[] selectColumns;
        private final String conditionColumn;
        @Override
        public Object invoke(Object target, Object... args) {
            Collection<?> keys = CollectionUtils.adaptObjectToCollection(args[0]);
            return keys.isEmpty() ?
                Collections.emptyList() : baseMapper.selectList(getQueryWrapper(keys));
        }
        private QueryWrapper<T> getQueryWrapper(Collection<?> keys) {
            QueryWrapper<T> wrapper = Wrappers.<T>query().in(conditionColumn, keys);
            if (selectColumns.length > 0) {
                wrapper.select(selectColumns);
            }
            return wrapper;
        }
    }
}
