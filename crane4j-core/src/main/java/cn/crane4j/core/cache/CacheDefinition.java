package cn.crane4j.core.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Definition of cache.
 *
 * @author huangchengxing
 * @since 2.4.0
 * @see Impl
 */
public interface CacheDefinition {

    /**
     * Get the name of this cache.
     *
     * @return cache name
     */
    String getName();

    /**
     * Get the cache factory name
     *
     * @return cache factory name
     * @see CacheManager
     */
    @Nullable
    String getCacheManager();

    /**
     * Get the expiry time of this cache.
     *
     * @return expire time
     */
    Long getExpireTime();

    /**
     * Get the time unit of this cache.
     *
     * @return time unit
     */
    TimeUnit getTimeUnit();

    /**
     * <p>Implementation of {@link CacheDefinition}.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    @Getter
    @RequiredArgsConstructor
    class Impl implements CacheDefinition {
        private final String name;
        private final String cacheManager;
        private final Long expireTime;
        private final TimeUnit timeUnit;
    }
}
