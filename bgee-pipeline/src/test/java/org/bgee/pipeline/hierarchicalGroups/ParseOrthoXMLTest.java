package org.bgee.pipeline.hierarchicalGroups;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.hierarchicalgroup.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;


/**
 * Tests the functions of {@link #org.bgee.pipeline.hierarchicalGroups.ParseOrthoXML}
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class ParseOrthoXMLTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(ParseOrthoXMLTest.class.getName());

    private static final String OMAFILE = "/orthoxml/fakeOMA.orthoxml";

    public ParseOrthoXMLTest(){
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link ParseOrthoXML#parseXML(String)}, which is 
     * the central method of the class doing all the job.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testParseXML() throws DAOException, FileNotFoundException,
            XMLStreamException, XMLParseException {
        log.debug("Testing if the OrthoXML file is parsed correctly..");

        // First, we need a mock MySQLGeneDAO to mock the return of getAllGenes() method.
        MySQLGeneDAO dao = mock(MySQLGeneDAO.class);
        GeneTO geneTO1 = new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true);
        GeneTO geneTO2 = new GeneTO("ID2", "genN2", "genDesc2", 21, 0, 0, true);
        GeneTO geneTO3 = new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false);
        List<GeneTO> expectedList = new ArrayList<GeneTO>();
        expectedList.add(geneTO1);
        expectedList.add(geneTO2);
        expectedList.add(geneTO3);
        // TODO finish the mock
        // when(dao.getAllGenes()).thenReturn(expectedList);

        // Second, we need a mock MySQLDAOManager, for the class to acquire mock
        // MySQLGeneDAO. This will allow to verify that the correct values were tried to
        // be inserted and inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();
        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        parser.parseXML(ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());

        // Generate the expected Sets of GeneTOs to verify the calls made to the DAO.
        Set<GeneTO> expectedGeneTOs = new HashSet<GeneTO>();
        // TODO create set

        ArgumentCaptor<Set> geneTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneDAO).updateGenes(
                geneTOsArg.capture(), Arrays.asList(GeneDAO.Attribute.OMAPARENTNODEID));
        if (!this.areGeneTOCollectionsEqual(expectedGeneTOs, geneTOsArg.getValue())) {
            throw new AssertionError("Incorrect HierarchicalGroupTOs generated to insert "
                    + "hierarchical groups, expected " + expectedGeneTOs.toString() + 
                    ", but was " + geneTOsArg.getValue());
        }

        // Generate the expected Sets of HierarchicalGroupTOs to verify the calls made 
        // to the DAO.
        Set<HierarchicalGroupTO> expectedHGroupTOs = new HashSet<HierarchicalGroupTO>();
        //TODO create set

        ArgumentCaptor<Set> hGroupsTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockHierarchicalGroupDAO).insertHierarchicalGroups(
                hGroupsTOsArg.capture());
        if (!this.areHGroupTOCollectionsEqual(
                expectedHGroupTOs, hGroupsTOsArg.getValue())) {
            throw new AssertionError("Incorrect HierarchicalGroupTOs generated to insert "
                    + "hierarchical groups, expected " + expectedHGroupTOs.toString() + 
                    ", but was " + hGroupsTOsArg.getValue());
        }
    }

    /**
     * Method to compare two {@code Collection}s of {@code GeneTO}s, to check for complete
     * equality of each attribute of each {@code GeneTO} calling {@link #areGeneTOEqual()}.
     * This is because the {@code equals} method of {@code GeneTO}s is solely based on
     * their ID, not on other attributes.
     * 
     * @param cGeneTO1 A {@code Collection} of {@code GeneTO}s to be compared to
     *            {@code cGeneTO2}.
     * @param cGeneTO2 A {@code Collection} of {@code GeneTO}s to be compared to
     *            {@code cGeneTO1}.
     * @return {@code true} if {@code cGeneTO1} and {@code cGeneTO2} contain the same
     *         number of {@code GeneTO}s, and each {@code GeneTO} of a {@code Collection}
     *         has an equivalent {@code GeneTO} in the other {@code Collection}, with all
     *         attributes equal.
     */
    private boolean areGeneTOCollectionsEqual(Set<GeneTO> cGeneTO1, Set<GeneTO> cGeneTO2) {
        log.entry(cGeneTO1, cGeneTO2);
        
        if (cGeneTO1.size() != cGeneTO2.size()) {
            log.debug("Non matching sizes, {} - {}", cGeneTO1.size(), cGeneTO2.size());
            return log.exit(false);
        }
        for (GeneTO g1: cGeneTO1) {
            boolean found = false;
            for (GeneTO g2: cGeneTO2) {
                log.trace("Comparing {} to {}", g1, g2);
                if (areGeneTOEqual(g1, g2)) {
                    found = true;    
                }
            }
            if (!found) {
                log.debug("No equivalent gene found for {}", g1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }
    
    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GeneTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param g1    A {@code GeneTO}s to be compared to {@code g2}.
     * @param g2    A {@code GeneTO}s to be compared to {@code g1}.
     * @return      {@code true} if {@code g1} and {@code g2} have all attributes equal.
     */
    private boolean areGeneTOEqual(GeneTO g1, GeneTO g2) {
        log.entry(g1, g2);
        if ((g1.getId() == null && g2.getId() == null || 
                g1.getId() != null && g1.getId().equals(g2.getId())) && 
            (g1.getName() == null && g2.getName() == null || 
                    g1.getName() != null && g1.getName().equals(g2.getName())) && 
             g1.getSpeciesId() == g2.getSpeciesId() && 
             g1.getGeneBioTypeId() == g2.getGeneBioTypeId() && 
             g1.getOMAParentNodeId() == g2.getOMAParentNodeId() && 
             g1.isEnsemblGene() == g2.isEnsemblGene()) {
            return log.exit(true);
        }
        log.debug("Genes are not equivalent {}", g1.getOMAParentNodeId());
        return log.exit(false);
    }

    /**
     * Method to compare two {@code Collection}s of {@code HierarchicalGroupTO}s, to check
     * for complete equality of each attribute of each {@code HierarchicalGroupTO}. This
     * is because the {@code equals} method of {@code HierarchicalGroupTO}s is solely
     * based on their ID, not on other attributes. Here we check for equality of each
     * attribute.
     * 
     * @param cHGroupTO1    A {@code Collection} of {@code HierarchicalGroupTO}s o be
     *                      compared to {@code cHGroupTO2}.
     * @param cHGroupTO2    A {@code Collection} of {@code HierarchicalGroupTO}s o be
     *                      compared to {@code cHGroupTO1}.
     * @return              {@code true} if {@code cHGroupTO1} and {@code cHGroupTO2} 
     *                      contain the same number of {@code HierarchicalGroupTO}s, and 
     *                      each {@code HierarchicalGroupTO} of a {@code Collection} has 
     *                      an equivalent {@code HierarchicalGroupTO} in the other 
     *                      {@code Collection}, with all attributes equal.
     */
    private boolean areHGroupTOCollectionsEqual(
            Set<HierarchicalGroupTO> cHGroupTO1, Set<HierarchicalGroupTO> cHGroupTO2) {
        log.entry(cHGroupTO1, cHGroupTO2);
        
        if (cHGroupTO1.size() != cHGroupTO2.size()) {
            log.debug("Non matching sizes, {} - {}", cHGroupTO1.size(), cHGroupTO2.size());
            return log.exit(false);
        }
        for (HierarchicalGroupTO hg1: cHGroupTO1) {
            boolean found = false;
            for (HierarchicalGroupTO hg2: cHGroupTO2) {
                log.trace("Comparing {} to {}", hg1, hg2);
                if ((hg1.getId() == null && hg2.getId() == null || 
                        hg1.getId() != null && hg1.getId().equals(hg2.getId())) && 
                    (hg1.getName() == null && hg2.getName() == null || 
                        hg1.getName() != null && hg1.getName().equals(hg2.getName())) && 
                     hg1.getOMAGroupId() == hg2.getOMAGroupId() && 
                     hg1.getNodeLeftBound() == hg2.getNodeLeftBound() && 
                     hg1.getNodeRightBound() == hg2.getNodeRightBound() && 
                     hg1.getNcbiTaxonomyId() == hg2.getNcbiTaxonomyId()) {
                    found = true;    
                }
            }
            if (!found) {
                log.debug("No equivalent hierarchical group found for {}", hg1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }

    /**
     * Test {@link ParseOrthoXML#generateTOsFromFile()}.
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws XMLParseException 
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     * @throws DAOException 
     */
    @SuppressWarnings("unchecked")
//    @Test
    public void testGenerateTOs() throws NoSuchFieldException, SecurityException,
                    IllegalArgumentException, IllegalAccessException,
                    NoSuchMethodException, InvocationTargetException, DAOException,
                    FileNotFoundException, XMLStreamException, XMLParseException {
        log.entry();
        // MySQLGeneDAO mockDao = Mockito.mock(MySQLGeneDAO.class);
        // GeneTOResultSet mockedGeneRs = Mockito.mock(GeneTOResultSet.class);
        // when(mockDao.getAllGenes()).thenReturn(mockedGeneRs);
        // mockDao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        // GeneTOResultSet methResults = mockDao.getAllGenes();

        // Mock getAllGenes()

        MySQLGeneDAO dao = mock(MySQLGeneDAO.class);
        GeneTO geneTO1 = new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true);
        GeneTO geneTO2 = new GeneTO("ID2", "genN2", "genDesc2", 21, 0, 0, true);
        GeneTO geneTO3 = new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false);
        List<GeneTO> expectedList = new ArrayList<GeneTO>();
        expectedList.add(geneTO1);
        expectedList.add(geneTO2);
        expectedList.add(geneTO3);

        // when(dao.getAllGenes()).thenReturn(expectedList);

        // Expected HierarchicalGroupTOs
        // First group
        HierarchicalGroupTO hierarchicalGroupTO1 = 
                new HierarchicalGroupTO(1, 1, 1, 4, "Euteleostomi");
        HierarchicalGroupTO hierarchicalGroupTO2 = 
                new HierarchicalGroupTO(2, 1, 2, 3, null);
        // Second group
        HierarchicalGroupTO hierarchicalGroupTO3 = 
                new HierarchicalGroupTO(3, 2, 5, 24, "Euteleostomi");
        HierarchicalGroupTO hierarchicalGroupTO4 = 
                new HierarchicalGroupTO(4, 2, 6, 11, "Vertebrata");
        HierarchicalGroupTO hierarchicalGroupTO5 = 
                new HierarchicalGroupTO(5, 2, 7, 8, null);
        HierarchicalGroupTO hierarchicalGroupTO6 = 
                new HierarchicalGroupTO(6, 2, 9, 10, null);
        HierarchicalGroupTO hierarchicalGroupTO7 = 
                new HierarchicalGroupTO(7, 2, 11, 14, null);
        HierarchicalGroupTO hierarchicalGroupTO8 =
                new HierarchicalGroupTO(8, 2, 12, 13, "Tetrapoda");
        HierarchicalGroupTO hierarchicalGroupTO9 = 
                new HierarchicalGroupTO(9, 2, 14, 21, null);
        HierarchicalGroupTO hierarchicalGroupTO10 = 
                new HierarchicalGroupTO(10, 2, 15, 18, "Cladistia");
        HierarchicalGroupTO hierarchicalGroupTO11 =
                new HierarchicalGroupTO(11, 2, 16, 17, null);
        HierarchicalGroupTO hierarchicalGroupTO12 = 
                new HierarchicalGroupTO(12, 2, 18, 19, null);
        // Third group
        HierarchicalGroupTO hierarchicalGroupTO13 = 
                new HierarchicalGroupTO(13, 3, 21, 24, "Chordata");
        HierarchicalGroupTO hierarchicalGroupTO14 = 
                new HierarchicalGroupTO(14, 3, 22, 23, null);
        Set<HierarchicalGroupTO> expectedTOs = new HashSet<HierarchicalGroupTO>();
        expectedTOs.add(hierarchicalGroupTO1);
        expectedTOs.add(hierarchicalGroupTO2);
        expectedTOs.add(hierarchicalGroupTO3);
        expectedTOs.add(hierarchicalGroupTO4);
        expectedTOs.add(hierarchicalGroupTO5);
        expectedTOs.add(hierarchicalGroupTO6);
        expectedTOs.add(hierarchicalGroupTO7);
        expectedTOs.add(hierarchicalGroupTO8);
        expectedTOs.add(hierarchicalGroupTO9);
        expectedTOs.add(hierarchicalGroupTO10);
        expectedTOs.add(hierarchicalGroupTO11);
        expectedTOs.add(hierarchicalGroupTO12);
        expectedTOs.add(hierarchicalGroupTO13);
        expectedTOs.add(hierarchicalGroupTO14);

        MockDAOManager mockManager = new MockDAOManager();
        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        parser.parseXML(ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());

        Field genesInDb = parser.getClass().getDeclaredField("genesInDb");
        genesInDb.setAccessible(true);
        genesInDb.set(parser, Arrays.asList("ENSDARG00000039453", "ENSDARG00000069839",
                "ENSDARG00000078198", "ENSDARG00000069837", "ENSDARG00000078286",
                "ENSDARG00000075443", "ENSDARG00000079599", "ENSG00000211633",
                "ENSG00000242580", "ENSG00000211655", "ENSMUSG00000050742",
                "ENSXETG00000024927", "ENSXETG00000021946", "ENSXETG00000021946",
                "ENSXETG00000030835"));
        
        Method methodGenerateTOs = parser.getClass().getDeclaredMethod(
                "generateTOsFromFile", String.class);
        methodGenerateTOs.setAccessible(true);
        methodGenerateTOs.invoke(parser, 
                ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());
        
        Field hierarchicalGroupTOs = parser.getClass().
                getDeclaredField("hierarchicalGroupTOs");
        hierarchicalGroupTOs.setAccessible(true);
        Set<HierarchicalGroupTO> setTO = 
                (Set<HierarchicalGroupTO>) hierarchicalGroupTOs.get(parser);
        for (Iterator<HierarchicalGroupTO> iterator = setTO.iterator(); 
                iterator.hasNext();) {
            HierarchicalGroupTO hierarchicalGroupTO = iterator.next();
            
            log.debug("OMANodeId={}, OMAGroupId={}, left={}, right={}, taxRange={}", 
                    hierarchicalGroupTO.getNodeId(), hierarchicalGroupTO.getOMAGroupId(),
                    hierarchicalGroupTO.getNodeLeftBound(), 
                    hierarchicalGroupTO.getNodeRightBound(),
                    hierarchicalGroupTO.getNcbiTaxonomyId());
            hierarchicalGroupTO.getNodeId();

        }
        log.exit();
    }
    
    public void testGetSpecies() {
    }
}
