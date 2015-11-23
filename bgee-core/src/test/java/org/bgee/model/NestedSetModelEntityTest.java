package org.bgee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.DevStage;
import org.junit.Test;

/**
 * Unit tests for the class {@link NestedSetModelEntity}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Nov. 2015
 */
public class NestedSetModelEntityTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(NestedSetModelEntityTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 

    /**
     * Test the implementation of {@code Comparable} in {@link NestedSetModelEntity}s.
     */
    @Test
    public void shouldCompareEntities() {
        DevStage stage1 = new DevStage("ID1", "stage1", null, 1, 5, 1, false, true);
        DevStage stage2 = new DevStage("ID2", "stage2", null, 2, 3, 2, true, false);
        DevStage stage3 = new DevStage("ID3", "stage3", null, 1, 3, 2, true, false);
        
        assertEquals("Incorrect comparison of NestedSetModelEntity", -1, stage1.compareTo(stage2));
        assertEquals("Incorrect comparison of NestedSetModelEntity", 0, stage1.compareTo(stage3));
        assertEquals("Incorrect comparison of NestedSetModelEntity", 1, stage2.compareTo(stage1));
        assertEquals("Incorrect comparison of NestedSetModelEntity", 1, stage2.compareTo(stage3));
        
        List<DevStage> orderedStages = Arrays.asList(stage2, stage3);
        Collections.sort(orderedStages);
        assertEquals("Incorrect ordering of List based on natural order", 
                Arrays.asList(stage3, stage2), orderedStages);
        
        DevStage stage4 = new DevStage("ID4", "stage4", null, 0, 3, 2, true, false);
        try {
            stage1.compareTo(stage4);
            fail("An Exception should be thrown when trying to order a NestedSetModelEntity "
                    + "with no left bound defined.");
        } catch (IllegalStateException e) {
            //test passed
        }
    }
}
