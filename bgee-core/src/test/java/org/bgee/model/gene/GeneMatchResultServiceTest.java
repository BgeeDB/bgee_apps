package org.bgee.model.gene;

import org.bgee.model.TestAncestor;
import org.bgee.model.species.Species;
import org.junit.Test;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class holds the unit tests for the {@code GeneMatchResultService} class.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @see     GeneMatchResult
 * @since   Bgee 14, Apr. 2019
 */
public class GeneMatchResultServiceTest extends TestAncestor {

    /**
     * Test {@link GeneMatchResultService#autocomplete(String, int)}.
     */
    @Test
    public void shouldAutocomplete() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 3;
        sphinxResult.attrNames = new String[]{"hit"};
        SphinxMatch sphinxMatch1 = new SphinxMatch(1, 10);
        sphinxMatch1.attrValues = new ArrayList();
        sphinxMatch1.attrValues.add("ENSG01");
        SphinxMatch sphinxMatch2 = new SphinxMatch(2, 5);
        sphinxMatch2.attrValues = new ArrayList();
        sphinxMatch2.attrValues.add("ENSG02");
        SphinxMatch sphinxMatch3 = new SphinxMatch(3, 1);
        sphinxMatch3.attrValues = new ArrayList();
        sphinxMatch3.attrValues.add("ENSG03");
        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1, sphinxMatch2, sphinxMatch3};

        String term = "ENSG";
        when(sphinxClient.Query(term, "bgee_autocomplete")).thenReturn(sphinxResult);

        GeneMatchResultService service = new GeneMatchResultService(sphinxClient);
        List<String> autocompleteResult = service.autocomplete(term, 100);

        assertNotNull(autocompleteResult);
        assertEquals(Arrays.asList("ENSG01", "ENSG02", "ENSG03"), autocompleteResult);
    }

    /**
     * Test {@link GeneMatchResultService#searchByTerm(String, Collection, int, int)}.
     */
    @Test
    public void shouldFindByTerm() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 1;
        sphinxResult.attrNames = new String[]{
                "bgeegeneid", "geneid", "genename", "genedescription",
                "genenamesynonym", "genexref", "speciesid", "genemappedtogeneidcount",
                "speciesgenus", "speciesname", "speciescommonname", "speciesdisplayorder"};
        SphinxMatch sphinxMatch1 = new SphinxMatch(1, 1);
        sphinxMatch1.attrValues = new ArrayList();
        sphinxMatch1.attrValues.add(86L);           //bgeeGeneId
        sphinxMatch1.attrValues.add("ENSG0086");    //geneId
        sphinxMatch1.attrValues.add("Name1");       //geneName
        sphinxMatch1.attrValues.add("Desc1");       //geneDescription
        sphinxMatch1.attrValues.add("Syn1||Syn2||Syn3");        //geneNameSynonym
        sphinxMatch1.attrValues.add("xref_1||xref:2||xref.3");  //geneXRef
        sphinxMatch1.attrValues.add(11L);           //speciesId
        sphinxMatch1.attrValues.add(1L);            //geneMappedToGeneIdCount
        sphinxMatch1.attrValues.add("Homo");        //speciesGenus
        sphinxMatch1.attrValues.add("sapiens");     //speciesName
        sphinxMatch1.attrValues.add("human");       //speciesCommonName
        sphinxMatch1.attrValues.add(1L);            //speciesDisplayOrder

        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1};

        String term = "Syn2";
        when(sphinxClient.Query(term, "bgee_genes")).thenReturn(sphinxResult);

        GeneMatchResultService service = new GeneMatchResultService(sphinxClient);
        GeneMatchResult geneMatchResult = service.searchByTerm(term, null, 0, 100);

        assertEquals(1, geneMatchResult.getTotalMatchCount());
        assertNotNull(geneMatchResult.getGeneMatches());
        assertEquals(1, geneMatchResult.getGeneMatches().size());
        
        Species species = new Species(11, "human", null,"Homo", "sapiens", null, null, null, null, null, 1);
        Gene g = new Gene("ENSG0086", "Name1", "Desc1", Arrays.asList("Syn1", "Syn2", "Syn3"), null,
                species, 1);
        
        assertEquals(new GeneMatch(g, "syn2", GeneMatch.MatchSource.SYNONYM), geneMatchResult.getGeneMatches().get(0));
    }

    /**
     * Test {@link GeneMatchResultService#searchByTerm(String, Collection, int, int)} 
     * of a search returning no result.
     */
    @Test
    public void shouldFindByTerm_noResult() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        String term = "XXX";

        when(sphinxClient.Query(term, "bgee_genes")).thenReturn(null);

        GeneMatchResultService service = new GeneMatchResultService(sphinxClient);
        GeneMatchResult geneMatchResult = service.searchByTerm(term, null, 0, 100);

        assertEquals(0, geneMatchResult.getTotalMatchCount());
        assertNull(geneMatchResult.getGeneMatches());
    }
}
