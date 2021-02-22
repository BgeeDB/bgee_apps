package org.bgee.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of static convenient methods.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jan. 2016
 * @since Bgee 13 Jan. 2016
 *
 */
public class BgeeUtils {
    private final static Logger log = LogManager.getLogger(BgeeUtils.class.getName());

    /**
     * Returns an unmodifiable view of a new {@code List} initialized from the specified {@code Collection}. 
     * This is a shallow copy operation. The returned {@code List} will be empty if {@code c} is {@code null}.
     * 
     * @param c     A {@code Collection} to convert. Can be {@code null}.
     * @return      A new and unmodifiable {@code List} initialized from {@code c}.
     */
    public static <T> List<T> toList(Collection<T> c) {
        log.entry(c);
        return log.traceExit(Collections.unmodifiableList(c == null? new ArrayList<>(): new ArrayList<>(c)));
    }
    /**
     * Returns an unmodifiable view of a new {@code Set} initialized from the specified {@code Collection}. 
     * This is a shallow copy operation. The returned {@code Set} will be empty if {@code c} is {@code null}.
     * 
     * @param c     A {@code Collection} to convert. Can be {@code null}.
     * @return      A new and unmodifiable {@code Set} initialized from {@code c}.
     */
    public static <T> Set<T> toSet(Collection<T> c) {
        log.entry(c);
        return log.traceExit(Collections.unmodifiableSet(c == null? new HashSet<>(): new HashSet<>(c)));
    }
    /**
     * Returns an unmodifiable view of a new {@code Map} initialized from the specified {@code Map}. 
     * This is a shallow copy operation. The returned {@code Map} will be empty if {@code m} is {@code null}.
     * 
     * @param m     A {@code Map} to convert. Can be {@code null}.
     * @return      A new and unmodifiable {@code Map} initialized from {@code m}.
     */
    public static <T, U> Map<T, U> toMap(Map<T, U> m) {
        log.entry(m);
        return log.traceExit(Collections.unmodifiableMap(m == null? new HashMap<>(): new HashMap<>(m)));
    }
}
