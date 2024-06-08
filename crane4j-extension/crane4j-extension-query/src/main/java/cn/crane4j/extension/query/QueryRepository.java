package cn.crane4j.extension.query;

/**
 * Repository for orm.
 *
 * @param <T> target type
 * @author huangchengxing
 */
public interface QueryRepository<T> {

    /**
     * Get the target of the repository.
     *
     * @return target
     */
    T getTarget();

    /**
     * Get the primary key of the repository.
     *
     * @return primary key
     */
    String getPrimaryKeyProperty();

    /**
     * Get the column name of the repository.
     *
     * @param propertyOrColumn property or column
     * @return column name
     */
    String resolveToColumn(String propertyOrColumn);

    /**
     * Get the entity type of the repository.
     *
     * @return entity type
     */
    Class<?> getEntityType();
}
