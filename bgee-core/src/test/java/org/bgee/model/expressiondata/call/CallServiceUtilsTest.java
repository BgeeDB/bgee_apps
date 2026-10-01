package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.junit.Test;

/**
 * Unit tests for {@link CallServiceUtils}: the restriction of the conditions retrieved to the
 * requested condition parameter combination, and the exclude-seed scoping used by
 * ExpressionMatrix {@code discard=SUMMARY} when expanding one SUMMARY organ.
 *
 * @author  Julien Wollbrett
 * @author  Harald Detering
 * @version Bgee 16
 */
public class CallServiceUtilsTest extends TestAncestor {

    private static final String ORGANISM = "UBERON:0000468";
    private static final String CNS = "UBERON:0001017";
    private static final String BRAIN = "UBERON:0000955";
    private static final String LEFTOVER = "leftoverUnderOrganism";
    private static final String DIGESTIVE = "UBERON:0001007";
    private static final String LIVER = "UBERON:0002107";
    private static final String ROOT = "UBERON:0001062";

    /**
     * organism
     *   ├── leftover
     *   ├── CNS
     *   │     └── brain
     *   └── digestive
     *         └── liver
     */
    private static final Map<String, Set<String>> DESCENDANT_MAP = Map.of(
            ROOT, Set.of(ORGANISM, LEFTOVER, CNS, BRAIN, DIGESTIVE, LIVER),
            ORGANISM, Set.of(LEFTOVER, CNS, BRAIN, DIGESTIVE, LIVER),
            CNS, Set.of(BRAIN),
            DIGESTIVE, Set.of(LIVER)
    );
    private static final Function<String, Set<String>> DESCENDANTS =
            id -> DESCENDANT_MAP.getOrDefault(id, Set.<String>of());

    //*************************************************************************
    // RESTRICTION TO THE REQUESTED CONDITION PARAMETER COMBINATION
    //*************************************************************************

    /**
     * When no {@code ConditionFilter2} is provided, the condition parameter combination is the
     * only information restricting the conditions retrieved, besides the species: the condition
     * parameters it does not contain must be restricted to their root. Without that restriction,
     * the conditions of every combination are retrieved, and the propagation then works over
     * conditions that can hold no data for the query.
     */
    @Test
    public void shouldRestrictToTheCondParamCombinationWithoutConditionFilter() {
        CallServiceUtils utils = new CallServiceUtils();

        //The combination used to export the EasyBgee expression calls: anat. entity/cell type
        //and dev. stage, so sex and strain must be restricted to their root.
        Set<DAOConditionFilter2> daoFilters = utils.convertConditionFiltersToDAOConditionFilters(
                null, null, null, Set.of(9606),
                Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE, ConditionParameter.DEV_STAGE));

