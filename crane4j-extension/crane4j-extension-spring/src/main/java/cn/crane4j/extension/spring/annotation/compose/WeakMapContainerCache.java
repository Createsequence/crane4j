package cn.crane4j.extension.spring.annotation.compose;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.MapCacheManager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a container as cacheable with {@link MapCacheManager.WeakConcurrentMapCacheManager}.
 *
 * @author huangchengxing
 * @see ContainerCache
 * @since 2.8.0
 */
@ContainerCache(cacheManager = CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WeakMapContainerCache {
}
