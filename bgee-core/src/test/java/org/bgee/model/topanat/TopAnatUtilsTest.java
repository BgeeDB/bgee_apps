package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;

import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatUtils}, of the IDs and names of the conditions of topAnat: they
 * are written in the generated files and rebuilt from the call files, so both must agree.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class TopAnatUtilsTest extends TestAncestor {

    /**
     * Test {@link TopAnatUtils#conditionId(String, String)}.
     */
    @Test
    public void shouldGetConditionId() {
        assertEquals("Incorrect ID of a condition targeting a cell type",
                "CL:0000540-UBERON:0000955",
                TopAnatUtils.conditionId("UBERON:0000955", "CL:0000540"));
        //The root of the cell types and no cell type at all give the same ID
        assertEquals("Incorrect ID of a condition targeting the root of the cell types",
                "UBERON:0000955",
                TopAnatUtils.conditionId("UBERON:0000955", ConditionDAO.CELL_TYPE_ROOT_ID));
        assertEquals("Incorrect ID of a condition targeting no cell type",
                "UBERON:0000955", TopAnatUtils.conditionId("UBERON:0000955", null));
    }

    /**
     * Test {@link TopAnatUtils#conditionName(AnatEntity, AnatEntity)}.
     */
    @Test
    public void shouldGetConditionName() {
        AnatEntity brain = new AnatEntity("UBERON:0000955", "brain", null);
        assertEquals("Incorrect name of a condition targeting a cell type",
                "neuron in brain", TopAnatUtils.conditionName(brain,
                        new AnatEntity("CL:0000540", "neuron", null)));
        assertEquals("Incorrect name of a condition targeting the root of the cell types",
                "brain", TopAnatUtils.conditionName(brain, new AnatEntity(
                        ConditionDAO.CELL_TYPE_ROOT_ID, "cellular_component", null)));
        assertEquals("Incorrect name of a condition targeting no cell type",
                "brain", TopAnatUtils.conditionName(brain, null));
    }
}
