package cn.crane4j.core.support.container.query;

import cn.crane4j.annotation.DuplicateStrategy;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>Provides functionality to generate a specific {@link MethodInvokerContainer} object that
 * supports generating SQL statements based on specified conditions and invoking the repository to execute the SQL,
 * thereby retrieving data from the database.
 *
 * <h3>Implementation</h3>
 * <p>The implementing class must provide a repository information object
 * that implements the {@link Repository} interface (typically as an inner class).<br />
 * It should also implement the {@link #createRepository} method to create this object when registering the repository.<br />
 * This object will provide necessary functionality during SQL statement generation, including:
 * <ul>
 *     <li>Retrieving the entity class type associated with the repository;</li>
 *     <li>Converting the specified property names in the JavaBean to query fields in SQL;</li>
 *     <li>Obtaining the table name for querying in SQL;</li>
 * </ul>
 *
 * <h3>Cache</h3>
 * <p>The registered {@link Repository} ({@code t}) and generated {@link MethodInvokerContainer} will be cached.<br />
 * Therefore, duplicate registration of the same {@link Repository} is not supported,
 * and duplicate creation of the same {@link MethodInvokerContainer} will be avoided.<br />
 * This cache is typically released only during JVM garbage collection.
 * If necessary, you can manually invoke {@link #destroy} to clear all caches.
 *
 * @param <T> The data type stored in the repository.
 * @author huangchengxing
 * @deprecated This class is still used, but it will be refactored in the future.
 */
@Deprecated
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractQueryContainerProvider<T> implements ContainerProvider {

    @Getter
    protected final Map<String, Repository<T>> registeredRepositories = new ConcurrentHashMap<>(32);
    protected final MethodInvokerContainerCreator methodInvokerContainerCreator;

    // =================== repository ===================

    /**
     * Registers a repository object.
     *
     * @param name name of the repository.
     * @param target repository object.
     */
    public final void registerRepository(String name, T target) {
        Objects.requireNonNull(name, "repository name must not null");
        Objects.requireNonNull(target, "repository must not null");
        CollectionUtils.computeIfAbsent(registeredRepositories, name, n -> createRepository(n, target));
    }

    /**
     * Creates a repository object.
     *
     * @param name name of the repository.
     * @param target repository object.
     * @return A {@link Repository} instance.
     */
    protected abstract Repository<T> createRepository(String name, T target);

    // =================== container ===================

    /**
     * Get container instance by given namespace
     *
     * @param namespace namespace of container
     * @return container instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {
        // is a heavy operation, but the manager will cache it
        return (Container<K>) createQueryContainer(namespace);
    }

    /**
     * <p>Get a container based on repository object {@link T}.<br />
     * When querying, the attribute name of the input parameter will be converted to
     * the table columns specified in the corresponding property.
     *
     * @param name mapper name
     * @param keyProperty key field name for a query, if it is empty, it defaults to the specified key field
     * @param properties fields to query, if it is empty, all table columns will be queried by default.
     * @return container
     */
    public <K> Container<K> getQueryContainer(
        String name, @Nullable String keyProperty, @Nullable List<String> properties) {
        String namespace = determineNamespace(name, keyProperty, properties);
        return getContainer(namespace);
    }

    /**
     * Determines the namespace of container.
     *
     * @param name mapper name
     * @param keyProperty key field name for a query, if it is empty, it defaults to the specified key field
     * @param properties fields to query, if it is empty, all table columns will be queried by default.
     * @return namespace
     * @see #resolveQueryInfo(String)
     */
    public abstract String determineNamespace(String name, @Nullable String keyProperty, @Nullable List<String> properties);

    /**
     * Resolves the query information for the specified namespace which generated by {@link #determineNamespace}.
     *
     * @param namespace namespace.
     * @return {@link QueryInfo} instance.
     */
    protected abstract QueryInfo resolveQueryInfo(String namespace);

    private Container<?> createQueryContainer(String namespace) {
        // prepare query info and repository
        QueryInfo queryInfo = resolveQueryInfo(namespace);
        Asserts.isNotNull(queryInfo, "cannot resolve query info for namespace [{}]", namespace);
        String name = queryInfo.getRepository();
        String keyProperty = queryInfo.getKeyProperty();
        List<String> properties = queryInfo.getProperties();
        Repository<T> repository = registeredRepositories.get(name);
        Asserts.isNotNull(repository, "cannot find repository [{}]", name);

        // resolve columns for query
        Set<String> queryColumns = CollectionUtils.defaultIfEmpty(properties, Collections.emptyList())
                .stream()
                .map(p -> repository.propertyToQueryColumn(p, p))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // resolve key column for query
        keyProperty = StringUtils.emptyToDefault(keyProperty, repository.getKeyProperty());
        String keyColumn = repository.propertyToColumn(keyProperty, keyProperty);
        String keyQueryColumn = repository.propertyToQueryColumn(keyProperty, keyProperty);
        if (!queryColumns.isEmpty()) {
            queryColumns.add(keyQueryColumn);
        }

        return doCreateContainer(
            namespace, keyProperty, repository, queryInfo, queryColumns, keyColumn);
    }

    private MethodInvokerContainer doCreateContainer(
            String namespace, String keyProperty, Repository<T> repository, QueryInfo queryInfo, Set<String> queryColumns, String keyColumn) {
        MethodInvoker methodInvoker = createMethodInvoker(
            namespace, repository, queryColumns, keyColumn, keyProperty
        );
        MethodInvokerContainerCreator.MethodInvokerContainerCreation containerCreation = MethodInvokerContainerCreator.MethodInvokerContainerCreation.builder()
            .target(repository.getTarget())
            .methodInvoker(methodInvoker)
            .mappingType(queryInfo.getMappingType())
            .namespace(namespace)
            .resultType(repository.getEntityType())
            .resultKey(keyProperty)
            .duplicateStrategy(DuplicateStrategy.ALERT)
            .build();
        return methodInvokerContainerCreator.createContainer(containerCreation);
    }

    /**
     * Creates a {@link MethodInvoker} object.
     *
     * @param namespace The namespace.
     * @param repository The repository object.
     * @param queryColumns The columns to query.
     * @param keyColumn The key column.
     * @param keyProperty The key property.
     * @return A MethodInvoker object.
     */
    @NonNull
    protected abstract MethodInvoker createMethodInvoker(
            String namespace, Repository<T> repository,
            Set<String> queryColumns, String keyColumn, String keyProperty);


    // =================== components ===================

    /**
     * Clear all caches
     */
    public void destroy() {
        this.registeredRepositories.clear();
    }

    /**
     * An interface for a generic repository that provides access to data storage.
     *
     * @param <T> the type of data stored in the repository.
     */
    public interface Repository<T> {

        /**
         * Returns the target object stored in the repository.
         *
         * @return the target object.
         */
        T getTarget();

        /**
         * Returns the name of the table where the data is stored.
         *
         * @return the table name.
         */
        String getTableName();

        /**
         * Returns the entity type of the stored data.
         *
         * @return the entity type.
         */
        Class<?> getEntityType();

        /**
         * Returns the key property for the data stored in the repository.
         *
         * @return the key property.
         */
        String getKeyProperty();

        /**
         * Converts a property name to its corresponding column name.<br />
         * eg:
         * <pre>
         *     userName -> user_name
         *     userAge -> user_age
         *     id -> id as id
         * </pre>
         *
         * @param property the property to convert.
         * @param defaultValue the default value to return if no matching column is found.
         * @return the column name.
         */
        String propertyToColumn(String property, String defaultValue);

        /**
         * Converts a property name to its corresponding query column name.<br />
         * eg:
         * <pre>
         *     userName -> user_name as userName
         *     userAge -> user_age as userAge
         *     id -> id as id
         * </pre>
         *
         * @param property the property to convert.
         * @param defaultValue the default value to return if no matching query column is found.
         * @return the query column name.
         */
        String propertyToQueryColumn(String property, String defaultValue);
    }

    /**
     * An interface for query information.
     *
     * @author huangchengxing
     */
    public interface QueryInfo {

        /**
         * Get repository name for a query.
         *
         * @return repository name
         * @see #registerRepository
         */
        String getRepository();

        /**
         * Get key property for a query.
         *
         * @return key property
         */
        @Nullable
        String getKeyProperty();

        /**
         * Get properties for query.
         *
         * @return properties
         */
        @Nullable
        List<String> getProperties();

        /**
         * Returns the mapping type for a given property.
         *
         * @return the mapping type.
         */
        MappingType getMappingType();
    }
}
