package cn.crane4j.core.container;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A container that stores key-value pairs.
 *
 * <p>Supports the following factory methods to create containers:
 * <ul>
 *     <li>{@link #forMap}: key-value pairs in the specified map;</li>
 * </ul>
 * this method also supports configuration through annotations.
 *
 * <p>for performance reasons, when get data from container,
 * it always returns all data that set in the creation time.
 * and data will not be updated after the container is created.
 *
 * @param <K> key type
 * @author tangcent
 * @author huangchengxing
 * @see LimitedContainer
 * @since 2.0.0
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImmutableMapContainer<K> implements LimitedContainer<K>, Container.Lifecycle {

    /**
     * namespace of the data source container,
     */
    @Getter
    private final String namespace;

    /**
     * data source objects grouped by key value
     */
    private volatile Map<K, ?> data;

    /**
     * <p>Create a key-value pair container based on the specified {@link Map} instance.
     *
     * @param namespace namespace
     * @param data      data source objects grouped by key value
     * @param <K>       key type
     * @return container
     */
    public static <K> ImmutableMapContainer<K> forMap(String namespace, @NonNull Map<K, ?> data) {
        Objects.requireNonNull(namespace, "namespace must not null");
        Objects.requireNonNull(data, "data must not null");
        return new ImmutableMapContainer<>(namespace, data);
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        return data;
    }

    /**
     * Get all data source objects in the container.
     *
     * @return all elements
     */
    @Override
    public Map<K, ?> getAll() {
        return data;
    }

    /**
     * Refresh the container with new data.
     *
     * @param data data
     * @since 2.9.0
     */
    @Override
    public void refresh(@NonNull Map<K, ?> data) {
        this.data = data;
    }

    /**
     * Destroy the container
     */
    @Override
    public void destroy() {
        try {
            data.clear();
        } catch (UnsupportedOperationException ex) {
            // ignore if map is immutable
        }
    }
}
