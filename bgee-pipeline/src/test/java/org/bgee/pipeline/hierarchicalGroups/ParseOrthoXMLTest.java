package org.bgee.pipeline.hierarchicalGroups;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
    private final static Logger log = 
            LogManager.getLogger(ParseOrthoXMLTest.class.getName());

    private static final String OMAFILE = "/orthoxml/fakeOMA.orthoxml";

    public ParseOrthoXMLTest(){
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link ParseOrthoXML#parseXML(String)}, which is the central method of the
     * class doing all the job.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldParseXML() throws DAOException, FileNotFoundException,
            XMLStreamException, XMLParseException {
        log.debug("Testing if the OrthoXML file is parsed correctly..");

        // First, we need a mock MySQLDAOManager, for the class to acquire mock
        // MySQLGeneDAO and mock MySQLHierarchicalGroupDAO. This will allow to verify that
        // the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mock(MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        // Determine the behavior of consecutive calls to getTO().
        
        //TODO: test should be much much simpler, do not use all species in Bgee.
        when(mockSpeciesTORs.getTO()).thenReturn(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "1", 
                        "path/file9606", "9606", ""),
                new SpeciesTO("10090", "mouse", "Mus", "musculus", "2",
                        "path/file9606", "10090", ""),
                new SpeciesTO("7955", "zebrafish", "Danio", "rerio", "3", 
                        "path/file9606", "7955", ""),
                new SpeciesTO("8364", "xenopus", "Xenopus", "tropicalis", "4", 
                        "path/file9606", "8364", ""),
                new SpeciesTO("7227", "fruitfly", "Drosophila", "melanogaster", "5", 
                        "path/file9606", "7227", ""),
                new SpeciesTO("9031", "chicken", "Gallus", "gallus", "6", 
                        "path/file9606", "9031", ""),
                new SpeciesTO("9593", "gorilla", "Gorilla", "gorilla", "7", 
                        "path/file9606", "9593", ""),
                new SpeciesTO("9544", "macaque", "Macaca", "mulatta", "8", 
                        "path/file9606", "9544", ""),
                new SpeciesTO("13616", "opossum", "Monodelphis", "domestica", "9", 
                        "path/file9606", "13616", ""),
                new SpeciesTO("9258", "platypus", "Ornithorhynchus", "anatinus", 
                        "10", "path/file9606", "9258", ""),
                new SpeciesTO("9598", "chimpanzee", "Pan", "troglodytes", "11", 
                        "path/file9606", "9598", ""),
                new SpeciesTO("9597", "bonobo", "Pan", "paniscus", "12", 
                        "path/file9606", "9598", "PPAG"),
                new SpeciesTO("9600", "orangutan", "Pongo", "pygmaeus", "13", 
                        "path/file9606", "9601", "PPYG"),
                new SpeciesTO("9913", "cow", "Bos", "taurus", "14", 
                        "path/file9606", "9913", ""),
                new SpeciesTO("10116", "rat", "Rattus", "norvegicus", "15", 
                        "path/file9606", "10116", ""),
                new SpeciesTO("28377", "anolis", "Anolis", "carolinensis", "16", 
                        "path/file9606", "28377", ""),
                new SpeciesTO("99883", "tetraodon", "Tetraodon", "nigroviridis", "17", 
                        "path/file9606", "99883", ""),
                new SpeciesTO("9823", "pig", "Sus", "scrofa", "18", 
                        "path/file9606", "9823", ""),
                new SpeciesTO("6239", "c.elegans", "Caenorhabditis", "elegans", "19", 
                        "path/file9606", "6239", ""));
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            //TODO: hmm, isn't currentIndex reinitialize at each call to thenAnswer?
            int currentIndex = -1;
            @SuppressWarnings("unused")
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Return true while there is speciesTO to return 
                return currentIndex++ < 19;
            }
        });

        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mock(MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getAllGenes()).thenReturn(mockGeneTORs);
        // Determine the behavior of consecutive calls to getTO().
        //TODO: are there some genes present in the fakeOMA file, and not in this list?
        when(mockGeneTORs.getTO()).thenReturn(
                new GeneTO("ENSACAG00000017588", "NAME17588", "DESC17588", 28377, 12, 0, true),
                new GeneTO("ENSACAG00000017588", "NAME17588", "DESC17588", 28377, 12, 0, true),
                new GeneTO("ENSBTAG00000019302", "NAME19302", "DESC19302", 9913, 12, 0, true),
                new GeneTO("ENSGALG00000012885", "NAME12885", "DESC12885", 9031, 12, 0, true),
                new GeneTO("ENSDARG00000089109", "NAME89109", "DESC89109", 7955, 12, 0, true),
                new GeneTO("ENSDARG00000025613", "NAME25613", "DESC25613", 7955, 12, 0, true),
                new GeneTO("ENSDARG00000087888", "NAME87888", "DESC87888", 7955, 12, 0, true),
                new GeneTO("FBgn0003721", "NAME3721", "DESC3721", 7227, 12, 0, true),
                new GeneTO("ENSGGOG00000000790", "NAME790", "DESC790", 9595, 12, 0, true),
                new GeneTO("ENSGGOG00000002173", "NAME2173", "DESC2173", 9595, 12, 0, true),
                new GeneTO("ENSG00000171791", "NAME171791", "DESC171791", 9606, 12, 0, true),
                new GeneTO("ENSMMUG00000006577", "NAME06577", "DESC06577", 9544, 12, 0, true),
                new GeneTO("ENSMODG00000027681", "NAME27681", "DESC27681", 13616, 12, 0, true),
                new GeneTO("ENSMODG00000005242", "NAME05242", "DESC05242", 13616, 12, 0, true),
                new GeneTO("ENSMODG00000029527", "NAME29527", "DESC29527", 13616, 12, 0, true),
                new GeneTO("ENSMUSG00000057329", "NAME57329", "DESC57329", 10090, 12, 0, true),
                new GeneTO("ENSPTRG00000010079", "NAME10079", "DESC10079", 9598, 12, 0, true),
                new GeneTO("PPAG00000010079", "NAME10079", "DESC10079", 9598, 12, 0, false),
                new GeneTO("ENSSSCG00000004895", "NAME04895", "DESC04895", 9823, 12, 0, true),
                new GeneTO("PPYG00000009212", "NAME09212", "DESC09212", 9601, 12, 0, false),
                new GeneTO("PPYG00000014510", "NAME14510", "DESC14510", 9601, 12, 0, false),
                new GeneTO("ENSRNOG00000002791", "NAME02791", "DESC02791", 10116, 12, 0, true),
                new GeneTO("ENSTNIG00000000982", "NAME00982", "DESC00982", 99883, 12, 0, true),
                new GeneTO("ENSXETG00000024124", "NAME24124", "DESC24124", 8364, 12, 0, true));
        // Determine the behavior of consecutive calls to next().
        when(mockGeneTORs.next()).thenAnswer(new Answer<Boolean>() {
            //TODO: hmm, isn't currentIndex reinitialize at each call to thenAnswer?
            int currentIndex = -1;
            @SuppressWarnings("unused")
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Return true while there is geneTO to return 
                return currentIndex++ < 24;
            }
        });

        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        parser.parseXML(this.getClass().getResource(OMAFILE).getFile());

        // Generate the expected List of HierarchicalGroupTOs to verify the calls made 
        // to the DAO.
        List<HierarchicalGroupTO> expectedHGroupTOs = Arrays.asList(
                new HierarchicalGroupTO(1, "HOG:SVYPSSI", 1, 4, 117571),
                new HierarchicalGroupTO(2, "HOG:SVYPSSI", 2, 3, 0),
                new HierarchicalGroupTO(3, "HOG:HADISHS", 5, 6, 9604),
                new HierarchicalGroupTO(4, "HOG:AFFEFGG", 7, 20, 117571),
                new HierarchicalGroupTO(5, "HOG:AFFEFGG", 8, 11, 0),
                new HierarchicalGroupTO(6, "HOG:AFFEFGG", 9, 10, 186625),
                new HierarchicalGroupTO(7, "HOG:AFFEFGG", 12, 19, 32523),
                new HierarchicalGroupTO(8, "HOG:AFFEFGG", 13, 18, 32524),
                new HierarchicalGroupTO(9 , "HOG:AFFEFGG", 14, 15, 32525),
                new HierarchicalGroupTO(10, "HOG:AFFEFGG", 16, 17, 32561),
                new HierarchicalGroupTO(11, "HOG:RIQLVEE", 21, 30, 33213),
                new HierarchicalGroupTO(12, "HOG:RIQLVEE", 22, 23, 0),
                new HierarchicalGroupTO(13, "HOG:RIQLVEE", 24, 25, 1206794),
                new HierarchicalGroupTO(14, "HOG:RIQLVEE", 26, 27, 1206794),
                new HierarchicalGroupTO(15, "HOG:RIQLVEE", 28, 29, 1206794));

        ArgumentCaptor<Set> hGroupsTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockHierarchicalGroupDAO).insertHierarchicalGroups(
                hGroupsTOsArg.capture());
        if (!this.areHGroupTOCollectionsEqual(expectedHGroupTOs, hGroupsTOsArg.getValue())) {
            throw new AssertionError("Incorrect HierarchicalGroupTOs generated to insert "
                    + "hierarchical groups, expected " + expectedHGroupTOs.toString() + 
                    ", but was " + hGroupsTOsArg.getValue());
        }
        
        // Generate the expected List of GeneTOs to verify the calls made to the DAO.
        List<GeneTO> expectedGeneTOs = Arrays.asList(
                new GeneTO("ENSDARG00000087888", "", "", 0, 0, 1, true),
                new GeneTO("ENSMODG00000027681", "", "", 0, 0, 2, true),
                new GeneTO("ENSMODG00000029527", "", "", 0, 0, 2, true),
                new GeneTO("ENSGGOG00000002173", "", "", 0, 0, 3, true),
                new GeneTO("PPYG00000014510", "", "", 0, 0, 3, true),
                new GeneTO("ENSPTRG00000010079", "", "", 0, 0, 3, true),
                new GeneTO("PPAG00000010079", "", "", 0, 0, 3, true),
                new GeneTO("ENSDARG00000025613", "", "", 0, 0, 5, true),
                new GeneTO("ENSTNIG00000000982", "", "", 0, 0, 6, true),
                new GeneTO("ENSDARG00000089109", "", "", 0, 0, 6, true),
                new GeneTO("ENSXETG00000024124", "", "", 0, 0, 7, true),
                new GeneTO("ENSG00000171791", "", "", 0, 0, 7, true),
                new GeneTO("PPYG00000009212", "", "", 0, 0, 9, true),
                new GeneTO("ENSMODG00000005242", "", "", 0, 0, 9, true),
                new GeneTO("ENSGALG00000012885", "", "", 0, 0, 10, true),
                new GeneTO("ENSACAG00000017588", "", "", 0, 0, 10, true),
                new GeneTO("FBgn0003721", "", "", 0, 0, 12, true));

        ArgumentCaptor<Set> geneTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneDAO).updateGenes(geneTOsArg.capture(), anyList());//TODO: why anyList?
        if (!this.areGeneTOCollectionsEqual(expectedGeneTOs, geneTOsArg.getValue())) {
            throw new AssertionError("Incorrect GeneTOs generated to update genes, "+
                    "expected " + expectedGeneTOs + ", but was " + geneTOsArg.getValue());
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
    //TODO: we should externalize all these TO comparisons in an external util class.
    private boolean areGeneTOCollectionsEqual(List<GeneTO> cGeneTO1, Set<GeneTO> cGeneTO2) {
        log.entry(cGeneTO1, cGeneTO2);
        
        if (cGeneTO1.size() != cGeneTO2.size()) {
            log.debug("Non matching sizes, {} - {}", cGeneTO1.size(), cGeneTO2.size());
            return log.exit(false);
        }
        for (GeneTO g1: cGeneTO1) {
            boolean found = false;
            for (GeneTO g2: cGeneTO2) {
                if (areGeneTOEqual(g1, g2)) {
                    found = true;    
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent gene found for {}", g1.getId());
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
        log.debug("Genes are not equivalent");
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
           List<HierarchicalGroupTO> cHGroupTO1, Set<HierarchicalGroupTO> cHGroupTO2) {
        log.entry(cHGroupTO1, cHGroupTO2);
        
        if (cHGroupTO1.size() != cHGroupTO2.size()) {
            log.debug("Non matching sizes, {} - {}", cHGroupTO1.size(), cHGroupTO2.size());
            return log.exit(false);
        }
        for (HierarchicalGroupTO hg1: cHGroupTO1) {
            boolean found = false;
            for (HierarchicalGroupTO hg2: cHGroupTO2) {
                if (areHierarchicalGroupTOEqual(hg1, hg2)) {
                    found = true;   
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent hierarchical group found for {}", hg1.getId());
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }
    
    /**
     * Method to compare two {@code HierarchicalGroupTO}s, to check for complete equality
     * of each attribute. This is because the {@code equals} method of
     * {@code HierarchicalGroupTO}s is solely based on their ID, not on other attributes.
     * 
     * @param hg1   A {@code HierarchicalGroupTO}s to be compared to {@code hg1}.
     * @param hg2   A {@code HierarchicalGroupTO}s to be compared to {@code hg1}.
     * @return      {@code true} if {@code hg1} and {@code hg1} have all attributes equal.
     */
    private boolean areHierarchicalGroupTOEqual(
            HierarchicalGroupTO hg1, HierarchicalGroupTO hg2) {
        log.entry(hg1, hg2);
        if ((hg1.getId() == null && hg2.getId() == null || 
                hg1.getId() != null && hg1.getId().equals(hg2.getId())) && 
            (hg1.getName() == null && hg2.getName() == null || 
                hg1.getName() != null && hg1.getName().equals(hg2.getName())) && 
             hg1.getOMAGroupId().equals(hg2.getOMAGroupId()) && 
             hg1.getLeftBound() == hg2.getLeftBound() && 
             hg1.getRightBound() == hg2.getRightBound() && 
             hg1.getTaxonId() == hg2.getTaxonId()) {
            return log.exit(true);
        }
        log.debug("Hierarchical Group are not equivalent");
        return log.exit(false);
    }

}
