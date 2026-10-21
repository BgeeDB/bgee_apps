package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.CommandExpressionSupport.ExprCallCondPartProcessingCacheKey;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link CommandExpressionSupport.ExprCallCondPartProcessingCacheKey}.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class CommandExpressionSupportTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CommandExpressionSupportTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Requests with no condition value over all the condition parameters have no condition
     * filter at all: the key of the cache of the processed condition parts must still tell
     * their species and their combination of condition parameters apart.
     */
    @Test
    public void shouldDistinguishRequestsWithNoConditionFilter() {
        //Built with its target type spelled out: allOf() returns
        //ConditionParameter<? extends NamedEntity<?>, ?>, not ConditionParameter<?, ?>
        Set<ConditionParameter<?, ?>> allCondParams = new HashSet<>(ConditionParameter.allOf());
        Set<ConditionParameter<?, ?>> anatEntity = Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE);

        ExprCallCondPartProcessingCacheKey fly = key(7227, "FBgn0000018", allCondParams);
        ExprCallCondPartProcessingCacheKey zebrafish = key(7955, "ENSDARG00000037870",
                allCondParams);
        assertEquals("Both requests should have no condition filter",
                fly.getCondFilters(), zebrafish.getCondFilters());
        assertNotEquals("Requests of different species must not share a cache entry",
                fly, zebrafish);
        assertNotEquals("Requests over different combinations must not share a cache entry",
                fly, key(7227, "FBgn0000018", anatEntity));

        ExprCallCondPartProcessingCacheKey otherGene = key(7227, "FBgn0000001", allCondParams);
        assertEquals("Requests of a same species over a same combination must share a cache entry",
                fly, otherGene);
        assertEquals("Equal keys must have the same hash code", fly.hashCode(), otherGene.hashCode());
    }

    private static ExprCallCondPartProcessingCacheKey key(int speciesId, String geneId,
            Set<ConditionParameter<?, ?>> condParams) {
        return new ExprCallCondPartProcessingCacheKey(new ExpressionCallFilter2(
                ExpressionCallFilter2.ALL_CALLS, new GeneFilter(speciesId, geneId), null, null,
                condParams, null, null, false));
    }
}
