package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author huangchengxing
 */
public abstract class BaseCacheManagerTest {

    protected CacheManager cacheManager;
    protected CacheObject<String> cache;

    @Before
    public final void init() {
        initManager();
    }

    protected abstract void initManager();

    @Test
    public void testManager() {
        // create & get
        CacheObject<String> cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(cache);
        Assert.assertEquals("test", cache.getName());
        Assert.assertSame(cache, cacheManager.getCache("test"));
        CacheObject<String> newCache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        Assert.assertNotSame(cache, newCache);

        // remove
        cache = cacheManager.getCache("test");
        Assert.assertNotNull(cache);
        cacheManager.removeCache("test");
        Assert.assertNull(cacheManager.getCache("test"));
        Assert.assertTrue(cache.isInvalid());

        // clearAll
        cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);
        cacheManager.createCache("test2", -1L, TimeUnit.MILLISECONDS);
        cacheManager.clearAll();
        Assert.assertNull(cacheManager.getCache("test"));
        Assert.assertNull(cacheManager.getCache("test2"));
    }

    @Test
    public void testCache() {
        // put & get & remove
        Object value = new Object();
        cache.put("test", value);
        Assert.assertSame(value, cache.get("test"));
        cache.remove("test");
        Assert.assertNull(cache.get("test"));

        // putIfAbsent
        cache.putIfAbsent("test", value);
        Assert.assertSame(value, cache.get("test"));
        cache.putIfAbsent("test2", value);
        Assert.assertSame(value, cache.get("test2"));

        // clear
        cache.clear();
        Assert.assertNull(cache.get("test"));
        Assert.assertNull(cache.get("test2"));

        // putAll
        Map<String, Object> values = new HashMap<>();
        values.put("test", value);
        values.put("test2", value);
        cache.putAll(Collections.emptyMap());
        cache.putAll(values);
        Assert.assertSame(value, cache.get("test"));
        Assert.assertSame(value, cache.get("test2"));
        // getAll
        Map<String, Object> cacheValues = cache.getAll(Arrays.asList("test", "test2"));
        Assert.assertEquals(value, cacheValues.get("test"));
        Assert.assertEquals(value, cacheValues.get("test2"));
        Assert.assertEquals(Collections.emptyMap(), cache.getAll(null));

        // remove all
        cache.removeAll(null);
        cache.removeAll(Arrays.asList("test", "test2"));
        Assert.assertNull(cache.get("test"));
        Assert.assertNull(cache.get("test2"));
    }
}
