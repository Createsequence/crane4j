package cn.crane4j.core.support.query;

/**
 * Repository provider.
 *
 * @param <R> repository type
 * @author huangchengxing
 */
@FunctionalInterface
public interface RepositoryTargetProvider<R> {

    /**
     * Get repository by given id.
     *
     * @param queryDefinition query definition
     * @return repository
     */
    R get(QueryDefinition queryDefinition);
}
