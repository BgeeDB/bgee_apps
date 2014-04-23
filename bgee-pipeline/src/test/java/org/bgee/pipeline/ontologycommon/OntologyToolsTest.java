package org.bgee.pipeline.ontologycommon;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyTools;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

/**
 * Unit tests for the class {@link OntologyTools}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class OntologyToolsTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(OntologyToolsTest.class.getName());
    
    /**
     * A {@code String} that is the path from the classpath to the fake Gene  
     * Ontology file. 
     */
    private final String GOFILE = "/ontologies/fakeGO.obo";
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
     * Default Constructor. 
     */
    public OntologyToolsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link OntologyTools#getObsoleteIds(String)} (and subsequently, 
     * {@link OntologyTools#getObsoleteIds(OWLOntology))).

     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException      
     */
    @Test
    public void shouldGetObsoleteIds() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OntologyTools go = new OntologyTools();
        Set<String> expectedIds = new HashSet<String>();
        expectedIds.add("GO:8");
        expectedIds.add("GO:9");
        expectedIds.add("GO:12");
        
        assertEquals("Incorrect obsolete IDs retrieved", expectedIds, 
                go.getObsoleteIds(this.getClass().getResource(GOFILE).getFile()));
    }
    
    /**
     * Test {@link OntologyTools#writeObsoletedTermsToFile(String, String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException 
     */
    @Test
    public void shouldWriteObsoleteIdsToFile() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OntologyTools go = new OntologyTools();
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
    
    /**
     * Test {@link OntologyTools#getAllRealOWLClassIds(String)} (and subsequently, 
     * {@link OntologyTools#getAllRealOWLClassIds(OWLGraphWrapper))).

     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException      
     */
    @Test
    public void shouldGetAllRealOWLClassIds() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OntologyTools tools = new OntologyTools();
        Set<String> expectedIds = new HashSet<String>();
        expectedIds.add("GO:1");
        expectedIds.add("GO:2");
        expectedIds.add("GO:3");
        expectedIds.add("GO:4");
        expectedIds.add("GO:5");
        expectedIds.add("GO:6");
        expectedIds.add("GO:7");
        
        assertEquals("Incorrect IDs retrieved", expectedIds, 
                tools.getAllRealOWLClassIds(this.getClass().getResource(GOFILE).getFile()));
    }
    
    /**
     * Test {@link OntologyTools#writeOWLClassIdsToFile(String, String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     * @throws UnknownOWLOntologyException 
     */
    @Test
    public void shouldWriteOWLClassIdsToFile() throws UnknownOWLOntologyException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        OntologyTools tools = new OntologyTools();
        Set<String> expectedIds = new HashSet<String>();
        expectedIds.add("GO:1");
        expectedIds.add("GO:2");
        expectedIds.add("GO:3");
        expectedIds.add("GO:4");
        expectedIds.add("GO:5");
        expectedIds.add("GO:6");
        expectedIds.add("GO:7");
        
        String tempFile = testFolder.newFile("ids.txt").getPath();
        
        tools.writeOWLClassIdsToFile(this.getClass().getResource(GOFILE).getFile(), 
                tempFile);
        
        Set<String> actualIds = new HashSet<String>();
        try(BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                actualIds.add(line);
            }
        }
        
        assertEquals("Incorrect IDs written to file", expectedIds, actualIds);
    }
}
