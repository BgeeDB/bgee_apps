package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Unit tests for {@link ConditionFilter}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, May 2016
 */
public class ConditionFilterTest extends TestAncestor {
    
    /**
     * Test the method {@link ConditionFilter#test(Condition)}.
     */
    @Test
    public void shoudTest() {
        Condition condition1 = new Condition(1, "ae1", "ds1", "spId");   // kept by filter 1
        Condition condition2 = new Condition(2, "ae2", "ds1", "spId");   // kept by filter 1
        Condition condition3 = new Condition(3, "ae2", "ds2", "spId");   // kept by filter 1
        Condition condition4 = new Condition(4, "ae4", "ds3", "spId");   // kept by filter 2
        Condition condition5 = new Condition(5, "ae5", "ds3", "spId");   // kept by filter 2
        Condition condition6 = new Condition(6, "ae3", "ds1", "spId");   // kept by filter 3
        Condition condition7 = new Condition(7, "ae5", "ds4", "spId");   // not kept even if ae5 is in filter 1
        Condition condition8 = new Condition(8, "ae4", "ds5", "spId");   // not kept even if ds5 is in filter 1
        Condition condition9 = new Condition(9, "ae6", "ds6", "spId");   // not kept by any filter
        Condition condition10 = new Condition(10, "ae5", null, "spId");   // not kept by any filter
        Condition condition11 = new Condition(11, null, "ds5", "spId");   // not kept by any filter
        
        Set<String> anatEntitieIds = new HashSet<>(Arrays.asList("ae1", "ae2", "ae5"));
        Set<String> devStageIds = new HashSet<>(Arrays.asList("ds1", "ds2", "ds5"));
        ConditionFilter conditionFilter = new ConditionFilter(anatEntitieIds, devStageIds);
        assertTrue(conditionFilter.test(condition1));
        assertTrue(conditionFilter.test(condition2));
        assertTrue(conditionFilter.test(condition3));
        assertFalse(conditionFilter.test(condition4));
        assertFalse(conditionFilter.test(condition5));
        assertFalse(conditionFilter.test(condition6));
        assertFalse(conditionFilter.test(condition7));
        assertFalse(conditionFilter.test(condition8));
        assertFalse(conditionFilter.test(condition9));
        assertTrue(conditionFilter.test(condition10));
        assertTrue(conditionFilter.test(condition11));

        conditionFilter = new ConditionFilter(anatEntitieIds, null);
        assertTrue(conditionFilter.test(condition1));
        assertTrue(conditionFilter.test(condition2));
        assertTrue(conditionFilter.test(condition3));
        assertFalse(conditionFilter.test(condition4));
        assertTrue(conditionFilter.test(condition5));
        assertFalse(conditionFilter.test(condition6));
        assertTrue(conditionFilter.test(condition7));
        assertFalse(conditionFilter.test(condition8));
        assertFalse(conditionFilter.test(condition9));
        assertTrue(conditionFilter.test(condition10));
        assertTrue(conditionFilter.test(condition11));

        anatEntitieIds = new HashSet<>(Arrays.asList("ae3"));
        conditionFilter = new ConditionFilter(anatEntitieIds, null);
        assertFalse(conditionFilter.test(condition1));
        assertFalse(conditionFilter.test(condition2));
        assertFalse(conditionFilter.test(condition3));
        assertFalse(conditionFilter.test(condition4));
        assertFalse(conditionFilter.test(condition5));
        assertTrue(conditionFilter.test(condition6));
        assertFalse(conditionFilter.test(condition7));
        assertFalse(conditionFilter.test(condition8));
        assertFalse(conditionFilter.test(condition9));
        assertFalse(conditionFilter.test(condition10));
        assertTrue(conditionFilter.test(condition11));

        devStageIds = new HashSet<>(Arrays.asList("ds3"));
        conditionFilter = new ConditionFilter(null, devStageIds);
        assertFalse(conditionFilter.test(condition1));
        assertFalse(conditionFilter.test(condition2));
        assertFalse(conditionFilter.test(condition3));
        assertTrue(conditionFilter.test(condition4));
        assertTrue(conditionFilter.test(condition5));
        assertFalse(conditionFilter.test(condition6));
        assertFalse(conditionFilter.test(condition7));
        assertFalse(conditionFilter.test(condition8));
        assertFalse(conditionFilter.test(condition9));
        assertTrue(conditionFilter.test(condition10));
        assertFalse(conditionFilter.test(condition11));
    }
}
