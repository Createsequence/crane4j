package cn.crane4j.core.cache;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDelegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Data source container wrapper class with cache function.<br />
 * When the data source is obtained from the key set,
 * it will first try to obtain it from the cache.<br />
 * If some key sets do not exist in the cache,
 * it will be obtained from the original container and added to the cache for this batch of keys.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see CacheObject
 * @see CacheableContainerProcessor
 * @since 2.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class CacheableContainer<K> implements ContainerDelegate<K> {

    private final Container<K> container;
    private final CacheDefinition cacheDefinition;
    private final CacheManager cacheManager;
    private volatile CacheObject<K> currentCache;

    /**
     * <p>Enter a batch of key values to return data source objects grouped by key values.
     * If part of the key value entered has been cached,
     * the data corresponding to that part of the key value will be obtained from the cache first.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        CacheObject<K> current = getCurrentCache();
        Map<K, Object> caches = current.getAll(keys);

        // all keys are not cached?
        if (caches.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("get none cached keys [{}] from container [{}]", keys, container.getNamespace());
            }
            Map<K, Object> values = (Map<K, Object>)container.get(keys);
            current.putAll(values);
            return values;
        }

        // some keys are cached?
        keys = keys.stream()
            .filter(k -> !caches.containsKey(k))
            .collect(Collectors.toSet());
        // fix https://github.com/opengoofy/crane4j/issues/304
        if (keys.isEmpty()) {
            return caches;
        }

        // get none cached keys from container
        if (log.isDebugEnabled()) {
            log.debug("get none cached keys [{}] from container [{}]", keys, container.getNamespace());
        }
        Map<K, Object> values = (Map<K, Object>)container.get(keys);
        current.putAll(values);
        // merge cached values and none cached values
        caches.putAll(values);
        return caches;
    }

    /**
     * Get the current cache object, if the current cache object is null or invalid,
     * recreate it by cache manager which is specified in cache definition.
     *
     * @return current cache object
     */
    public CacheObject<K> getCurrentCache() {
        if (currentCache == null || currentCache.isInvalid()) {
            synchronized (this) {
                if (currentCache == null || currentCache.isInvalid()) {
                    if (log.isDebugEnabled()) {
                        log.debug("recreate cache object for container [{}], because it is invalid or null", container.getNamespace());
                    }
                    currentCache = cacheManager.createCache(
                        cacheDefinition.getName(), cacheDefinition.getExpireTime(), cacheDefinition.getTimeUnit()
                    );
                }
            }
        }
        return currentCache;
    }
}
