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
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
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
 * A condition may carry only an anatomical entity, or only a cell type. The filter has to
 * resolve the missing member to the root of that parameter, which is how an organ-level call
 * satisfies {@code cell_type_id=SUMMARY}. The filter also discards the calls of the sexes
 * and strains retrieved only to be propagated to their root.
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
    public void shouldKeepOrganLevelCallsWhenTheCellTypeIsTheRoot() {
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
        assertTrue("An organ-level call, stored with no cell type, must be kept",
                keep.test(call(brain)));
        assertFalse("A cell type outside the requested organ must be rejected",
                keep.test(call(neuron)));
    }

    /**
     * Requesting the root of sexes or of strains retrieves the conditions of all their values,
     * for their observations to be propagated to the root: only the calls of the requested values
     * must be kept. The request may spell a value with another case, as the database does not
     * mind it when retrieving the conditions.
     */
    @Test
    public void shouldKeepOnlyTheRequestedSexesAndStrains() {
        Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParams = new HashMap<>();
        condParams.put(ConditionParameter.SEX, new ComposedFilterIds<>(
                new FilterIds<>(Set.of("ANY"), false)));
        condParams.put(ConditionParameter.STRAIN, new ComposedFilterIds<>(
                new FilterIds<>(Set.of(ConditionDAO.STRAIN_ROOT_ID), false)));
        ConditionFilter2 filter = new ConditionFilter2(SPECIES.getId(), condParams,
                Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE, ConditionParameter.SEX,
                        ConditionParameter.STRAIN), null, false);
        Predicate<OTFExpressionCall> keep = OTFExpressionCallFilterEngine.compile(Set.of(filter));

        assertTrue("The call of the requested sex and strain must be kept",
                keep.test(call(ConditionDAO.SEX_ROOT_ID, ConditionDAO.STRAIN_ROOT_ID)));
        assertFalse("The call of a sex below the requested root must be rejected",
                keep.test(call("female", ConditionDAO.STRAIN_ROOT_ID)));
        assertFalse("The call of a strain below the requested root must be rejected",
                keep.test(call(ConditionDAO.SEX_ROOT_ID, "C57BL/6J")));
    }

    private static OTFExpressionCall call(String sexId, String strainId) {
        Map<ConditionParameter<?, ?>, ComposedEntity<?>> params = new HashMap<>();
        params.put(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                new ComposedEntity<>(new AnatEntity(BRAIN, null, null, false), AnatEntity.class));
        params.put(ConditionParameter.SEX, new ComposedEntity<>(new Sex(sexId), Sex.class));
        params.put(ConditionParameter.STRAIN,
                new ComposedEntity<>(new Strain(strainId), Strain.class));
        return new OTFExpressionCall(GENE, new Condition2(params, SPECIES), null,
                null, null, null, null, 1,
                null, null, null, null, null, null, null);
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
