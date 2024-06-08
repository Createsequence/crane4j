package cn.crane4j.extension.query;

import cn.crane4j.annotation.DuplicateStrategy;
import cn.crane4j.annotation.MappingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

/**
 * <p>The definition of orm query,
 * such as "{@code select [selectColumns] from [fromTable] where [conditionColumns] in (keys) }"
 *
 * @author huangchengxing
 * @since 2.9.0
 */
public interface QueryDefinition {

    /**
     * Get the datasource.
     *
     * @return datasource
     */
    @Nullable
    String getDatasource();

    /**
     * Get the mapping type of the query result.
     *
     * @return mapping type
     */
    MappingType getMappingType();

    /**
     * Get the table.
     *
     * @return String
     */
    String getRepositoryId();

    /**
     * Get the select columns.
     *
     * @return columns
     */
    Set<String> getSelectColumns();

    /**
     * Get the key column.
     *
     * @return column
     */
    @Nullable
    String getConditionColumn();

    /**
     * Get the duplicate strategy.
     *
     * @return DuplicateStrategy
     */
    DuplicateStrategy getDuplicateStrategy();

    @Getter
    @RequiredArgsConstructor
    class Impl implements QueryDefinition {
        private final String datasource;
        private final MappingType mappingType;
        private final String repositoryId;
        private final Set<String> selectColumns;
        private final String conditionColumn;
        private final DuplicateStrategy duplicateStrategy;
    }
}
