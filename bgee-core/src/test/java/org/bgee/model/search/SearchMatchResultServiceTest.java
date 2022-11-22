package org.bgee.model.search;

import org.bgee.model.NamedEntity;
import org.bgee.model.TestAncestor;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneBioTypeTOResultSet;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.search.SearchMatch;
import org.bgee.model.search.SearchMatchResult;
import org.bgee.model.search.SearchMatchResultService;
import org.bgee.model.species.Species;
import org.junit.Before;
import org.junit.Test;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
 * @version Bgee 14, May 2019
 * @see     GeneMatchResult
 * @since   Bgee 14, Apr. 2019
 */
public class SearchMatchResultServiceTest extends TestAncestor {

    @Before
    public void loadCommonMocks() {
        GeneBioTypeTOResultSet mockBioTypeRs = getMockResultSet(GeneBioTypeTOResultSet.class,
                Arrays.asList(new GeneBioTypeTO(1, "type1"), new GeneBioTypeTO(2, "type2")));
        when(this.geneDAO.getGeneBioTypes()).thenReturn(mockBioTypeRs);
    }
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
        when(sphinxClient.Query("\"" + term + "\"", "autocomplete_index")).thenReturn(sphinxResult);

        SearchMatchResultService service = new SearchMatchResultService(sphinxClient, this.serviceFactory,
                "genes_index", "anat_entity_index", "strains_index", "autocomplete_index",
                GENE_BIO_TYPES);
        List<String> autocompleteResult = service.autocomplete(term, 100);

