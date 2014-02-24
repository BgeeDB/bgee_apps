package org.bgee.pipeline.gene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GOTermTO;
import org.bgee.model.dao.api.ontologycommon.RelationTO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

/**
 * Unit tests for the classes {@link InsertGO} and {@link GOTools}.
 * 
 * @author admin
 *
 */
public class GOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(GOTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake Gene  
     * Ontology file. 
     */
    private final String GOFILE = "/gene/fakeGO.obo";
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
     * Default Constructor. 
     */
    public GOTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertGO#insert(String)}, which is 
     * the central method of the class doing all the job.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertGO() throws FileNotFoundException, 
        OWLOntologyCreationException, OBOFormatParserException, IllegalArgumentException, 
        DAOException, IOException {
        //first, we need a mock MySQLDAOManager, for the class to acquire mock 
        //MySQLGeneOntologyDAO. This will allow to verify 
        //that the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();
        
        InsertGO insert = new InsertGO(mockManager);
        insert.insert(this.getClass().getResource(GOFILE).getFile());
        
        //generate the expected Sets of GOTermTOs to verify the calls 
        //made to the DAO
        Set<GOTermTO> expectedGOTermTOs = new HashSet<GOTermTO>();
        expectedGOTermTOs.add(
                new GOTermTO("GO:1", "test A", GOTermTO.Domain.BP));
        expectedGOTermTOs.add(
                new GOTermTO("GO:2", "test B", GOTermTO.Domain.MF, 
                        Arrays.asList("GO:2_alt1", "GO:2_alt2", "GO:2_alt3")));
        expectedGOTermTOs.add(
                new GOTermTO("GO:3", "test C", GOTermTO.Domain.CC, 
                        Arrays.asList("GO:3_alt1", "GO:3_alt2")));
        expectedGOTermTOs.add(
                new GOTermTO("GO:4", "test D", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:5", "test E", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:6", "test F", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:7", "test G", GOTermTO.Domain.CC, 
                        Arrays.asList("GO:7_alt1")));
        
        ArgumentCaptor<Set> goTermTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneOntologyDAO).insertTerms(goTermTOsArg.capture());
        if (!this.areGOTermTOCollectionsEqual(
                expectedGOTermTOs, goTermTOsArg.getValue())) {
            throw new AssertionError("Incorrect GOTermTOs generated to insert GO terms, " +
                    "expected " + expectedGOTermTOs.toString() + ", but was " + 
                    goTermTOsArg.getValue());
        }
        
        
        //generate the expected Sets of RelationTOs to verify the calls 
        //made to the DAO
        Set<RelationTO> expectedRelationTOs = new HashSet<RelationTO>();
        expectedRelationTOs.add(new RelationTO("GO:6", "GO:1"));
        expectedRelationTOs.add(new RelationTO("GO:6", "GO:5"));
        expectedRelationTOs.add(new RelationTO("GO:6", "GO:4"));
        expectedRelationTOs.add(new RelationTO("GO:6", "GO:3"));
        
        expectedRelationTOs.add(new RelationTO("GO:5", "GO:4"));
        expectedRelationTOs.add(new RelationTO("GO:5", "GO:3"));
        
        expectedRelationTOs.add(new RelationTO("GO:4", "GO:3"));
        
        ArgumentCaptor<Set> relationTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneOntologyDAO).insertRelations(relationTOsArg.capture());
        //RelationTO is not an EntityTO, and implements hashCode and equals, 
        //so we can directly use assertEquals
        assertEquals("Incorrect RelationTOs generated", expectedRelationTOs, 
                relationTOsArg.getValue());
    }
    
    /**
     * Method to compare two {@code Collection}s of {@code GOTermTO}s, to check 
     * for complete equality of each attribute of each {@code GOTermTO}. This is 
     * because the {@code equals} method of {@code GOTermTO}s is solely based 
     * on their ID, not on other attributes. Here we check for equality of each 
     * attribute. 
     * 
     * @param c1    A {@code Collection} of {@code GOTermTO}s o be compared to {@code c2}.
     * @param c2    A {@code Collection} of {@code GOTermTO}s o be compared to {@code c1}.
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code GOTermTO}s, and each {@code GOTermTO} of a {@code Collection} 
     *              has an equivalent {@code GOTermTO} in the other {@code Collection}, 
     *              with all attributes equal.
     */
    private boolean areGOTermTOCollectionsEqual(Collection<GOTermTO> c1, 
            Collection<GOTermTO> c2) {
        log.entry(c1, c2);
        
        if (c1.size() != c2.size()) {
            log.debug("Non matching sizes, {} - {}", c1.size(), c2.size());
            return log.exit(false);
        }
        for (GOTermTO s1: c1) {
            boolean found = false;
            for (GOTermTO s2: c2) {
                log.trace("Comparing {} to {}", s1, s2);
                if ((s1.getId() == null && s2.getId() == null || 
                        s1.getId() != null && s1.getId().equals(s2.getId())) && 
                    (s1.getName() == null && s2.getName() == null || 
                        s1.getName() != null && s1.getName().equals(s2.getName())) && 
                    (s1.getDomain() == null && s2.getDomain() == null || 
                        s1.getDomain() != null && s1.getDomain().equals(s2.getDomain())) && 
                    (s1.getAltIds().equals(s2.getAltIds())) ) {
                    found = true;    
                }
            }
            if (!found) {
                log.debug("No equivalent term found for {}", s1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }
    
    /**
     * Test {@link GOTools#getObsoleteIds(String)} (and subsequently, 
     * {@link GOTools#getObsoleteIds(OWLOntology))).

     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException      */
    @Test
    public void shouldGetObsoleteIds() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        GOTools go = new GOTools();
        Set<String> expectedIds = new HashSet<String>();
        expectedIds.add("GO:8");
        expectedIds.add("GO:9");
        expectedIds.add("GO:12");
        
        assertEquals("Incorrect obsolete IDs retrieved", expectedIds, 
                go.getObsoleteIds(this.getClass().getResource(GOFILE).getFile()));
    }
    
    /**
     * Test {@link GOTools#writeObsoletedTermsToFile(String, String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException 
     */
    @Test
    public void shouldWriteObsoleteIdsToFile() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        GOTools go = new GOTools();
        Set<String> expectedIds = new HashSet<String>();
        expectedIds.add("GO:8");
        expectedIds.add("GO:9");
        expectedIds.add("GO:12");
        
        String tempFile = testFolder.newFile("obsIds.txt").getPath();
        
        go.writeObsoletedTermsToFile(this.getClass().getResource(GOFILE).getFile(), 
                tempFile);
        
        Set<String> actualIds = new HashSet<String>();
        try(BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                actualIds.add(line);
            }
        }
        
        assertEquals("Incorrect obsolete IDs written to file", expectedIds, actualIds);
    }
}
