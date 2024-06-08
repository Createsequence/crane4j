package cn.crane4j.extension.query;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.Try;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * DataSource provider.
 *
 * @author huangchengxing
 */
@FunctionalInterface
public interface DataSourceProvider {

    /**
     * Get connection.
     *
     * @param datasourceName datasource name
     * @return connection
     */
    @NonNull
    Connection getConnection(String datasourceName);

    /**
     * Create a data source provider.
     *
     * @param dataSource data source
     * @return data source provider
     */
    static DataSourceProvider of(@NonNull DataSource dataSource) {
        return new SingleDataSourceProvider(dataSource);
    }

    /**
     * Create a data source provider.
     *
     * @param dataSources data source
     * @return data source provider
     */
    static DataSourceProvider of(@NonNull Map<String, DataSource> dataSources) {
        Asserts.isNotEmpty(dataSources, "DataSource map must not be empty");
        return new MultiDataSourceProvider(dataSources);
    }

    /**
     * A data source provider that supports multiple data sources.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    class MultiDataSourceProvider implements DataSourceProvider {

        private final Map<String, DataSource> dataSourceMap;

        /**
         * Get connection.
         *
         * @param datasourceName datasource name
         * @return connection
         */
        @Override
        public @NonNull Connection getConnection(String datasourceName) {
            DataSource dataSource = dataSourceMap.get(datasourceName);
            Asserts.isNotNull(dataSource, "DataSource [{}] not found", datasourceName);
            return Try.of(() -> dataSource.getConnection())
                .getOrElseThrow(Crane4jException::new);
        }
    }

    /**
     * Single data source provider.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    class SingleDataSourceProvider implements DataSourceProvider {

        private final DataSource dataSource;

        /**
         * Get connection.
         *
         * @param datasourceName datasource name
         * @return connection
         */
        @Override
        public @NonNull Connection getConnection(String datasourceName) {
            return Try.of(() -> dataSource.getConnection())
                .getOrElseThrow(Crane4jException::new);
        }
    }
}
