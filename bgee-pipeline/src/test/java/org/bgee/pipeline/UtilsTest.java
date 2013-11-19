package org.bgee.pipeline;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * Unit tests for {@link Utils}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UtilsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UtilsTest.class.getName());

    /**
     * Default Constructor. 
     */
    public UtilsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Tests {@link Utils.getSpeciesIds(String)}.
     */
    @Test
    public void shouldGetSpeciesIds() throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        Set<String> speciesIds = Utils.getTaxonIds(
                this.getClass().getResource("/species/species.tsv").getFile());
        assertTrue("Incorrect species IDs returned", speciesIds.size() == 4 && 
                speciesIds.contains("NCBITaxon:8") && speciesIds.contains("NCBITaxon:13") && 
                speciesIds.contains("NCBITaxon:15") && speciesIds.contains("NCBITaxon:1001"));
    }
    

    /**
     * Tests {@link Utils.parseColumnAsString(String, int, int, CellProcessor)}.
     */
    @Test
    public void shouldParseColumnAsString() throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        CellProcessor processor = new NotNull(new UniqueHashCode());
        List<String> values = Utils.parseColumnAsString(
                this.getClass().getResource("/utils/tsvTestFile.tsv").getFile(), 
                1, 3, processor);
        assertEquals("Incorrect values returned", 3, values.size());
        assertEquals("Incorrect values returned", "b1", values.get(0));
        assertEquals("Incorrect values returned", "b2", values.get(1));
        assertEquals("Incorrect values returned", "b3", values.get(2));
    }
    
}
