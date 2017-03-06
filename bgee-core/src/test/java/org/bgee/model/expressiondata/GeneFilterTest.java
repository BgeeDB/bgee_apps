package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.bgee.model.TestAncestor;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.junit.Test;

/**
 * Unit tests for {@link GeneFilter}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Nov. 2016
 */
public class GeneFilterTest extends TestAncestor {
    
    @Test
    public void shouldTest() {
        Gene gene = new Gene("gA");
        
        GeneFilter filter = new GeneFilter(gene.getEnsemblGeneId());
        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(gene));
        assertFalse("Gene ID should not pass filter", filter.test(new Gene("gB")));
        
        filter = new GeneFilter(Arrays.asList(gene.getEnsemblGeneId(), "g2"));
        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(gene));
        assertFalse("Gene ID should not pass filter", filter.test(new Gene("gB")));
    }
}
