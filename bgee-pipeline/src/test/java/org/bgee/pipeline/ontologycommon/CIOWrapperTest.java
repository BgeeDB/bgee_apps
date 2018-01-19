package org.bgee.pipeline.ontologycommon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Unit tests for {@link CIOWrapper}. This class does not use the usual bgee utils, 
 * to be provided as code example. The dependencies for this class are: 
 * OWLTools, OWLAPI, Log4j2, Junit.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Feb. 2015
 * @since Bgee 13
 */
public class CIOWrapperTest {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CIOWrapperTest.class.getName());
    
    /**
     * The {@code CIOWrapper} wrapping the CIO ontology used for tests.
     */
    private static CIOWrapper cioWrapper;
    /**
     * The {@code OWLGraphWrapper} used by {@link #cioWrapper} (returned by 
     * {@link CIOWrapper#getOWLGraphWrapper()}).
     */
    private static OWLGraphWrapper graphWrapper;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    /**
     * Default Constructor. 
     */
    public CIOWrapperTest() {
    }
    
    @BeforeClass
    /**
     * Load the CIO ontology used for tests, and set {@link #cioWrapper} and 
     * {@link #graphWrapper}.
     * @throws OBOFormatParserException
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    public static void loadOntology() throws OBOFormatParserException, 
    OWLOntologyCreationException, IOException {
        log.entry();
        cioWrapper = new CIOWrapper(OntologyUtils.loadOntology(CIOWrapperTest.class.
                getResource("/ontologies/cio.owl").getFile()));
        graphWrapper = cioWrapper.getOWLGraphWrapper();
        log.exit();
    }

    
    /**
     * Test method {@link CIOWrapper#getConfidenceLevel(OWLClass)}.
     */
    @Test
    public void shouldGetConfidenceLevel() {
        log.entry();
    
        OWLClass highConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.HIGH_CONF_LEVEL_ID);
        OWLClass mediumConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.MEDIUM_CONF_LEVEL_ID);
        OWLClass lowConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.LOW_CONF_LEVEL_ID);
        
        assertEquals("Incorrect confidence level retrieved", highConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003")));
        assertEquals("Incorrect confidence level retrieved", highConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012")));
        assertEquals("Incorrect confidence level retrieved", highConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017")));
        assertEquals("Incorrect confidence level retrieved", highConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022")));
        assertEquals("Incorrect confidence level retrieved", highConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025")));
    
        assertEquals("Incorrect confidence level retrieved", mediumConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004")));
        assertEquals("Incorrect confidence level retrieved", mediumConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013")));
        assertEquals("Incorrect confidence level retrieved", mediumConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019")));
        assertEquals("Incorrect confidence level retrieved", mediumConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024")));
        assertEquals("Incorrect confidence level retrieved", mediumConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
    
        assertEquals("Incorrect confidence level retrieved", lowConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005")));
        assertEquals("Incorrect confidence level retrieved", lowConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014")));
        assertEquals("Incorrect confidence level retrieved", lowConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertEquals("Incorrect confidence level retrieved", lowConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023")));
        assertEquals("Incorrect confidence level retrieved", lowConf, 
                cioWrapper.getConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026")));
    
        //test when method used with a CI element rather than a CI statement
        thrown.expect(IllegalArgumentException.class);
        cioWrapper.getConfidenceLevel(graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000038"));
        
        log.exit();
    }
    
    /**
     * Test {@link CIOWrapper#hasLeafConfidenceLevel(OWLClass)}.
     */
    @Test
    public void testHasLeafConfidenceLevel() {
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", false, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000000")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", false, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000001")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", false, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000002")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", false, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000006")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024")));
        assertEquals("Incorrect value returned by hasLeafConfidenceLevel", true, 
                cioWrapper.hasLeafConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
    }
    
    /**
     * Test {@link CIOWrapper#isStronglyConflicting(OWLClass)}.
     */
    @Test
    public void testIsStronglyConflicting() {
        assertEquals("Incorrect value returned by isStronglyConflicting", false, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000000")));
        assertEquals("Incorrect value returned by isStronglyConflicting", false, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003")));
        assertEquals("Incorrect value returned by isStronglyConflicting", false, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000000")));
        assertEquals("Incorrect value returned by isStronglyConflicting", false, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013")));
        assertEquals("Incorrect value returned by isStronglyConflicting", false, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000008")));
        assertEquals("Incorrect value returned by isStronglyConflicting", true, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000010")));
        assertEquals("Incorrect value returned by isStronglyConflicting", true, 
                cioWrapper.isStronglyConflicting(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
    }

    /**
     * Test method {@link CIOWrapper#getEvidenceConcordance(OWLClass)}.
     */
    @Test
    public void shouldGetEvidenceConcordance() {
        log.entry();
    
        OWLClass singleConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.SINGLE_EVIDENCE_CONCORDANCE_ID);
        OWLClass multipleConc = graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000043");
        OWLClass congruentConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.CONGRUENT_CONCORDANCE_ID);
        OWLClass weaklyConflictingConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.WEAKLY_CONFLICTING_CONCORDANCE_ID);
        OWLClass stronglyConflictingConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.STRONGLY_CONFLICTING_CONCORDANCE_ID);
        
        assertEquals("Incorrect evidence concordance retrieved", singleConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000001")));
        assertEquals("Incorrect evidence concordance retrieved", singleConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003")));
        assertEquals("Incorrect evidence concordance retrieved", singleConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004")));
        assertEquals("Incorrect evidence concordance retrieved", singleConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005")));
    
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000008")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000016")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertEquals("Incorrect evidence concordance retrieved", congruentConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019")));
    
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000011")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000021")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026")));
        assertEquals("Incorrect evidence concordance retrieved", weaklyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
    
        assertEquals("Incorrect evidence concordance retrieved", stronglyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000010")));
        assertEquals("Incorrect evidence concordance retrieved", stronglyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
        assertEquals("Incorrect evidence concordance retrieved", stronglyConflictingConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000010")));
    
        assertEquals("Incorrect evidence concordance retrieved", multipleConc, 
                cioWrapper.getEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000002")));
        
        //test when method used with a CI element rather than a CI statement
        thrown.expect(IllegalArgumentException.class);
        cioWrapper.getEvidenceConcordance(graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000042"));
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#getEvidenceTypeConcordance(OWLClass)}.
     */
    @Test
    public void shouldGetEvidenceTypeConcordance() {
        log.entry();
        
        OWLClass sameType = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
        OWLClass differentTypes = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
        
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000006")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000015")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000016")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000021")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023")));
        assertEquals("Incorrect evidence type concordance retrieved", sameType, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024")));
        
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000007")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000008")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000009")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000010")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000011")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026")));
        assertEquals("Incorrect evidence type concordance retrieved", differentTypes, 
                cioWrapper.getEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
    
        //test when method used with a CI element rather than a CI statement
        thrown.expect(IllegalArgumentException.class);
        cioWrapper.getEvidenceTypeConcordance(graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000033"));
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#getBestTermWithConfidenceLevel(Collection)}.
     */
    @Test
    public void shouldGetBestTermWithConfidenceLevel() {
        log.entry();
        
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005"))));
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005"))));
    
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014"))));
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014"))));
    
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018"))));
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018"))));
    
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024"))));
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024"))));
    
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027"))));
        assertEquals("Incorrect best term retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025"), 
                cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025"), 
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027"))));
        
        //check that exceptions are thrown if terms not member of a same branch, 
        //or not linked to a confidence level
        try {
            cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
            //test failed
            throw new AssertionError("Expecting to thrown an Exception");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004")));
            //test failed
            throw new AssertionError("Expecting to thrown an Exception");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        try {
            cioWrapper.getBestTermWithConfidenceLevel(Arrays.asList(
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023"), 
                    graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000021")));
            //test failed
            throw new AssertionError("Expecting to thrown an Exception");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#getSingleEvidenceConfidenceStatement(OWLClass)}.
     */
    @Test
    public void shouldGetSingleEvidenceConfidenceStatement() {
        log.entry();
        
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000003"), 
                cioWrapper.getSingleEvidenceConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.HIGH_CONF_LEVEL_ID)));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000004"), 
                cioWrapper.getSingleEvidenceConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.MEDIUM_CONF_LEVEL_ID)));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000005"), 
                cioWrapper.getSingleEvidenceConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.LOW_CONF_LEVEL_ID)));
        
        thrown.expect(IllegalArgumentException.class);
        cioWrapper.getSingleEvidenceConfidenceStatement(
                graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.CONFIDENCE_LEVEL_ID));
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#getConfidenceStatement(OWLClass, OWLClass, OWLClass)}.
     */
    @Test
    public void shouldGetConfidenceStatement() {
        log.entry();
        
        OWLClass highConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.HIGH_CONF_LEVEL_ID);
        OWLClass mediumConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.MEDIUM_CONF_LEVEL_ID);
        OWLClass lowConf = graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.LOW_CONF_LEVEL_ID);
        OWLClass multipleConc = graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000043");
        OWLClass congruentConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.CONGRUENT_CONCORDANCE_ID);
        OWLClass weaklyConflictingConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.WEAKLY_CONFLICTING_CONCORDANCE_ID);
        OWLClass stronglyConflictingConc = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.STRONGLY_CONFLICTING_CONCORDANCE_ID);
        OWLClass sameType = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
        OWLClass differentTypes = graphWrapper.getOWLClassByIdentifierNoAltIds(
                CIOWrapper.DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
        
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000006"), 
                cioWrapper.getConfidenceStatement(multipleConc, sameType, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000007"), 
                cioWrapper.getConfidenceStatement(multipleConc, differentTypes, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000008"), 
                cioWrapper.getConfidenceStatement(congruentConc, differentTypes, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000010"), 
                cioWrapper.getConfidenceStatement(stronglyConflictingConc, differentTypes, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000011"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, differentTypes, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000012"), 
                cioWrapper.getConfidenceStatement(congruentConc, differentTypes, highConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000013"), 
                cioWrapper.getConfidenceStatement(congruentConc, differentTypes, mediumConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000014"), 
                cioWrapper.getConfidenceStatement(congruentConc, differentTypes, lowConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000016"), 
                cioWrapper.getConfidenceStatement(congruentConc, sameType, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000017"), 
                cioWrapper.getConfidenceStatement(congruentConc, sameType, highConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018"), 
                cioWrapper.getConfidenceStatement(congruentConc, sameType, lowConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000019"), 
                cioWrapper.getConfidenceStatement(congruentConc, sameType, mediumConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020"), 
                cioWrapper.getConfidenceStatement(stronglyConflictingConc, sameType, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000021"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, sameType, null));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000022"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, sameType, highConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000023"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, sameType, lowConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000024"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, sameType, mediumConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000025"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, differentTypes, highConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000026"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, differentTypes, lowConf));
        assertEquals("Incorrect confidence statement retrieved", 
                graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027"), 
                cioWrapper.getConfidenceStatement(weaklyConflictingConc, differentTypes, mediumConf));
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#isConfidenceStatement(OWLClass)}.
     */
    @Test
    public void testIsConfidenceStatement() {
        log.entry();
        
        //check random CI statements
        assertTrue("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000016")));
        assertTrue("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
        assertTrue("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000000")));
        assertTrue("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000006")));
        
        //check random non-CI statements
        assertFalse("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000041")));
        assertFalse("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000040")));
        assertFalse("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertFalse("Incorrect value returned by isConfidenceStatement", 
                cioWrapper.isConfidenceStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000028")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isConfidenceElement(OWLClass)}.
     */
    @Test
    public void testIsConfidenceElement() {
        log.entry();
        
        //check random CI elements
        assertTrue("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000041")));
        assertTrue("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000040")));
        assertTrue("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertTrue("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000028")));
        
        //check random non-CI elements
        assertFalse("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000016")));
        assertFalse("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000027")));
        assertFalse("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000000")));
        assertFalse("Incorrect value returned by isConfidenceElement", 
                cioWrapper.isConfidenceElement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000006")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isEvidenceConcordance(OWLClass)}.
     */
    @Test
    public void testIsEvidenceConcordance() {
        log.entry();
        
        //check random evidence concordance terms
        assertTrue("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000032")));
        assertTrue("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000033")));
        assertTrue("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000043")));
        assertTrue("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000042")));
        
        //check random terms that are not evidence concordance terms
        assertFalse("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
        assertFalse("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000029")));
        assertFalse("Incorrect value returned by isEvidenceConcordance", 
                cioWrapper.isEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isSingleEvidenceConcordance(OWLClass)}.
     */
    @Test
    public void testIsSingleEvidenceConcordance() {
        log.entry();
        
        //check single evidence concordance term
        assertTrue("Incorrect value returned by isSingleEvidenceConcordance", 
                cioWrapper.isSingleEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000042")));
        
        //check random terms that are not single evidence concordance terms
        assertFalse("Incorrect value returned by isSingleEvidenceConcordance", 
                cioWrapper.isSingleEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
        assertFalse("Incorrect value returned by isSingleEvidenceConcordance", 
                cioWrapper.isSingleEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000029")));
        assertFalse("Incorrect value returned by isSingleEvidenceConcordance", 
                cioWrapper.isSingleEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertFalse("Incorrect value returned by isSingleEvidenceConcordance", 
                cioWrapper.isSingleEvidenceConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000043")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isEvidenceTypeConcordance(OWLClass)}.
     */
    @Test
    public void testIsEvidenceTypeConcordance() {
        log.entry();
        
        //check random evidence type concordance terms
        assertTrue("Incorrect value returned by isEvidenceTypeConcordance", 
                cioWrapper.isEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertTrue("Incorrect value returned by isEvidenceTypeConcordance", 
                cioWrapper.isEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000038")));
        
        //check random terms that are not evidence type concordance terms
        assertFalse("Incorrect value returned by isEvidenceTypeConcordance", 
                cioWrapper.isEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000020")));
        assertFalse("Incorrect value returned by isEvidenceTypeConcordance", 
                cioWrapper.isEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000029")));
        assertFalse("Incorrect value returned by isEvidenceTypeConcordance", 
                cioWrapper.isEvidenceTypeConcordance(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000043")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isConfidenceLevel(OWLClass)}.
     */
    @Test
    public void testIsConfidenceLevel() {
        log.entry();
        
        //check random confidence level terms
        assertTrue("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000028")));
        assertTrue("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000029")));
        assertTrue("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000030")));
        assertTrue("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000031")));
        
        //check random terms that are not confidence level terms
        assertFalse("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertFalse("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertFalse("Incorrect value returned by isConfidenceLevel", 
                cioWrapper.isConfidenceLevel(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000033")));
        
        log.exit();
    }
    /**
     * Test method {@link CIOWrapper#isRejectedStatement(OWLClass)}.
     */
    @Test
    public void testIsRejectedStatement() {
        log.entry();
        
        //check random confidence level terms
        assertTrue("Incorrect value returned by isRejectedStatement", 
                cioWrapper.isRejectedStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.REJECTED_STATEMENT_ID)));
        
        //check random terms that are not rejected statement terms
        assertFalse("Incorrect value returned by isRejectedStatement", 
                cioWrapper.isRejectedStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000018")));
        assertFalse("Incorrect value returned by isRejectedStatement", 
                cioWrapper.isRejectedStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000037")));
        assertFalse("Incorrect value returned by isRejectedStatement", 
                cioWrapper.isRejectedStatement(
                        graphWrapper.getOWLClassByIdentifierNoAltIds("CIO:0000033")));
        
        log.exit();
    }

    /**
     * Test method {@link CIOWrapper#getOrderedConfidenceLevels()}.
     */
    @Test
    public void shouldGetOrderedConfidenceLevels() {
        log.entry();
        
        assertEquals("Incorrect ordered confidence levels", Arrays.asList(
                graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.LOW_CONF_LEVEL_ID), 
                graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.MEDIUM_CONF_LEVEL_ID), 
                graphWrapper.getOWLClassByIdentifierNoAltIds(CIOWrapper.HIGH_CONF_LEVEL_ID)), 
                cioWrapper.getOrderedConfidenceLevels());
        
        log.exit();
    }
    
    /**
     * Test that the public static final {@code String}s providing OBO IDs of elements 
     * allow to retrieve non-null, not obsolete {@code OWLClass}es.
     * 
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    @Test
    public void shouldGetById() {
        log.entry();
        OWLGraphWrapper wrapper = cioWrapper.getOWLGraphWrapper();
        
        this.checkClass(wrapper, CIOWrapper.CONFIDENCE_ELEMENT_ID);
        this.checkClass(wrapper, CIOWrapper.CONFIDENCE_LEVEL_ID);
        this.checkClass(wrapper, CIOWrapper.CONFIDENCE_STATEMENT_ID);
        this.checkClass(wrapper, CIOWrapper.CONGRUENT_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.EVIDENCE_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.EVIDENCE_TYPE_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.HIGH_CONF_LEVEL_ID);
        this.checkClass(wrapper, CIOWrapper.LOW_CONF_LEVEL_ID);
        this.checkClass(wrapper, CIOWrapper.MEDIUM_CONF_LEVEL_ID);
        this.checkClass(wrapper, CIOWrapper.REJECTED_STATEMENT_ID);
        this.checkClass(wrapper, CIOWrapper.SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.SINGLE_EVIDENCE_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.STRONGLY_CONFLICTING_CONCORDANCE_ID);
        this.checkClass(wrapper, CIOWrapper.WEAKLY_CONFLICTING_CONCORDANCE_ID);
        
        this.checkObjectProperty(wrapper, CIOWrapper.HAS_CONFIDENCE_ELEMENT_ID);
        this.checkObjectProperty(wrapper, CIOWrapper.HAS_CONFIDENCE_LEVEL_ID);
        this.checkObjectProperty(wrapper, CIOWrapper.HAS_EVIDENCE_CONCORDANCE_ID);
        this.checkObjectProperty(wrapper, CIOWrapper.HAS_EVIDENCE_TYPE_CONCORDANCE_ID);
        this.checkObjectProperty(wrapper, CIOWrapper.PROVIDES_GREATER_CONF_THAN_ID);
        
        log.exit();
    }
    /**
     * Checks that there exists, in the ontology wrapped by {@code wrapper}, 
     * an {@code OWLClass} with the OBO-like ID {@code clsId}, not obsolete.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} wrapping the ontology to use.
     * @param clsId     A {@code String} that is the OBO-like ID of a class to retrieve.
     * @throws AssertionError   If there is no {@code OWLClass} with OBO-like ID {@code clsId}, 
     *                          or it is obsolete.
     */
    private void checkClass(OWLGraphWrapper wrapper, String clsId) {
        OWLClass clsTested = wrapper.getOWLClassByIdentifierNoAltIds(clsId);
        assertNotNull("Class with ID " + clsId + " could not be retrieved", clsTested);
        assertFalse("Class with ID " + clsId + " is obsolete", wrapper.isObsolete(clsTested));  
    }
    /**
     * Checks that there exists, in the ontology wrapped by {@code wrapper}, 
     * an {@code OWLObjectProperty} with the OBO-like ID {@code propId}, not obsolete.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} wrapping the ontology to use.
     * @param propId    A {@code String} that is the OBO-like ID of an object property to retrieve.
     * @throws AssertionError   If there is no {@code OWLObjectProperty} with OBO-like ID 
     *                          {@code propId}, or it is obsolete.
     */
    private void checkObjectProperty(OWLGraphWrapper wrapper, String propId) {
        OWLObjectProperty propTested = wrapper.getOWLObjectPropertyByIdentifier(propId);
        assertNotNull("Object property with ID " + propId + " could not be retrieved", 
                propTested);
        assertFalse("Object property with ID " + propId + " is obsolete", 
                wrapper.isObsolete(propTested));  
    }
}
