package cn.crane4j.extension.redisson;
import cn.crane4j.core.cache.AbstractCacheManager;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Setter
@Slf4j
public class StringKeyRedissonCacheManger extends AbstractCacheManager {
    /**
     * Global prefix for all cache keys
     */
    @NonNull
    private String globalPrefix = "crane4j:cache:";
    @NonNull
    private RedissonClient redissonClient;
    private boolean enableClearCache = false;
    public StringKeyRedissonCacheManger(@NonNull RedissonClient redisson) {
        this.redissonClient = redisson;
    }

    /**
     * Create cache instance.
     */
    @NonNull
    @Override
    protected RedissonCacheObject doCreateCache(String name,Long expireTime, TimeUnit timeUnit){
        return new RedissonCacheObject(name,expireTime,timeUnit);
    }

    /**
     * Get the cache key which is used to store value in redisson.
     */
    protected String resolveCacheKey(String cacheName,String key){
        return globalPrefix + ":" + cacheName + ":" + key;
    }

    /**
     * Resolve cache value.
     * @param value cache value
     * @return cache value
     */
    protected Object resolveCacheValue(Object value){return  value;}

    /**
     * Clean all cache value for a specified cache object.
     */
    protected void cleanCache(String cacheName) {
        if (enableClearCache) {
            String prefix = globalPrefix + ":" + cacheName + ":*";

            long deletedCount = 0;
            for (String key : redissonClient.getKeys().getKeysByPattern(prefix)) {
                RBucket<Object> rBucket = redissonClient.getBucket(key);
                rBucket.delete();
                deletedCount++;
            }

            log.warn("Cleared [{}] keys from cache [{}] by prefix [{}]", deletedCount, cacheName, prefix);
        } else {
            log.warn("Clearing all cache values is not supported in redis cache [{}]", cacheName);
        }
    }



    /**
     * Redis cache object.
     */
    protected class RedissonCacheObject extends AbstractCacheObject<String>{
        private final long expireTime;
        private final TimeUnit timeUnit;
        private volatile boolean invalid = false;

        protected RedissonCacheObject(String name,Long expireTime,TimeUnit timeUnit){
            super(name);
            this.expireTime = expireTime;
            this.timeUnit = timeUnit;
        }

        /**
         * Clear all cache value.
         */
        public void clear(){cleanCache(getName());}

        /**
         * Add all cache value.
         * @param caches value
         */
        public void putAll(Map<String,Object> caches){
            RBatch rBatch = redissonClient.createBatch();

            for(Map.Entry<String,Object> entry : caches.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                String cacheKey = globalPrefix + ":" + getName() + ":" + key;
                RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
                bucket.setAsync(value);
                rBatch.getBucket(cacheKey).setAsync(value);
            }
            rBatch.execute();
        }




        /**
         * Get all caches according to the key values.
         * @param keys keys
         * @return map containing keys and their corresponding values
         */
        public Map<String, Object> getAll(Iterable<String> keys) {
            Set<String> keySet = StreamSupport.stream(keys.spliterator(), false)
                .map(key -> resolveCacheKey(getName(), key))
                .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, Object> results = new LinkedHashMap<>();

            RBatch rBatch = redissonClient.createBatch();
            for (String key : keySet) {
                rBatch.getBucket(key).getAsync();
            }

            List<?> resultValues = rBatch.execute().getResponses();
            int index = 0;
            for (String key : keys) {
                Object value = resultValues.get(index++);
                if (value != null) {
                    results.put(key, value);
                }
            }
            return results;
        }



        /**
         * Clear all cache value for a specified cache object.
         * @param cacheName
         */
        protected void cleanCache(String cacheName) {
            if (enableClearCache) {
                String prefix = globalPrefix + ":" + cacheName + ":*";

                Set<String> keys = new HashSet<>();
                for (String key : redissonClient.getKeys().getKeysByPattern(prefix)) {
                    keys.add(key);
                }

                RBatch rBatch = redissonClient.createBatch();
                for (String key : keys) {
                    rBatch.getBucket(key).deleteAsync();
                }
                rBatch.execute();

                log.warn("Cleared [{}] keys from cache [{}] by prefix [{}]", keys.size(), cacheName, prefix);
            } else {
                log.warn("Clearing all cache values is not supported in redis cache [{}]", cacheName);
            }
        }


        /**
         * Get the cache according to the key value.
         * @param key key
         * @return cache value
         */
        @Nullable
        public Object get(String key){
            String cacheKey = resolveCacheKey(getName(),key);
            RBucket<Object> rBucket = redissonClient.getBucket(cacheKey);
            return rBucket.get();
        }

        /**
         * Add cache value.
         * @param key key
         * @param value value
         */
        public void put(String key, Object value){
            String cacheKey = resolveCacheKey(getName(),key);
            Object cacheValue = resolveCacheValue(value);
            RBucket<Object> bucket = redissonClient.getBucket(cacheKey);
            bucket.set(cacheValue,expireTime,timeUnit);
        }

        /**
         * Add cache value if it does not exist
         * @param key key
         * @param value value
         */
        public void putIfAbsent(String key, Object value) {
            String cacheKey = resolveCacheKey(getName(), key);
            RBucket<Object> bucket = redissonClient.getBucket(cacheKey);

            if (bucket.isExists()) return;
            Object cacheValue = resolveCacheValue(value);
            bucket.set(cacheValue, expireTime, timeUnit);
        }

        /**
         * Remove cache value
         * @param key key
         */
        public void remove(String key){
            String cacheKey = resolveCacheKey(getName(),key);
            RBucket rBucket = redissonClient.getBucket(cacheKey);
            rBucket.delete();
        }

        /**
         * Remove all cache value
         * @param keys keys
         */
        public void removeAll(Iterable<String> keys) {
            Set<String> keySet = StreamSupport.stream(keys.spliterator(), false)
                .map(key -> resolveCacheKey(getName(), key))
                .collect(Collectors.toSet());

            RBatch rBatch = redissonClient.createBatch();

            for (String key : keySet) {
                RBucket<Object> bucket = redissonClient.getBucket(key);
                rBatch.getBucket(bucket.getName()).deleteAsync();
            }

            rBatch.execute();
        }
    }
}