package cn.crane4j.core.container;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * A {@link Container} that has a limit on the number of data source objects it can contain.
 *
 * @author huangchengxing
 * @see ImmutableMapContainer
 * @since 2.2.0
 */
public interface LimitedContainer<K> extends Container<K> {

    /**
     * Get all data source objects in the container.
     *
     * @return all elements
     */
    Map<K, ?> getAll();

    /**
     * Refresh the container with new data.
     *
     * @param data data
     * @since 2.9.0
     */
    default void refresh(@NonNull Map<K, ?> data) {
        // do nothing
    }
}
