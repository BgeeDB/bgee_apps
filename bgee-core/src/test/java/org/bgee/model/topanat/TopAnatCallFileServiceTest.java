package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgee.model.ComposedEntity;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatCallFileService}, of the columns of the generated files: topAnat
 * reads them by name, so a change of their number, their name or their order breaks it. And of
 * the content of the files describing the conditions, on a small graph.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class TopAnatCallFileServiceTest extends TestAncestor {

    /**
     * Test {@link TopAnatCallFileService#dataTypeCombinations()}.
     */
    @Test
    public void shouldGetDataTypeCombinations() {
        List<EnumSet<DataType>> combinations = TopAnatCallFileService.dataTypeCombinations();

        //Every non-empty combination of the data types, so 2^n - 1 of them
        assertEquals("Incorrect number of data type combinations",
                (1 << DataType.values().length) - 1, combinations.size());
        assertEquals("The combinations should be distinct",
                combinations.size(), combinations.stream().distinct().count());
        assertEquals("The combination of all data types should be the last one",
                EnumSet.allOf(DataType.class), combinations.get(combinations.size() - 1));
        //Ordered by increasing number of data types
        for (int i = 1; i < combinations.size(); i++) {
            assertEquals("The combinations should be ordered by increasing size", true,
                    combinations.get(i - 1).size() <= combinations.get(i).size());
        }
    }

    /**
     * Test {@link TopAnatCallFileService#combinationColumnName(EnumSet)}.
     */
    @Test
    public void shouldGetCombinationColumnName() {
        assertEquals("Incorrect column name of a single data type",
                "RNA_SEQ",
                TopAnatCallFileService.combinationColumnName(EnumSet.of(DataType.RNA_SEQ)));
        //The separator must not be one that the names of the data types contain, otherwise
        //a combination could not be read back unambiguously
        assertEquals("Incorrect column name of a combination of data types",
                "SC_RNA_SEQ-RNA_SEQ",
                TopAnatCallFileService.combinationColumnName(
                        EnumSet.of(DataType.SC_RNA_SEQ, DataType.RNA_SEQ)));
        assertEquals("The column names of the combinations should all be distinct",
                TopAnatCallFileService.dataTypeCombinations().size(),
                TopAnatCallFileService.dataTypeCombinations().stream()
                        .map(TopAnatCallFileService::combinationColumnName)
                        .collect(Collectors.toSet()).size());
    }

    private final static AnatEntity ROOT = new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID,
            "anatomical entity", null);
    private final static AnatEntity BRAIN = new AnatEntity("UBERON:0000955", "brain", null);
    private final static AnatEntity CELL_TYPE_ROOT = new AnatEntity(
            ConditionDAO.CELL_TYPE_ROOT_ID, "cellular_component", null);
    private final static AnatEntity NEURON = new AnatEntity("CL:0000540", "neuron", null);
    private final static AnatEntity CELL = new AnatEntity("CL:0000000", "cell", null);

    /**
     * @return  A condition of topAnat built as the conditions loaded from the database:
     *          the cell type first, then the anat. entity.
     */
    private static Condition2 condition(AnatEntity anatEntity, AnatEntity cellType) {
        LinkedHashSet<AnatEntity> entities = new LinkedHashSet<>();
        if (cellType != null) {
            entities.add(cellType);
        }
        entities.add(anatEntity);
        return new Condition2(Map.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE,
                new ComposedEntity<>(entities, AnatEntity.class)), new Species(1));
    }
    /**
     * @return  The conditions of a small graph, by internal ID: the root, the brain, the neurons,
     *          and the neurons of the brain.
     */
    private static Map<Integer, Condition2> conditions() {
        return Map.of(1, condition(ROOT, CELL_TYPE_ROOT), 2, condition(BRAIN, CELL_TYPE_ROOT),
                3, condition(ROOT, NEURON), 4, condition(BRAIN, NEURON));
    }
    /**
     * @return  The direct ancestors of the conditions of {@link #conditions()}, plus those of
     *          a condition of another combination of condition parameters.
     */
    private static Map<Integer, int[]> directAncestors() {
        return Map.of(1, new int[0], 2, new int[] {1}, 3, new int[] {1}, 4, new int[] {2, 3},
                5, new int[] {4});
    }

    /**
     * Test {@link TopAnatCallFileService#conditionLines(java.util.Collection)}.
     */
    @Test
    public void shouldGetConditionLines() {
        assertEquals("Incorrect lines of the condition file", List.of(
                "BGEE:0000000\tanatomical entity",
                "CL:0000540-BGEE:0000000\tneuron in anatomical entity",
                "CL:0000540-UBERON:0000955\tneuron in brain",
                "UBERON:0000955\tbrain"),
                TopAnatCallFileService.conditionLines(conditions().values()));

        //A cell type that is the anat. entity itself is held as a single entity by the condition,
        //it must not be confused with the anat. entity alone
        assertEquals("Incorrect lines of a cell type that is the anat. entity itself", List.of(
                "CL:0000000\tcell",
                "CL:0000000-CL:0000000\tcell in cell"),
                TopAnatCallFileService.conditionLines(
                        List.of(condition(CELL, CELL_TYPE_ROOT), condition(CELL, CELL))));

        assertThrows("Two conditions with the same ID should be rejected",
                IllegalStateException.class, () -> TopAnatCallFileService.conditionLines(List.of(
                        condition(BRAIN, CELL_TYPE_ROOT), condition(BRAIN, CELL_TYPE_ROOT))));
    }

    /**
     * Test {@link TopAnatCallFileService#relationLines(Map, Map)}.
     */
    @Test
    public void shouldGetRelationLines() {
        //The ancestors of the condition of another combination are ignored
        assertEquals("Incorrect lines of the relation file", List.of(
                "CL:0000540-BGEE:0000000\tBGEE:0000000",
                "CL:0000540-UBERON:0000955\tCL:0000540-BGEE:0000000",
                "CL:0000540-UBERON:0000955\tUBERON:0000955",
                "UBERON:0000955\tBGEE:0000000"),
                TopAnatCallFileService.relationLines(conditions(), directAncestors()));

        assertThrows("A parent that is not one of the conditions should be rejected",
                IllegalStateException.class, () -> TopAnatCallFileService.relationLines(
                        Map.of(2, condition(BRAIN, CELL_TYPE_ROOT)), directAncestors()));
        assertThrows("Conditions with several roots should be rejected",
                IllegalStateException.class, () -> TopAnatCallFileService.relationLines(
                        Map.of(1, condition(ROOT, CELL_TYPE_ROOT),
                                2, condition(BRAIN, CELL_TYPE_ROOT)),
                        Map.of(1, new int[0], 2, new int[0])));
    }
}
