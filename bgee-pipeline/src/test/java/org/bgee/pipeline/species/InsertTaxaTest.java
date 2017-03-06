package org.bgee.pipeline.species;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.mysql.keyword.MySQLKeywordDAO.MySQLKeywordTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InsertTaxa}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertTaxaTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertTaxaTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake species 
     * file, containing the NCBI IDs of the species to used in Bgee, as a TSV file.
     */
    private final String SPECIESFILE = "/species/species.tsv";
    /**
     * A {@code String} that is the path from the classpath to the fake taxon IDs 
     * file, containing the NCBI IDs of the additional taxa to insert in Bgee, 
     * as a TSV file.
     */
    private final String TAXONFILE = "/species/taxonIds.tsv";
    /**
     * A {@code String} that is the path from the classpath to the fake NCBI 
     * taxonomy ontology. This taxonomy is as following; least common ancestors 
     * of considered species are marked with *, nested set model parameters after 
     * species removal are in brackets: 
     * <pre>
     *                    NCBITaxon:1 taxon A (1, 16, 1)
     *                         /                         \
     *                NCBITaxon:2 taxon B (2, 15, 2)      NCBITaxon:17 taxon not included
     *                        /                      \
     *            * NCBITaxon:6 taxon C * (3, 12, 3)   NCBITaxon:3 taxon z (13, 14, 3)
     *                   /                   \           
     *     NCBITaxon:11 taxon D (4, 9, 4)  NCBITaxon:16 taxon G (10, 11, 4)    
     *                  |                             |
     * * NCBITaxon:12 taxon E * (5, 8, 5)  NCBITaxon:8 species A 
     *          /                    \
     * NCBITaxon:13 species B    NCBITaxon:14 taxon F (6, 7, 6)
     *                                     |
     *                           NCBITaxon:15 species C 
     * </pre>
     */
    private final String TAXONTOLOGYFILE = "/species/fakeNCBITaxonomy.obo";

    /**
     * Default Constructor. 
     */
    public InsertTaxaTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertTaxa#insertSpeciesAndTaxa(String, String)}, which is 
     * the central method of the class doing all the job.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertSpeciesAndTaxa() throws FileNotFoundException, 
        OWLOntologyCreationException, OBOFormatParserException, IllegalArgumentException, 
        DAOException, IOException {
        //first, we need a mock MySQLDAOManager, for the class to acquire mock 
        //MySQLSpeciesDAO and mock MySQLTaxonDAO. This will allow to verify 
        //that the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();
        
        Set<String> expectedKeywords = new HashSet<>(Arrays.asList("speB", "speC", "speC 2"));
        KeywordTOResultSet mockRs = createMockDAOResultSet(Arrays.asList(
                new KeywordDAO.KeywordTO(1, "speB"), 
                new KeywordDAO.KeywordTO(2, "speC"), 
                new KeywordDAO.KeywordTO(3, "speC 2")), 
                MySQLKeywordTOResultSet.class);
        when(mockManager.mockKeywordDAO.getKeywords(expectedKeywords)).thenReturn(mockRs);
        
        InsertTaxa insert = new InsertTaxa(mockManager);
        insert.insertSpeciesAndTaxa(this.getClass().getResource(SPECIESFILE).getFile(), 
                this.getClass().getResource(TAXONFILE).getFile(), 
                this.getClass().getResource(TAXONTOLOGYFILE).getFile());
        
        //generate the expected Sets of SpeciesTOs and taxonTOs to verify the calls 
        //made to the DAOs
        Set<SpeciesTO> expectedSpeciesTOs = new HashSet<SpeciesTO>();
        expectedSpeciesTOs.add(
                new SpeciesTO(8, "my common nameA", "my genusA", "my speciesA", 2, 16, 
                        "file/pathA", "versionA", 2, null));
        expectedSpeciesTOs.add(
                new SpeciesTO(13, "my common nameB", "my genusB", "my speciesB", 1, 12, 
                        "file/pathB", "versionB", 24, 20));
        expectedSpeciesTOs.add(
                new SpeciesTO(15, "my common nameC", "my genusC", "my speciesC", 3, 14, 
                        "file/pathC", "versionC", 2, null));
        ArgumentCaptor<Set> speciesTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockSpeciesDAO).insertSpecies(speciesTOsArg.capture());
        if (!this.areSpeciesTOCollectionsEqual(
                expectedSpeciesTOs, speciesTOsArg.getValue())) {
            throw new AssertionError("Incorrect SpeciesTOs generated to insert species, " +
            		"expected " + expectedSpeciesTOs.toString() + ", but was " + 
            		speciesTOsArg.getValue());
        }
        
        //keywords
        ArgumentCaptor<Set> keywordArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockKeywordDAO).insertKeywords(keywordArg.capture());
        assertEquals("Incorrect keywords inserted", expectedKeywords, keywordArg.getValue());
        //species to keywords
        ArgumentCaptor<Set> speToKeywordArg = ArgumentCaptor.forClass(Set.class);
        Set<EntityToKeywordTO<Integer>> expectedSpeToKeywordTOs = new HashSet<>(Arrays.asList(
                new EntityToKeywordTO<>(13, 1), new EntityToKeywordTO<>(15, 2), 
                new EntityToKeywordTO<>(15, 3)));
        verify(mockManager.mockKeywordDAO).insertKeywordToSpecies(speToKeywordArg.capture());
        assertEquals("Incorrect species-keywords relations inserted", expectedSpeToKeywordTOs, 
                speToKeywordArg.getValue());
        
        

        Set<TaxonTO> expectedTaxonTOs = new HashSet<TaxonTO>();
        expectedTaxonTOs.add(new TaxonTO(1, null, "taxon A", 1, 16, 1, false));
        expectedTaxonTOs.add(new TaxonTO(2, "common name taxon B", "taxon B", 2, 15, 2, false));
        expectedTaxonTOs.add(new TaxonTO(3, null, "taxon z", 13, 14, 3, false));
        expectedTaxonTOs.add(new TaxonTO(6, "common name taxon C", "taxon C", 3, 12, 3, true));
        expectedTaxonTOs.add(new TaxonTO(11, "common name taxon D", "taxon D", 4, 9, 4, false));
        expectedTaxonTOs.add(new TaxonTO(12, "common name taxon E", "taxon E", 5, 8, 5, true));
        expectedTaxonTOs.add(new TaxonTO(14, "common name taxon F", "taxon F", 6, 7, 6, false));
        expectedTaxonTOs.add(new TaxonTO(16, "common name taxon G", "taxon G", 10, 11, 4, false));
        ArgumentCaptor<Set> taxonTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockTaxonDAO).insertTaxa(taxonTOsArg.capture());
        if (!this.areTaxonTOCollectionsEqual(
                expectedTaxonTOs, taxonTOsArg.getValue())) {
            throw new AssertionError("Incorrect TaxonTOs generated to insert species, " +
                    "expected " + expectedTaxonTOs.toString() + ", but was " + 
                    taxonTOsArg.getValue());
        }
    }
    
    /**
     * Method to compare two {@code Collection}s of {@code SpeciesTO}s, to check 
     * for complete equality of each attribute of each {@code SpeciesTO}. This is 
     * because the {@code equals} method of {@code SpeciesTO}s is solely based 
     * on their ID, not on other attributes. Here we check for equality of each 
     * attribute. 
     * 
     * @param c1    A {@code Collection} of {@code SpeciesTO}s o be compared to {@code c2}.
     * @param c2    A {@code Collection} of {@code SpeciesTO}s o be compared to {@code c1}.
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code SpeciesTO}s, and each {@code SpeciesTO} of a {@code Collection} 
     *              has an equivalent {@code SpeciesTO} in the other {@code Collection}, 
     *              with all attributes equal.
     */
    private boolean areSpeciesTOCollectionsEqual(Collection<SpeciesTO> c1, 
            Collection<SpeciesTO> c2) {
        if (c1.size() != c2.size()) {
            return false;
        }
        for (SpeciesTO s1: c1) {
            boolean found = false;
            for (SpeciesTO s2: c2) {
                if ((s1.getId() == null && s2.getId() == null || 
                        s1.getId() != null && s1.getId().equals(s2.getId())) && 
                    (s1.getName() == null && s2.getName() == null || 
                        s1.getName() != null && s1.getName().equals(s2.getName())) && 
                    (s1.getGenus() == null && s2.getGenus() == null || 
                        s1.getGenus() != null && s1.getGenus().equals(s2.getGenus())) && 
                    (s1.getSpeciesName() == null && s2.getSpeciesName() == null || 
                        s1.getSpeciesName() != null && s1.getSpeciesName().equals(s2.getSpeciesName())) && 
                    (s1.getParentTaxonId() == null && s2.getParentTaxonId() == null || 
                        s1.getParentTaxonId() != null && s1.getParentTaxonId().equals(s2.getParentTaxonId())) && 
                    (s1.getDescription() == null && s2.getDescription() == null || 
                        s1.getDescription() != null && s1.getDescription().equals(s2.getDescription())) && 
                    (s1.getGenomeFilePath() == null && s2.getGenomeFilePath() == null || 
                        s1.getGenomeFilePath() != null && s1.getGenomeFilePath().equals(s2.getGenomeFilePath())) && 
                    (s1.getGenomeSpeciesId() == null && s2.getGenomeSpeciesId() == null || 
                        s1.getGenomeSpeciesId() != null && s1.getGenomeSpeciesId().equals(s2.getGenomeSpeciesId())) ) {
                    found = true;   
                    break;
                }
            }
            if (!found) {
                return false;
            }      
        }
        return true;
    }
    
    /**
     * Method to compare two {@code Collection}s of {@code TaxonTO}s, to check 
     * for complete equality of each attribute of each {@code TaxonTO}. This is 
     * because the {@code equals} method of {@code TaxonTO}s is solely based 
     * on their ID, not on other attributes. Here we check for equality of each 
     * attribute. 
     * 
     * @param c1    A {@code Collection} of {@code TaxonTO}s o be compared to {@code c2}.
     * @param c2    A {@code Collection} of {@code TaxonTO}s o be compared to {@code c1}.
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code TaxonTO}s, and each {@code TaxonTO} of a {@code Collection} 
     *              has an equivalent {@code TaxonTO} in the other {@code Collection}, 
     *              with all attributes equal.
     */
    private boolean areTaxonTOCollectionsEqual(Collection<TaxonTO> c1, 
            Collection<TaxonTO> c2) {
        if (c1.size() != c2.size()) {
            return false;
        }
        for (TaxonTO t1: c1) {
            boolean found = false;
            for (TaxonTO t2: c2) {
                if ((t1.getId() == null && t2.getId() == null || 
                        t1.getId() != null && t1.getId().equals(t2.getId())) && 
                    (t1.getName() == null && t2.getName() == null || 
                        t1.getName() != null && t1.getName().equals(t2.getName())) && 
                    (t1.getScientificName() == null && t2.getScientificName() == null || 
                        t1.getScientificName() != null && 
                        t1.getScientificName().equals(t2.getScientificName())) && 
                    (t1.getLeftBound() == t2.getLeftBound()) && 
                    (t1.getRightBound() == t2.getRightBound()) && 
                    (t1.getLevel() ==t2.getLevel()) && 
                    (t1.isLca() == null && t2.isLca() == null || 
                        t1.isLca() != null && t1.isLca().equals(t2.isLca())) && 
                    (t1.getDescription() == null && t2.getDescription() == null || 
                        t1.getDescription() != null && t1.getDescription().equals(t2.getDescription())) ) {
                    found = true; 
                    break;
                }
            }
            if (!found) {
                return false;
            }      
        }
        return true;
    }
    
}
