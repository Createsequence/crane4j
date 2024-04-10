package cn.crane4j.extension.spring.annotation.compose;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.GuavaCacheManager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation to mark a container as cacheable with {@link GuavaCacheManager}.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @since 2.8.0
 */
@ContainerCache(cacheManager = CacheManager.DEFAULT_GUAVA_CACHE_MANAGER_NAME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GuavaContainerCache {

    /**
     * The time to live of the cache,
     * default to -1L, which means the cache will never proactive evict.
     *
     * @return time to live
     * @since 2.4.0
     */
    long expirationTime() default -1L;

    /**
     * The time unit of the cache expiry time,
     * default to {@link TimeUnit#MILLISECONDS}.
     *
     * @return time unit
     * @since 2.4.0
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
