package org.bgee.controller.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to be used as as LRU cache. See
 * {@link http://java-planet.blogspot.com/2005/08/how-to-set-up-simple-lru-cache-using.html}
 * for explanation about the implementation,
 * and {@link https://stackoverflow.com/a/3355399} about using it with the {@code Map} view
 * returned by {@code Collections.synchronizedMap}.
 * <p>
 * Maybe we should use Guava cache instead.
 *
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 4463692925656926922L;

    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity + 1, 1.1f, true);
        this.capacity = capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}