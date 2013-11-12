package org.bgee.pipeline;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

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
        Set<Integer> speciesIds = Utils.getSpeciesIds(
                this.getClass().getResource("/species/species.tsv").getFile());
        assertTrue("Incorrect species IDs returned", speciesIds.size() == 4 && 
                speciesIds.contains(8) && speciesIds.contains(13) && 
                speciesIds.contains(15) && speciesIds.contains(1001));
    }
    
}
