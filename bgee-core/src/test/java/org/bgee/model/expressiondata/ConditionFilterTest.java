package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link ConditionFilter}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2017
 * @since   Bgee 13, May 2016
 */
public class ConditionFilterTest extends TestAncestor {
    
    /**
     * Test the method {@link ConditionFilter#test(Condition)}.
     */
    @Test
    public void shoudTest() {
        Condition condition1 = new Condition(new AnatEntity("ae1"), new DevStage("ds1"), new Species(1));   // kept by filter 1
        Condition condition2 = new Condition(new AnatEntity("ae2"), new DevStage("ds1"), new Species(1));   // kept by filter 1
        Condition condition3 = new Condition(new AnatEntity("ae2"), new DevStage("ds2"), new Species(1));   // kept by filter 1
        Condition condition4 = new Condition(new AnatEntity("ae4"), new DevStage("ds3"), new Species(1));   // kept by filter 2
        Condition condition5 = new Condition(new AnatEntity("ae5"), new DevStage("ds3"), new Species(1));   // kept by filter 2
        Condition condition6 = new Condition(new AnatEntity("ae3"), new DevStage("ds1"), new Species(1));   // kept by filter 3
        Condition condition7 = new Condition(new AnatEntity("ae5"), new DevStage("ds4"), new Species(1));   // not kept even if ae5 is in filter 1
        Condition condition8 = new Condition(new AnatEntity("ae4"), new DevStage("ds5"), new Species(1));   // not kept even if ds5 is in filter 1
        Condition condition9 = new Condition(new AnatEntity("ae6"), new DevStage("ds6"), new Species(1));   // not kept by any filter
        Condition condition10 = new Condition(new AnatEntity("ae5"), null, new Species(1));   // not kept by any filter
        Condition condition11 = new Condition(null, new DevStage("ds5"), new Species(1));   // not kept by any filter
        
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
