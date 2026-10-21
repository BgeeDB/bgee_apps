package org.bgee.model.expressiondata.call;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.bgee.model.ComposedEntity;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.BaseConditionFilter2.ComposedFilterIds;
import org.bgee.model.expressiondata.BaseConditionFilter2.FilterIds;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link OTFExpressionCallFilterEngine#compile(Set)}.
 * <p>
 * A missing anatomical entity or cell type rejects the call. A composed value with a single
 * member is both, because the anatomical entity and the cell type collapse when they are
 * the same term.
 */
public class OTFExpressionCallFilterEngineTest extends TestAncestor {

    private static final String BRAIN = "UBERON:0000955";
    private static final Species SPECIES = new Species(9606);
    private static final Gene GENE = new Gene("ENSG00000170178", SPECIES, new GeneBioType("protein_coding"));

    /**
     * The shape of {@code anat_entity_id=SUMMARY} together with {@code cell_type_id=SUMMARY}:
     * one organ, exact match, and the cell-type root.
     */
    @Test
    public void shouldKeepACallOnlyWhenBothTheOrganAndTheCellTypeRootArePresent() {
        Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParams = new HashMap<>();
        condParams.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE, new ComposedFilterIds<>(List.of(
                new FilterIds<>(Set.of(BRAIN), false),
                new FilterIds<>(Set.of(ConditionDAO.CELL_TYPE_ROOT_ID), false))));
        ConditionFilter2 filter = new ConditionFilter2(SPECIES.getId(), condParams,
                Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE), null, false);
        Predicate<OTFExpressionCall> keep = OTFExpressionCallFilterEngine.compile(Set.of(filter));

        AnatEntity brain = new AnatEntity(BRAIN, null, null, false);
        AnatEntity cellRoot = new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID, null, null, true);
        AnatEntity neuron = new AnatEntity("CL:0000540", null, null, true);

        assertTrue("A call in the organ and the cell-type root must be kept",
                keep.test(call(cellRoot, brain)));
        assertFalse("A single member is both the anatomical entity and the cell type, "
                + "so an organ stored alone is not the cell-type root",
                keep.test(call(brain)));
        assertFalse("A cell type outside the requested organ must be rejected",
                keep.test(call(neuron)));
        assertFalse("A call with no anatomical entity and no cell type must be rejected",
                keep.test(call()));
    }

    private static OTFExpressionCall call(AnatEntity... entities) {
        LinkedHashSet<AnatEntity> members = new LinkedHashSet<>();
        for (AnatEntity entity : entities) {
            members.add(entity);
        }
        Map<ConditionParameter<?, ?>, ComposedEntity<?>> params = new HashMap<>();
        params.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                new ComposedEntity<>(members, AnatEntity.class));
        return new OTFExpressionCall(GENE, new Condition2(params, SPECIES), null,
                null, null, null, null, 1,
                null, null, null, null, null, null, null);
    }
}
