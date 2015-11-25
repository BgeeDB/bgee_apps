package org.bgee.model;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for the class {@link BgeeEnum}. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov. 2015
 * @since   Bgee 13, Nov. 2015
 */
public class BgeeEnumTest extends TestAncestor {

    /**
     * {@code Logger} of this class.
     */
    private final static Logger log = LogManager.getLogger(BgeeEnumTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 

    @Test
    public void shouldConvert() {
        assertEquals("Incorrect decorrelation type",
                null, BgeeEnum.convert(DecorrelationType.class, null));
        assertEquals("Incorrect decorrelation type",
                DecorrelationType.NONE, BgeeEnum.convert(DecorrelationType.class, "classic"));
        assertEquals("Incorrect decorrelation type",
                DecorrelationType.NONE, BgeeEnum.convert(DecorrelationType.class, "NONE"));
        try {
            BgeeEnum.convert(DecorrelationType.class, "fake");
            fail("An IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }

    @Test
    public void shouldConvertStringSetToEnumSet() {
        // Class<T> enumClass, Set<String> representations
        
        assertEquals("Incorrect decorrelation types",
                null, BgeeEnum.convertStringSetToEnumSet(DataQuality.class, null));
        
        Set<String> strings = new HashSet<>();
        assertEquals("Incorrect decorrelation types",
                null, BgeeEnum.convertStringSetToEnumSet(DataQuality.class, strings));

        Set<DataQuality> qualities = new HashSet<>();
        strings.add("HIGH");
        qualities.add(DataQuality.HIGH);
        assertEquals("Incorrect decorrelation types",
                qualities, BgeeEnum.convertStringSetToEnumSet(DataQuality.class, strings));

        strings.add("LOW");
        qualities.add(DataQuality.LOW);
        assertEquals("Incorrect decorrelation types",
                qualities, BgeeEnum.convertStringSetToEnumSet(DataQuality.class, strings));

        strings.add(null);
        qualities.add(null);
        assertEquals("Incorrect decorrelation types",
                qualities, BgeeEnum.convertStringSetToEnumSet(DataQuality.class, strings));
        
        strings.add("fake");
        try {
            BgeeEnum.convertStringSetToEnumSet(DecorrelationType.class, strings);
            fail("An IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }

    @Test
    public void shouldConvertEnumSetToStringSet() {
        // Set<T> enums
    }

    @Test
    public void shouldIsInEnum() {
        // Class<T> enumClass, String representation
    }

    @Test
    public void shouldAreAllInEnum() {
        // Class<T> enumClass, Set<String> representations
    }
}
