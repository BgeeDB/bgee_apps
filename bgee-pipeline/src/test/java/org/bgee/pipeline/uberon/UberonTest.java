package org.bgee.pipeline.uberon;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link Uberon}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonTest.class.getName());
    
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Default Constructor. 
     */
    public UberonTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link Uberon#extractTaxonIds(String)}.
     */
    @Test
    public void shouldExtractTaxonIds() throws OWLOntologyCreationException, 
        OBOFormatParserException, IllegalArgumentException, IOException {
        
        Set<Integer> expectedTaxonIds = new HashSet<Integer>();
        //this one should be obtained from the 
        //oboInOwl:treat-xrefs-as-reverse-genus-differentia ontology annotations
        expectedTaxonIds.add(1); 
        //those should be obtained from the object properties
        expectedTaxonIds.addAll(Arrays.asList(2, 3, 4, 13)); 
        //those should be obtained from annotation properties
        expectedTaxonIds.addAll(Arrays.asList(5, 6, 7, 8, 9, 10, 11, 12));
        
        assertEquals("Incorrect taxon IDs extracted", expectedTaxonIds, 
            new Uberon(this.getClass().getResource("/uberon/uberonTaxonTest.owl").getPath()).
            extractTaxonIds());
    }
    
    /**
     * Test the method {@link Uberon#saveSimplificationInfo(OWLOntology, String, Map)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldSaveSimplificationInfo() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        Uberon uberonTest = 
                new Uberon(this.getClass().getResource("/uberon/simplifyInfoTest.obo").getPath());
        OWLOntology uberonOnt = OntologyUtils.loadOntology(
                this.getClass().getResource("/uberon/simplifyInfoTest.obo").getPath());
        
        //U:4 is obsolete and should not be displayed in the file
        Map<String, String> classesRemoved = new HashMap<String, String>();
        classesRemoved.put("U:4", "reason 4");
        classesRemoved.put("U:3", "reason 3");
        classesRemoved.put("U:2", "reason 2");
        classesRemoved.put("U:23", "reason 23");
        String tempFile = testFolder.newFile("simplifyInfo.tsv").getPath();
        uberonTest.saveSimplificationInfo(uberonOnt, tempFile, classesRemoved);
        
        //now, read the generated TSV file
        int i = 0;
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true); 
            final CellProcessor[] processors = new CellProcessor[] {
                    new UniqueHashCode(new NotNull()), //Uberon ID
                    new NotNull(), //Uberon name
                    new Optional(), //relations
                    new NotNull()}; //reason for removal

            Map<String, Object> infoMap;
            while( (infoMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", infoMap);
                String uberonId = (String) infoMap.get(headers[0]);
                String uberonName = (String) infoMap.get(headers[1]);
                String relations = (String) infoMap.get(headers[2]);
                String reason = (String) infoMap.get(headers[3]);
                log.trace("Retrieved info from line: {} - {}", uberonId, uberonName); 
                if (i == 0) {
                    assertEquals("U:2", uberonId);
                    assertEquals("brain", uberonName);
                    assertEquals("reason 2", reason);
                    String relationTested = "is_a U:1 anatomical structure";
                    assertTrue("Missing relation for U:2: '" + relationTested + "' - " +
                    		"Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else if (i == 1) {
                    assertEquals("U:3", uberonId);
                    assertEquals("forebrain", uberonName);
                    assertEquals("reason 3", reason);
                    String relationTested = "is_a U:1 anatomical structure";
                    assertTrue("Missing relation for U:3: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                    relationTested = "part_of U:2 brain";
                    assertTrue("Missing relation for U:3: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else if (i == 2) {
                    assertEquals("U:23", uberonId);
                    assertEquals("U_23", uberonName);
                    assertEquals("reason 23", reason);
                    String relationTested = "is_a U:22 antenna";
                    assertTrue("Missing relation for U:23: '" + relationTested + "' - " +
                            "Actual relations were: " + relations, 
                            relations.contains(relationTested));
                } else {
                    throw new AssertionError("Incorrect number of Uberon terms listed, " +
                    		"currently iterated term: " + uberonId + " - " + uberonName);
                }
                i++;
                
            }
        }
        assertEquals("Incorrect number of lines in TSV output", 3, i);
    }
    
    /**
     * Test the method {@link Uberon#simplifyUberon(OWLOntology, Collection, 
     * Collection, Collection, Collection, Collection)}
     */
    @Test
    public void shouldSimplifyUberon() {
        //TODO: this unit test should be done, but as it only uses methods 
        //from OWLGraphManipulator, that are already tested, I am lazy here.
        //but it should be done anyway, because OWLGraphManipulator is not officially 
        //part of Bgee, but of owltools, so we must ensure that no modifications 
        //of owltools mess up our simplification
        //ontology to use for the test: /uberon/simplifyUberonTest.obo
    }
    
    /**
     * Test the method {@link Uberon#saveXRefMappingsToFile(OWLOntology, String)}.
     * @throws IOException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    @Test
    public void shouldSaveXRefMappings() throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        
        String tempFile = testFolder.newFile("xRefMappings.tsv").getPath();
        
        new Uberon(UberonTest.class.getResource("/ontologies/xRefMappings.obo").getFile()).
        saveXRefMappingsToFile(tempFile);
        
        //now, read the generated TSV file
        int i = 0;
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(tempFile), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true); 
            final CellProcessor[] processors = new CellProcessor[] {
                    new NotNull(), //XRef ID
                    new NotNull()}; //Uberon ID

            Map<String, Object> xRefMap;
            while( (xRefMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", xRefMap);
                String xRefId = (String) xRefMap.get(headers[0]);
                String uberonId = (String) xRefMap.get(headers[1]);
                log.trace("Retrieved info from line: {} - {}", xRefId, uberonId); 
                
                if (!(xRefId.equals("ALT_ID:1") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ALT_ID:1") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ID:3") && uberonId.equals("ID:3") || 
                        xRefId.equals("ALT_ALT_ID:3") && uberonId.equals("ID:3") || 
                        xRefId.equals("ALT_ID:2") && uberonId.equals("ID:1") || 
                        xRefId.equals("ALT_ID:2") && uberonId.equals("ID:2") || 
                        xRefId.equals("ID_REPLACED_BIS:4") && uberonId.equals("ID_REPLACED_BIS_XREF:4") || 
                        xRefId.equals("ID_XREF_OBSOLETE:6") && uberonId.equals("ID:4") || 
                        xRefId.equals("ID_XREF_OBSOLETE:6") && uberonId.equals("ID:5") || 
                        xRefId.equals("ID_XREF_OBSOLETE:1") && uberonId.equals("ID:1") || 
                        xRefId.equals("ID_XREF_OBSOLETE:5") && uberonId.equals("ID:5") || 
                        xRefId.equals("ID:7") && uberonId.equals("ID:6"))) {
                    throw new AssertionError("Incorrect line: " + mapReader.getUntokenizedRow());
                }
                i++;
                
            }
        }
        assertEquals("Incorrect number of lines in TSV output", 12, i);
    }

    
    //@Test
    public void test() throws OBOFormatParserException, OWLOntologyCreationException, IOException {
        OWLOntology ont = OntologyUtils.loadOntology("/Users/admin/Desktop/composite-metazoan.owl");
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OWLGraphManipulator manip = new OWLGraphManipulator(wrapper);
        
        manip.removeUnrelatedRelations(Arrays.asList("BFO:0000050", "RO:0002202", "RO:0002494"));
        
        //need to understand where the indirect transformation_of relation between 
        //UBERON:0000010 and UBERON:0016880 comes from
        for (OWLGraphEdge edge: wrapper.getOutgoingEdgesNamedClosureOverSupPropsWithGCI(
                wrapper.getOWLClassByIdentifier("UBERON:0000010"))) {
            if (edge.getTarget().equals(wrapper.getOWLClassByIdentifier("UBERON:0016880"))) {
                log.info(edge);
            }
        }
        for (OWLGraphEdge edge: wrapper.getOutgoingEdgesWithGCI(
                wrapper.getOWLClassByIdentifier("UBERON:0000010"))) {
            if (edge.getTarget().equals(wrapper.getOWLClassByIdentifier("UBERON:0016880"))) {
                log.info(edge);
            }
        }
    }
}
