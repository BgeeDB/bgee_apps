package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatCallFileService}, of the columns of the generated files: topAnat
 * reads them by name, so a change of their number, their name or their order breaks it.
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
}
