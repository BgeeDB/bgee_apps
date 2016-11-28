package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.bgee.model.TestAncestor;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link GeneFilter}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2016
 * @since   Bgee 13, Nov. 2016
 */
public class GeneFilterTest extends TestAncestor {
    
    @Test
    public void shouldTest() {
        String geneId = "gA";
        
        GeneFilter filter = new GeneFilter(geneId);
        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(geneId));
        assertFalse("Gene ID should not pass filter", filter.test("gB"));
        
        filter = new GeneFilter(Arrays.asList(geneId, "g2"));
        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(geneId));
        assertFalse("Gene ID should not pass filter", filter.test("gB"));
    }
}
