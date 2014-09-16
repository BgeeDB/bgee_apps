package org.bgee.pipeline.gene;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Unit tests for the classes {@link InsertGO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
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
        if (!TOComparator.areTOCollectionsEqual(
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
        verify(mockManager.mockRelationDAO).insertGeneOntologyRelations(relationTOsArg.capture());
        //RelationTO is not an EntityTO, and implements hashCode and equals, 
        //so we can directly use assertEquals
        assertEquals("Incorrect RelationTOs generated", expectedRelationTOs, 
                relationTOsArg.getValue());
    }
}
