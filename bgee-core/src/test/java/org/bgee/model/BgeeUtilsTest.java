package org.bgee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Unit tests for {@link BgeeUtils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jan. 2016
 * @since Bgee 13 Jan. 2016
 */
public class BgeeUtilsTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(BgeeUtilsTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link BgeeUtils#toList(Collection)}.
     */
    @Test
    public void shouldConvertToList() {
        BgeeUtilsTest.<List<String>>testConvert(new ArrayList<>(Arrays.asList("a", null, "b")), 
                BgeeUtils::toList, c -> c.add("test add"), Collection::isEmpty);
    }
    /**
     * Test {@link BgeeUtils#toSet(Collection)}.
     */
    @Test
    public void shouldConvertToSet() {
        BgeeUtilsTest.<Set<String>>testConvert(new HashSet<>(Arrays.asList("a", null, "b")), 
                BgeeUtils::toSet, c -> c.add("test add"), Collection::isEmpty);
    }
    /**
     * Test {@link BgeeUtils#toMap(Map)}.
     */
    @Test
    public void shouldConvertToMap() {
        Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("key", "value");
        BgeeUtilsTest.<Map<String, String>>testConvert(sourceMap, 
                BgeeUtils::toMap, m -> m.put("newKey", "newValue"), Map::isEmpty);
    }
    /**
     * Tests the conversion method {@code convertFunc} on {@code source}.
     * @param source            The {@code T} to test the method on.
     * @param convertFunc       A {@code Function} that is the method to test.
     * @param modifyFunc        A {@code Consumer} that will be used to modify {@code T}s.
     * @param testEmptyFunc     A {@code Function} allowing to test that a {@code T} is empty.
     */
    private static <T> void testConvert(T source, Function<T, T> convertFunc, Consumer<T> modifyFunc, 
            Function<T, Boolean> testEmptyFunc) {
        log.entry(source, convertFunc, modifyFunc, testEmptyFunc);
        
        //test that a null argument returns a new empty Collection/Map.
        T nullCollection = convertFunc.apply(null);
        assertTrue("Incorrect Collection or Map generated from null", 
                nullCollection != null && testEmptyFunc.apply(nullCollection));
        
        T newCollection = convertFunc.apply(source);
        assertEquals("new Collection or Map not equal", source, newCollection);
        
        //Check that we cannot modify the new Collection/Map
        try {
            modifyFunc.accept(newCollection);
            //test failed
            throw log.throwing(new AssertionError("An exception should have been thrown."));
        } catch (UnsupportedOperationException e) {
            //test passed
        }
        
        //Check that modifying the source Collection/Map does not modify the new Collection
        modifyFunc.accept(source);
        assertNotEquals("Collections or Maps should not be equal after modification", source, newCollection);
        
        log.traceExit();
    }
}