package cn.crane4j.extension.redisson;

import cn.crane4j.core.cache.CacheObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class StringKeyRedissonChangeManagerTest {
    private static final String PREFIX = "prefix";
    private static final String CACHE_NAME = "test";
    private static final long EXPIRE_TIME = 3000L;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private StringKeyRedissonCacheManger cacheManager;
    private RedissonClient redissonClient;
    private CacheObject<String> cache;

    @Before
    public void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(1);
        redissonClient = Redisson.create(config);
        cacheManager = new StringKeyRedissonCacheManger(redissonClient);
        cacheManager.setGlobalPrefix(PREFIX);
        cache = cacheManager.createCache("test", EXPIRE_TIME, TIME_UNIT);
    }

    @Test
    public void testPut() {
        cache.put("key", "value");
        Assert.assertEquals("value", cache.get("key"));
        Assert.assertEquals("value", redissonClient.getBucket("prefix:test:key").get());

        redissonClient.getBucket("prefix:test:key").delete();



    }

    @Test
    public void testPutAll() {
        Map<String, Object> values = new LinkedHashMap<>(2);
        values.put("key1", "value1");
        values.put("key2", "value2");

        cache.putAll(values);

        Assert.assertEquals("value1", redissonClient.getBucket("prefix:test:key1").get());
        redissonClient.getBucket("prefix:test:key1").delete();
        Assert.assertEquals("value2",redissonClient.getBucket("prefix:test:key2").get());
        redissonClient.getBucket("prefix:test:key2").delete();
    }

    @Test
    public void testGet() {
        cache.put("key", "value");
        Assert.assertEquals("value", cache.get("key"));
        redissonClient.getBucket("prefix:test:key").delete();
    }

    @Test
    public void testGetAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        Map<String, Object> map = cache.getAll(Arrays.asList("key1", "none", "key2"));
        Assert.assertEquals("value1", map.get("key1"));
        Assert.assertEquals("value2", map.get("key2"));
        Assert.assertNull(map.get("none"));
        redissonClient.getBucket("prefix:test:key1").delete();
        redissonClient.getBucket("prefix:test:key2").delete();
    }

    @Test
    public void putIfAbsent() {
        cache.put("key1", "value1");
        cache.putIfAbsent("key1", "value1");
        Assert.assertEquals("value1",redissonClient.getBucket("prefix:test:key1").get());
        redissonClient.getBucket("prefix:test:key1").delete();

        cache.putIfAbsent("key2", "value2");
        Assert.assertEquals("value2", redissonClient.getBucket("prefix:test:key2").get());
        redissonClient.getBucket("prefix:test:key2").delete();
    }

    @Test
    public void remove() {
        cache.put("key", "value");
        cache.remove("key");
        redissonClient.getBucket("prefix:test:key").delete();
    }

    @Test
    public void removeAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.removeAll(Arrays.asList("key1", "key2"));
        Assert.assertNull(redissonClient.getBucket("prefix:test:key1").get());
        Assert.assertNull(redissonClient.getBucket("prefix:test:key2").get());
    }

    @Test
    public void clear() {
        cache.put("key", "value");
        cache.clear();
        Assert.assertEquals("value", redissonClient.getBucket("prefix:test:key").get());
        cacheManager.setEnableClearCache(true);
        cache.clear();
        Assert.assertNull(redissonClient.getBucket("prefix:test:key").get());
    }
}
