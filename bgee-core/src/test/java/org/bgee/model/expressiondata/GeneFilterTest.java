package org.bgee.model.expressiondata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.bgee.model.TestAncestor;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link GeneFilter}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Nov. 2016
 */
public class GeneFilterTest extends TestAncestor {
    
    @Test
    public void shouldTest() {
        Gene gene = new Gene("gA", new Species(1));
        
        // Test filtering on gene ID only
        GeneFilter filter = new GeneFilter(1, gene.getEnsemblGeneId());
//        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(gene));
        assertFalse("Gene ID should not pass filter", filter.test(new Gene("gB", new Species(1))));
        
        // Test filtering on gene IDs only
        filter = new GeneFilter(1, Arrays.asList(gene.getEnsemblGeneId(), "g2"));
//        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Gene ID should pass filter", filter.test(gene));
        assertFalse("Gene ID should not pass filter", filter.test(new Gene("gB", new Species(1))));
        
        // Test filtering on gene and species IDs
        filter = new GeneFilter(1, Arrays.asList(gene.getEnsemblGeneId(), "g2"));
//        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Species ID should pass filter", filter.test(gene));
        assertFalse("Species ID should not pass filter", filter.test(new Gene("gA", new Species(2))));
        assertFalse("Gene ID should not pass filter", filter.test(new Gene("gB", new Species(1))));

        // Test filtering on species ID only
        filter = new GeneFilter(1);
//        assertTrue("Null should pass filter", filter.test(null));
        assertTrue("Species ID should pass filter", filter.test(gene));
        assertFalse("Species ID should not pass filter", filter.test(new Gene("gA", new Species(2))));
    }
}