        assertNotNull(autocompleteResult);
        assertEquals(Arrays.asList("ENSG01", "ENSG02", "ENSG03"), autocompleteResult);
    }

    /**
     * Test {@link SearchMatchResultService#searchGenesByTerm(String, Collection, int, int)}.
     */
    @Test
    public void shouldFindGeneByTerm() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 1;
        sphinxResult.attrNames = new String[]{
                "bgeegeneid", "geneid", "genename", "genedescription",
                "genenamesynonym", "genexref", "speciesid", "genemappedtogeneidcount", "genebiotypeid",
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
        sphinxMatch1.attrValues.add(1L);            //geneBioTypeId
        sphinxMatch1.attrValues.add("Homo");        //speciesGenus
        sphinxMatch1.attrValues.add("sapiens");     //speciesName
        sphinxMatch1.attrValues.add("human");       //speciesCommonName
        sphinxMatch1.attrValues.add(1L);            //speciesDisplayOrder

        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1};

        String term = "Syn2";
        when(sphinxClient.Query("\"" + term + "\"", "genes_index")).thenReturn(sphinxResult);

        SearchMatchResultService service = new SearchMatchResultService(sphinxClient, this.serviceFactory,
                "genes_index", "anat_entities_index", "strains_index", "autocomplete_index",
                GENE_BIO_TYPES);
        SearchMatchResult<Gene> geneMatchResult = service.searchGenesByTerm(term, null, 0, 100);

        assertEquals(1, geneMatchResult.getTotalMatchCount());
        assertNotNull(geneMatchResult.getSearchMatches());
        assertEquals(1, geneMatchResult.getSearchMatches().size());
        
        Species expSpecies = new Species(11, "human", null, "Homo", "sapiens", null, null, null, null, null, null, null, 1);
        Gene expGene = new Gene("ENSG0086", "Name1", "Desc1", 
                new HashSet<>(Arrays.asList("Syn1", "Syn2", "Syn3")), null, expSpecies, new GeneBioType("type1"), 1);

        assertEquals(new SearchMatch<Gene>(expGene, "syn2", SearchMatch.MatchSource.SYNONYM, Gene.class),
                geneMatchResult.getSearchMatches().get(0));
        Gene actualGene = geneMatchResult.getSearchMatches().get(0).getSearchedObject();
        Species actualSpecies = actualGene.getSpecies();
        assertEquals(expSpecies.getId(), actualSpecies.getId());
        assertEquals(expSpecies.getName(), actualSpecies.getName());
        assertEquals(expSpecies.getGenus(), actualSpecies.getGenus());
        assertEquals(expSpecies.getSpeciesName(), actualSpecies.getSpeciesName());
        assertEquals(expSpecies.getPreferredDisplayOrder(), actualSpecies.getPreferredDisplayOrder());
        assertEquals(expGene.getGeneId(), actualGene.getGeneId());
        assertEquals(expGene.getName(), actualGene.getName());
        assertEquals(expGene.getDescription(), actualGene.getDescription());
        assertEquals(expGene.getSynonyms(), actualGene.getSynonyms());
        assertEquals(expGene.getXRefs(), actualGene.getXRefs());
        assertEquals(expGene.getGeneMappedToSameGeneIdCount(), actualGene.getGeneMappedToSameGeneIdCount());
    }

    @Test
    public void shouldFindAnatEntityByTerm() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        SphinxResult sphinxResult = new SphinxResult();
        sphinxResult.totalFound = 1;
        sphinxResult.attrNames = new String[]{
                "anatentityid", "anatentityname", "speciesid", "type"};
        SphinxMatch sphinxMatch1 = new SphinxMatch(1, 1);
        sphinxMatch1.attrValues = new ArrayList();
        sphinxMatch1.attrValues.add("ID:0001");  //anatEntityId
        sphinxMatch1.attrValues.add("anat1");    //anatEntityName
        sphinxMatch1.attrValues.add("9606");     //speciesId
        sphinxMatch1.attrValues.add(1);          //type
        SphinxMatch sphinxMatch2 = new SphinxMatch(1, 1);
        sphinxMatch2.attrValues.add("ID:0001");  //anatEntityId
        sphinxMatch2.attrValues.add("anat1");    //anatEntityName
        sphinxMatch2.attrValues.add("9606");     //speciesId
        sphinxMatch2.attrValues.add(2);          //type
        SphinxMatch sphinxMatch3 = new SphinxMatch(1, 1);
        sphinxMatch3.attrValues.add("ID:0001");  //anatEntityId
        sphinxMatch3.attrValues.add("anat1");    //anatEntityName
        sphinxMatch3.attrValues.add("10090");    //speciesId
        sphinxMatch3.attrValues.add(2);          //type
        SphinxMatch sphinxMatch4 = new SphinxMatch(1, 1);
        sphinxMatch4.attrValues.add("ID:0002");  //anatEntityId
        sphinxMatch4.attrValues.add("an1");    //anatEntityName
        sphinxMatch4.attrValues.add("9606");    //speciesId
        sphinxMatch4.attrValues.add(2);          //type

        sphinxResult.matches = new SphinxMatch[] {sphinxMatch1, sphinxMatch2,
                sphinxMatch3};

        String term = "anat";
        when(sphinxClient.Query("\"" + term + "\"", "anat_entities_index")).thenReturn(sphinxResult);

        SearchMatchResultService service = new SearchMatchResultService(sphinxClient, this.serviceFactory,
                "genes_index", "anat_entities_index", "strains_index", "autocomplete_index",
                GENE_BIO_TYPES);
        SearchMatchResult<AnatEntity> anatMatchResult =
                service.searchAnatEntitiesByTerm(term, null, true, true, 0, 100);

        assertEquals(3, anatMatchResult.getTotalMatchCount());
        assertNotNull(anatMatchResult.getSearchMatches());
        assertEquals(3, anatMatchResult.getSearchMatches().size());

        AnatEntity expAE = new AnatEntity("ID:0001", "anat1", null);

        assertEquals(new SearchMatch<AnatEntity>(expAE, "anat", SearchMatch.MatchSource.NAME, AnatEntity.class),
                anatMatchResult.getSearchMatches().get(0));
        NamedEntity<String> actualAE = anatMatchResult.getSearchMatches().get(0).getSearchedObject();
        assertEquals(expAE.getId(), actualAE.getId());
        assertEquals(expAE.getName(), actualAE.getName());
        assertEquals(expAE.getDescription(), actualAE.getDescription());
    }

    /**
     * Test {@link GeneMatchResultService#searchByTerm(String, Collection, int, int)} 
     * of a search returning no result.
     */
    @Test
    public void shouldFindGeneByTerm_noResult() throws SphinxException {
        SphinxClient sphinxClient = mock(SphinxClient.class);

        String term = "XXX";

        when(sphinxClient.Query(term, "prefix_genes")).thenReturn(null);

        SearchMatchResultService service = new SearchMatchResultService(sphinxClient, this.serviceFactory,
                "genes_index", "anat_entities_index", "strains_index", "autocomplete_index",
                GENE_BIO_TYPES);
        SearchMatchResult<Gene> geneMatchResult = service.searchGenesByTerm(term, null, 0, 100);

        assertEquals(0, geneMatchResult.getTotalMatchCount());
        assertNull(geneMatchResult.getSearchMatches());
    }
}