        assertEquals("One filter should have been created", 1, daoFilters.size());
        DAOConditionFilter2 daoFilter = daoFilters.iterator().next();
        assertEquals("Incorrect species", Set.of(9606), daoFilter.getSpeciesIds());
        assertTrue("The requested anat. entity must not be restricted",
                daoFilter.getAnatEntityIds().isEmpty());
        assertTrue("The requested cell type must not be restricted",
                daoFilter.getCellTypeIds().isEmpty());
        assertTrue("The requested dev. stage must not be restricted",
                daoFilter.getDevStageIds().isEmpty());
        assertEquals("The sex was not requested, it must be restricted to its root",
                Set.of(ConditionDAO.SEX_ROOT_ID), daoFilter.getSexIds());
        assertEquals("The strain was not requested, it must be restricted to its root",
                Set.of(ConditionDAO.STRAIN_ROOT_ID), daoFilter.getStrainIds());
    }

    /**
     * Requesting all the condition parameters must restrict nothing, as before this restriction
     * was implemented.
     */
    @Test
    public void shouldNotRestrictWhenAllCondParamsRequested() {
        CallServiceUtils utils = new CallServiceUtils();

        //Both sets are built with their target type spelled out: allOf() returns
        //ConditionParameter<? extends NamedEntity<?>, ?>, which denotes the same types as
        //ConditionParameter<?, ?> without being the same type argument, and inferring a common
        //one inside a Set.of() is not accepted by every compiler.
        Set<ConditionParameter<?, ?>> allCondParams = new HashSet<>(ConditionParameter.allOf());
        Set<ConditionParameter<?, ?>> noCondParam = Set.of();
        for (Set<ConditionParameter<?, ?>> combination: List.of(allCondParams, noCondParam)) {
            DAOConditionFilter2 daoFilter = utils.convertConditionFiltersToDAOConditionFilters(
                    null, null, null, Set.of(9606), combination).iterator().next();
            assertTrue("Nothing should be restricted for " + combination,
                    daoFilter.getAnatEntityIds().isEmpty() && daoFilter.getCellTypeIds().isEmpty()
                    && daoFilter.getDevStageIds().isEmpty() && daoFilter.getSexIds().isEmpty()
                    && daoFilter.getStrainIds().isEmpty());
        }
    }

    //*************************************************************************
    // EXCLUDE-SEED SCOPING
    //*************************************************************************

    /**
     * Expanding multicellular organism with discard=SUMMARY must punch out other
     * SUMMARY subtrees (CNS/brain, digestive/liver) and keep leftover organs.
     */
    @Test
    public void shouldPunchOutSiblingSummarySubtreesWhenExpandingBucket() {
        FilterIds<String> filter = new FilterIds<>(
                Set.of(ORGANISM), true, Set.of(CNS, DIGESTIVE), null);

        assertEquals(Set.of(CNS, DIGESTIVE),
                CallServiceUtils.selectApplicableExcludeSeeds(filter, DESCENDANTS));

        Set<String> expanded = expandInclude(filter);
        expanded.removeAll(CallServiceUtils.expandApplicableExcludeIds(filter, DESCENDANTS));
        assertTrue(expanded.contains(ORGANISM));
        assertTrue(expanded.contains(LEFTOVER));
        assertFalse(expanded.contains(CNS));
        assertFalse(expanded.contains(BRAIN));
        assertFalse(expanded.contains(DIGESTIVE));
        assertFalse(expanded.contains(LIVER));
    }

    /**
     * Expanding CNS with discard=SUMMARY must not apply the ancestor bucket
     * (multicellular organism) or an unrelated SUMMARY organ (digestive system).
     * Neither is a descendant of CNS, so the CNS tree, including brain, stays.
     */
    @Test
    public void shouldSkipAncestorBucketWhenExpandingNestedSummaryTerm() {
        FilterIds<String> filter = new FilterIds<>(
                Set.of(CNS), true, Set.of(ORGANISM, DIGESTIVE), null);

        assertEquals(Set.of(),
                CallServiceUtils.selectApplicableExcludeSeeds(filter, DESCENDANTS));

        Set<String> expanded = expandInclude(filter);
        expanded.removeAll(CallServiceUtils.expandApplicableExcludeIds(filter, DESCENDANTS));
        assertTrue(expanded.contains(CNS));
        assertTrue(expanded.contains(BRAIN));
        assertFalse(expanded.contains(ORGANISM));
        assertFalse(expanded.contains(LEFTOVER));
        assertFalse(expanded.contains(DIGESTIVE));
        assertFalse(expanded.contains(LIVER));
    }

    /**
     * Complementary (include root + SUMMARY terms, discard the SUMMARY terms)
     * must still discard every non-root SUMMARY tree, including the bucket.
     */
    @Test
    public void shouldKeepComplementaryDiscardOfAllSummaryTrees() {
        FilterIds<String> filter = new FilterIds<>(
                Set.of(ROOT, ORGANISM, CNS, DIGESTIVE), true,
                Set.of(ORGANISM, CNS, DIGESTIVE), null);

        assertEquals(Set.of(ORGANISM, CNS, DIGESTIVE),
                CallServiceUtils.selectApplicableExcludeSeeds(filter, DESCENDANTS));

        Set<String> expanded = expandInclude(filter);
        expanded.removeAll(CallServiceUtils.expandApplicableExcludeIds(filter, DESCENDANTS));
        assertTrue(expanded.contains(ROOT));
        assertFalse(expanded.contains(ORGANISM));
        assertFalse(expanded.contains(CNS));
        assertFalse(expanded.contains(BRAIN));
        assertFalse(expanded.contains(DIGESTIVE));
        assertFalse(expanded.contains(LEFTOVER));
    }

    @Test
    public void shouldKeepAllExcludeSeedsWhenNoDescendantFunction() {
        FilterIds<String> filter = new FilterIds<>(
                Set.of(CNS), true, Set.of(ORGANISM, DIGESTIVE), null);
        assertEquals(Set.of(ORGANISM, DIGESTIVE),
                CallServiceUtils.expandApplicableExcludeIds(filter, null));
    }

    private static Set<String> expandInclude(FilterIds<String> filter) {
        HashSet<String> expanded = new HashSet<>(filter.getIds());
        for (String id : filter.getIds()) {
            expanded.addAll(DESCENDANTS.apply(id));
        }
        return expanded;
    }
}
