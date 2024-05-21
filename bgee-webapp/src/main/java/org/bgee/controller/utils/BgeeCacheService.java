package org.bgee.controller.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class manages local caches through all the webapp. It is not a response cache,
 * caching whole responses from the server.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Jan. 2023
 * @since Bgee 15.0, Jan. 2023
 */
public class BgeeCacheService implements Closeable {
    private final static Logger log = LogManager.getLogger(BgeeCacheService.class.getName());

    public static enum CacheType {
        LRU
    }

    /**
     * Class allowing to describe a cache. {@code hashCode} and {@code equals} methods
     * are based solely on {@code name} attribute, so be careful to use unique names
     * for your cache definitions.
     *
     * @author Frederic Bastian
     * @version Bgee 15.0, Jan. 2023
     * @since Bgee 15.0, Jan. 2023
     *
     * @param <T>   The type of the keys in the cache
     * @param <U>   The type of the associated values in the cache
     */
    public static class CacheDefinition<T, U> {
        private final String name;
        private final Class<T> keyType;
        private final Class<U> valueType;
        private final CacheType cacheType;
        private final int maxSize;

        public CacheDefinition(String name, Class<T> keyType, Class<U> valueType,
                CacheType cacheType, int maxSize) {
            this.name = name;
            this.keyType = keyType;
            this.valueType = valueType;
            this.cacheType = cacheType;
            this.maxSize = maxSize;
        }

        public String getName() {
            return name;
        }
        public Class<T> getKeyType() {
            return keyType;
        }
        public Class<U> getValueType() {
            return valueType;
        }
        public CacheType getCacheType() {
            return cacheType;
        }
        public int getMaxSize() {
            return maxSize;
        }

        
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheDefinition<?, ?> other = (CacheDefinition<?, ?>) obj;
            return Objects.equals(name, other.name);
        }

        public boolean completelyEquals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheDefinition<?, ?> other = (CacheDefinition<?, ?>) obj;
            return cacheType == other.cacheType
                    && Objects.equals(keyType.getName(), other.keyType.getName())
                    && maxSize == other.maxSize
                    && Objects.equals(name, other.name)
                    && Objects.equals(valueType.getName(), other.valueType.getName());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CacheDefinition [")
                   .append("name=").append(name)
                   .append(", keyType=").append(keyType)
                   .append(", valueType=").append(valueType)
                   .append(", cacheType=").append(cacheType)
                   .append(", maxSize=").append(maxSize)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * A {@code Map} storing all the defined caches, stored using their {@code CacheDefinition}
     * as key. This {@code Map} is thread-safe {@code ConcurrentHashMap}.
     */
    private static final ConcurrentHashMap<CacheDefinition<?, ?>, Map<?, ?>> ALL_CACHES =
            new ConcurrentHashMap<>();

    public BgeeCacheService() {
        
    }

    public <T, U> Map<T, U> registerCache(CacheDefinition<T, U> cacheDefinition) {
        log.traceEntry("{}", cacheDefinition);
        if (cacheDefinition == null) {
            throw log.throwing(new IllegalArgumentException("CacheDefinition cannot be null"));
        }
        //Check not atomic, it will be redone at every use of a cache anyway
        if (ALL_CACHES.keySet().stream()
                .anyMatch(k -> k.equals(cacheDefinition) && !k.completelyEquals(cacheDefinition))) {
            throw log.throwing(new IllegalArgumentException(
                    "A CacheDefinition with the same name but a different configuration already exists."));
        }
        //SuppressWarnings since we create the Map with the correct type
        @SuppressWarnings("unchecked")
        Map<T, U> cache = (Map<T, U>) ALL_CACHES.computeIfAbsent(cacheDefinition, k -> {
            switch (cacheDefinition.getCacheType()) {
            case LRU:
                // The {@code Map} is thread-safe by using the method {@code Collections.synchronizedMap},
                // and is backed-up by a {@link org.bgee.controller.utils.LRUCache LRUCache}.
                // Maybe we should use a Guava cache instead.
                return Collections.synchronizedMap(new LRUCache<T, U>(cacheDefinition.getMaxSize()));
            default:
                throw log.throwing(new IllegalStateException("Unsupported cache type: "
                        + cacheDefinition.getCacheType()));
            }
        });
        return log.traceExit(cache);
    }

    public <T, U> U useCacheNonAtomic(CacheDefinition<T, U> cacheDefinition, T cacheKey,
            Supplier<U> computeResults, Long computeTimeForCacheInMs) {
        log.traceEntry("{}, {}, {}, {}", cacheDefinition, cacheKey, computeResults,
                computeTimeForCacheInMs);
        return log.traceExit(this.useCacheNonAtomic(cacheDefinition, cacheKey,
                computeResults, x -> x, x -> x, computeTimeForCacheInMs));
    }
    public <T, U, V> V useCacheNonAtomic(CacheDefinition<T, U> cacheDefinition, T cacheKey,
            Supplier<V> computeIfCacheMiss, Function<V, U> getCacheFromCompute,
            Function<U, V> computeWithCacheHit, Long computeTimeForCacheInMs) {
        log.traceEntry("{}, {}, {}, {}", cacheDefinition, cacheKey, computeIfCacheMiss,
                getCacheFromCompute, computeWithCacheHit, computeTimeForCacheInMs);

        log.debug("Cache search for: {}", cacheDefinition);
        Map<T, U> cache = this.registerCache(cacheDefinition);
        //the search and insertion are not atomic, it's good enough in most cases,
        //and is explicit with this method name.
        //Also, we don't use the method computeIfAbsent, because that would probably block
        //the whole cache while the computation 'computeResults' is done,
        //since we simply used Collections.synchronizedMap to make the cache thread-safe.
        //It is a simple optimization, we don't care so much if several threads
        //are computing the same results.
        U hit = cache.get(cacheKey);
        log.debug("Entries in the cache before: {}", cache.size());

        V compute = null;
        if (hit == null) {
            log.debug("Cache miss for cache key: {}", cacheKey);
            long startTime = System.currentTimeMillis();
            compute = computeIfCacheMiss.get();
            hit = getCacheFromCompute.apply(compute);
            long executionTime = System.currentTimeMillis() - startTime;
            if (computeTimeForCacheInMs == null || executionTime > computeTimeForCacheInMs) {
                log.debug("Computation to store in cache, execution time: {}",
                        executionTime);
                log.trace("Cache before: {}", cache);
                cache.putIfAbsent(cacheKey, hit);
                log.trace("Cache after: {}", cache);
            } else {
                log.debug("Computation fast enough, not stored in cache, execution time: {}",
                        executionTime);
            }
        } else {
            log.debug("Cache hit for cache key: {}", cacheKey);
            log.trace("Hit: {}", hit);
            compute = computeWithCacheHit.apply(hit);
            log.trace("computed with cache hit: {}", hit);
        }
        log.debug("Entries in the cache after: {}", cache.size());
        return log.traceExit(compute);
    }

    @Override
    public void close() throws IOException {
        log.traceEntry();
        releaseAll();
        log.traceExit();
    }
    public static void releaseAll() {
        log.traceEntry();
        ALL_CACHES.values().stream().forEach(cache -> cache.clear());
        ALL_CACHES.clear();
        log.traceExit();
    }
}
