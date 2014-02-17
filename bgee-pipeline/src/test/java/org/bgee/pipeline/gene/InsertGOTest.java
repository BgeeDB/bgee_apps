package org.bgee.pipeline.gene;

import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GOTermTO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class InsertGOTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertGOTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake Gene  
     * Ontology file. 
     */
    private final String GOFILE = "/gene/fakeGO.obo";
    
    /**
     * Default Constructor. 
     */
    public InsertGOTest() {
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
    @SuppressWarnings("unchecked")
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
                new GOTermTO("GO:2", "test B", GOTermTO.Domain.MF));
        expectedGOTermTOs.add(
                new GOTermTO("GO:3", "test C", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:4", "test D", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:5", "test E", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:6", "test F", GOTermTO.Domain.CC));
        expectedGOTermTOs.add(
                new GOTermTO("GO:7", "test G", GOTermTO.Domain.CC));
        
        ArgumentCaptor<Set> goTermTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneOntologyDAO).insertTerms(goTermTOsArg.capture());
        if (!this.areGOTermTOCollectionsEqual(
                expectedGOTermTOs, goTermTOsArg.getValue())) {
            throw new AssertionError("Incorrect GOTermTOs generated to insert GO terms, " +
                    "expected " + expectedGOTermTOs.toString() + ", but was " + 
                    goTermTOsArg.getValue());
        }
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
        if (c1.size() != c2.size()) {
            return false;
        }
        for (GOTermTO s1: c1) {
            boolean found = false;
            for (GOTermTO s2: c2) {
                if ((s1.getId() == null && s2.getId() == null || 
                        s1.getId() != null && s1.getId().equals(s2.getId())) && 
                    (s1.getName() == null && s2.getName() == null || 
                        s1.getName() != null && s1.getName().equals(s2.getName())) && 
                    (s1.getDomain() == null && s2.getDomain() == null || 
                        s1.getDomain() != null && s1.getDomain().equals(s2.getDomain())) ) {
                    found = true;    
                }
            }
            if (!found) {
                return false;
            }      
        }
        return true;
    }
}
