package cn.crane4j.extension.spring;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;

/**
 * test for {@link SoftConcurrentMapCacheManager}
 *
 * @author huangchengxing
 */
public class SoftConcurrentMapCacheManagerTest extends BaseCacheManagerTest {

    @Override
    protected void initManager() {
        cacheManager = new SoftConcurrentMapCacheManager();
        cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        Assert.assertEquals(cacheManager.getClass().getSimpleName(), cacheManager.getName());
    }
}
