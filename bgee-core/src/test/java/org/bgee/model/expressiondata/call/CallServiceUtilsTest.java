package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter2;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.junit.Test;

/**
 * Unit tests for {@link CallServiceUtils}.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class CallServiceUtilsTest extends TestAncestor {

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

        for (Set<ConditionParameter<?, ?>> combination: Set.of(
                Set.copyOf(ConditionParameter.allOf()), Set.<ConditionParameter<?, ?>>of())) {
            DAOConditionFilter2 daoFilter = utils.convertConditionFiltersToDAOConditionFilters(
                    null, null, null, Set.of(9606), combination).iterator().next();
            assertTrue("Nothing should be restricted for " + combination,
                    daoFilter.getAnatEntityIds().isEmpty() && daoFilter.getCellTypeIds().isEmpty()
                    && daoFilter.getDevStageIds().isEmpty() && daoFilter.getSexIds().isEmpty()
                    && daoFilter.getStrainIds().isEmpty());
        }
    }
}
