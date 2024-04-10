package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.MapCacheManager;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Map;

/**
 * An implementation of the {@link MapCacheManager} that
 * creates a cache instance what stores data in the {@link ConcurrentReferenceHashMap}.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
public class SoftConcurrentMapCacheManager extends MapCacheManager {

    /**
     * Create a {@link Map} instance.
     *
     * @return map instance
     */
    @Override
    protected <K> Map<K, Object> createMap() {
        return new ConcurrentReferenceHashMap<>(32, ConcurrentReferenceHashMap.ReferenceType.SOFT);
    }
}
