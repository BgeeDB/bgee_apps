package org.bgee.pipeline.gene;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO.MySQLTaxonTOResultSet;
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
    private static final String MAPPING_FILE = "/gene/fakeGeneMapping.csv";

    public ParseOrthoXMLTest(){
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link ParseOrthoXML#parseXML(String, String)}, which is the central method of the
     * class doing all the job.
     * @throws SQLException 
     * @throws IllegalStateException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldParseXML() 
            throws DAOException, XMLStreamException, XMLParseException, IOException, IllegalStateException, SQLException {
        log.debug("Testing if the OrthoXML file is parsed correctly..");

        // First, we need a mock MySQLDAOManager, for the class to acquire mock
        // MySQLGeneDAO and mock MySQLHierarchicalGroupDAO. This will allow to verify that
        // the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mockGetAllSpecies(mockManager);
        
        // We need a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa().
        MySQLTaxonTOResultSet mockTaxonTORs = mockGetAllTaxa(mockManager);

        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mockGetAllGenes(mockManager);

        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        parser.parseXML(this.getClass().getResource(OMAFILE).getFile(), null);

        // Generate the expected List of HierarchicalGroupTOs to verify the calls made 
        // to the DAO.
        List<HierarchicalGroupTO> expectedHGroupTOs = Arrays.asList(
                new HierarchicalGroupTO(1, "HOG:SVYPSSI", 1, 4, 117571),
                new HierarchicalGroupTO(2, "HOG:SVYPSSI", 2, 3, 0),
                new HierarchicalGroupTO(3, "HOG:HADISHS", 5, 6, 9604),
                new HierarchicalGroupTO(4, "HOG:AFFEFGG", 7, 18, 117571),
                new HierarchicalGroupTO(5, "HOG:AFFEFGG", 8, 11, 0),
                new HierarchicalGroupTO(6, "HOG:AFFEFGG", 9, 10, 186625),
                new HierarchicalGroupTO(7, "HOG:AFFEFGG", 12, 17, 32523),
                new HierarchicalGroupTO(8, "HOG:AFFEFGG", 13, 16, 32524),
                new HierarchicalGroupTO(9 , "HOG:AFFEFGG", 14, 15, 32525),
                new HierarchicalGroupTO(10, "HOG:RIQLVEE", 19, 28, 33213),
                new HierarchicalGroupTO(11, "HOG:RIQLVEE", 20, 21, 0),
                new HierarchicalGroupTO(12, "HOG:RIQLVEE", 22, 23, 1206794),
                new HierarchicalGroupTO(13, "HOG:RIQLVEE", 24, 25, 1206794),
                new HierarchicalGroupTO(14, "HOG:RIQLVEE", 26, 27, 1206794));

        ArgumentCaptor<Set> hGroupsTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockHierarchicalGroupDAO).insertHierarchicalGroups(
                hGroupsTOsArg.capture());
        assertTrue("Incorrect HierarchicalGroupTOs generated to insert hierarchical groups",
                TOComparator.areTOCollectionsEqual(expectedHGroupTOs, hGroupsTOsArg.getValue()));
        
        // Generate the expected List of GeneTOs to verify the calls made to the DAO.
        List<GeneTO> expectedGeneTOs = Arrays.asList(
                new GeneTO("ENSDARG00000087888", null, null, null, null, 1, null),
                new GeneTO("ENSG00000027681", null, null, null, null, 2, null),
                new GeneTO("PPYG00000014510", null, null, null, null, 3, null),
                new GeneTO("ENSDARG00000025613", null, null, null, null, 5, null),
                new GeneTO("ENSDARG00000089109", null, null, null, null, 6, null),
                new GeneTO("ENSDARG00000024124", null, null, null, null, 7, null),
                new GeneTO("ENSG00000171791", null, null, null, null, 7, null),
                new GeneTO("PPYG00000009212", null, null, null, null, 9, null),
                new GeneTO("ENSG00000005242", null, null, null, null, 9, null),
                new GeneTO("FBgn0003721", null, null, null, null, 11, null));

        ArgumentCaptor<Set> geneTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneDAO).updateGenes(geneTOsArg.capture(), 
                eq(Arrays.asList(GeneDAO.Attribute.OMA_PARENT_NODE_ID)));
        assertTrue("Incorrect GeneTOs generated to update genes",
                TOComparator.areTOCollectionsEqual(expectedGeneTOs, geneTOsArg.getValue()));
        
        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockTaxonTORs).close();
        verify(mockGeneTORs).close();

        // Verify that startTransaction() and commit()
        verify(mockManager.getConnection(), times(1)).startTransaction();
        verify(mockManager.getConnection(), times(1)).commit();

        //TODO check that the DAO was closed

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(
                SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.COMMON_NAME, 
                SpeciesDAO.Attribute.GENOME_SPECIES_ID, SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        verify(mockManager.mockTaxonDAO, times(1)).setAttributes(TaxonDAO.Attribute.ID);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(GeneDAO.Attribute.ID);
    }
    
    /**
     * Test {@link ParseOrthoXML#parseXML(String, String)}, which is the central method of the
     * class doing all the job.
     * @throws SQLException 
     * @throws IllegalStateException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldParseXMLWithMappingFile() 
            throws DAOException, XMLStreamException, XMLParseException, IOException, IllegalStateException, SQLException {
        log.debug("Testing if the OrthoXML file is parsed correctly..");

        // First, we need a mock MySQLDAOManager, for the class to acquire mock
        // MySQLGeneDAO and mock MySQLHierarchicalGroupDAO. This will allow to verify that
        // the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mockGetAllSpecies(mockManager);
        
        // We need a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa().
        MySQLTaxonTOResultSet mockTaxonTORs = mockGetAllTaxa(mockManager);

        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mockGetAllGenes(mockManager);

        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        parser.parseXML(this.getClass().getResource(OMAFILE).getFile(), 
                this.getClass().getResource(MAPPING_FILE).getFile());

        // Generate the expected List of HierarchicalGroupTOs to verify the calls made 
        // to the DAO.
        List<HierarchicalGroupTO> expectedHGroupTOs = Arrays.asList(
                new HierarchicalGroupTO(1, "HOG:SVYPSSI", 1, 4, 117571),
                new HierarchicalGroupTO(2, "HOG:SVYPSSI", 2, 3, 0),
                new HierarchicalGroupTO(3, "HOG:HADISHS", 5, 6, 9604),
                new HierarchicalGroupTO(4, "HOG:AFFEFGG", 7, 18, 117571),
                new HierarchicalGroupTO(5, "HOG:AFFEFGG", 8, 11, 0),
                new HierarchicalGroupTO(6, "HOG:AFFEFGG", 9, 10, 186625),
                new HierarchicalGroupTO(7, "HOG:AFFEFGG", 12, 17, 32523),
                new HierarchicalGroupTO(8, "HOG:AFFEFGG", 13, 16, 32524),
                new HierarchicalGroupTO(9 , "HOG:AFFEFGG", 14, 15, 32525),
                new HierarchicalGroupTO(10, "HOG:RIQLVEE", 19, 28, 33213),
                new HierarchicalGroupTO(11, "HOG:RIQLVEE", 20, 21, 0),
                new HierarchicalGroupTO(12, "HOG:RIQLVEE", 22, 23, 1206794),
                new HierarchicalGroupTO(13, "HOG:RIQLVEE", 24, 25, 1206794),
                new HierarchicalGroupTO(14, "HOG:RIQLVEE", 26, 27, 1206794));

        ArgumentCaptor<Set> hGroupsTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockHierarchicalGroupDAO).insertHierarchicalGroups(
                hGroupsTOsArg.capture());
        assertTrue("Incorrect HierarchicalGroupTOs generated to insert hierarchical groups",
                TOComparator.areTOCollectionsEqual(expectedHGroupTOs, hGroupsTOsArg.getValue()));
        
        // Generate the expected List of GeneTOs to verify the calls made to the DAO.
        List<GeneTO> expectedGeneTOs = Arrays.asList(
                new GeneTO("ENSDARG00000087888", null, null, null, null, 1, null),
                new GeneTO("ENSG00000027681", null, null, null, null, 2, null),
                new GeneTO("ENSG00000029527", null, null, null, null, 2, null),
                new GeneTO("PPYG00000014510", null, null, null, null, 3, null),
                new GeneTO("ENSPTRG00000010079", null, null, null, null, 3, null),
                new GeneTO("PPAG00000010079", null, null, null, null, 3, null),
                new GeneTO("ACAG00000010079", null, null, null, null, 3, null),
                new GeneTO("ENSDARG00000025613", null, null, null, null, 5, null),
                new GeneTO("ENSDARG00000089109", null, null, null, null, 6, null),
                new GeneTO("ENSDARG00000024124", null, null, null, null, 7, null),
                new GeneTO("ENSG00000171791", null, null, null, null, 7, null),
                new GeneTO("PPYG00000009212", null, null, null, null, 9, null),
                new GeneTO("ENSG00000005242", null, null, null, null, 9, null),
                new GeneTO("FBgn0003721", null, null, null, null, 11, null));

        ArgumentCaptor<Set> geneTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneDAO).updateGenes(geneTOsArg.capture(), 
                eq(Arrays.asList(GeneDAO.Attribute.OMA_PARENT_NODE_ID)));
        assertTrue("Incorrect GeneTOs generated to update genes",
                TOComparator.areTOCollectionsEqual(expectedGeneTOs, geneTOsArg.getValue()));
        
        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockTaxonTORs).close();
        verify(mockGeneTORs).close();

        // Verify that startTransaction() and commit()
        verify(mockManager.getConnection(), times(1)).startTransaction();
        verify(mockManager.getConnection(), times(1)).commit();

        //TODO check that the DAO was closed
        
        // Verify that setAttributes are correctly called.
        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(
                SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.COMMON_NAME, 
                SpeciesDAO.Attribute.GENOME_SPECIES_ID, SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        verify(mockManager.mockTaxonDAO, times(1)).setAttributes(TaxonDAO.Attribute.ID);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(GeneDAO.Attribute.ID);
    }

    /**
     * Test {@link ParseOrthoXML#parseXML(String, String)}, which is the central method of the
     * class doing all the job, throws error
     * @throws SQLException 
     * @throws IllegalStateException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldParseXMLFakePrefixError()
            throws DAOException, XMLStreamException, XMLParseException, IOException, IllegalStateException, SQLException {
        log.debug("Testing if the OrthoXML file is parsed correctly..");

        // First, we need a mock MySQLDAOManager, for the class to acquire mock
        // MySQLGeneDAO and mock MySQLHierarchicalGroupDAO. This will allow to verify that
        // the correct values were tried to be inserted into the database.
        MockDAOManager mockManager = new MockDAOManager();

        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mock(MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockSpeciesTORs.getTO()).thenReturn(
                // if genomeSpeciesId is not "0" or equal to speciesID
                // the fakeGeneIdPrefix shouldn't be empty
                new SpeciesTO("9600", "orangutan", null, null, null, null, "9601", ""),
                new SpeciesTO("6239", "c.elegans", null, null, null, null, "0", ""));
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 2;
            }
        });
        
        // We need a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa().
        MySQLTaxonTOResultSet mockTaxonTORs = mockGetAllTaxa(mockManager);

        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mockGetAllGenes(mockManager);

        try {
            ParseOrthoXML parser = new ParseOrthoXML(mockManager);
            parser.parseXML(this.getClass().getResource(OMAFILE).getFile(), null);
            // test failed
        } catch (IllegalArgumentException e) {
            //test passed
        }
        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockTaxonTORs).close();
        verify(mockGeneTORs).close();
        
        // Verify that startTransaction() and commit() never called
        verify(mockManager.getConnection(), times(0)).startTransaction();
        verify(mockManager.getConnection(), times(0)).commit();

        //TODO check that the DAO was closed
        
        // Verify that setAttributes are correctly called.
        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(
                SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.COMMON_NAME, 
                SpeciesDAO.Attribute.GENOME_SPECIES_ID, SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        verify(mockManager.mockTaxonDAO, times(1)).setAttributes(TaxonDAO.Attribute.ID);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(GeneDAO.Attribute.ID);

    }
    
    /**
     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private MySQLSpeciesTOResultSet mockGetAllSpecies(MockDAOManager mockManager) {
        
        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mock(MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        // Determine the behavior of consecutive calls to getTO().
        // Some species are presents in the fakeOMA file, and not in this list.
        when(mockSpeciesTORs.getTO()).thenReturn(
                // if genomeSpeciesId is equal to "0" or speciesId 
                // we don't take account fakeGeneIdPrefix
                new SpeciesTO("9606", "human", null, null, null, null, null, ""),
                new SpeciesTO("7955", "zebrafish", null, null, null, null, "7955", "TOTO"),
                new SpeciesTO("7227", "fruitfly", null, null, null, null, "0", "TATA"),
                new SpeciesTO("9598", "chimpanzee", null, null, null, null, "0", ""),
                new SpeciesTO("9597", "bonobo", null, null, null, null, "9598", "PPAG"),
                new SpeciesTO("9600", "orangutan", null, null, null, null, "9601", "PPYG"),
                new SpeciesTO("28377", "anolis", null, null, null, null, "9598", "ACAG"),
                new SpeciesTO("6239", "c.elegans", null, null, null, null, "0", ""));
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 7;
            }
        });
        
        return mockSpeciesTORs;
    }

    /**
     * Define a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private MySQLTaxonTOResultSet mockGetAllTaxa(MockDAOManager mockManager) {

        // We need a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa().
        MySQLTaxonTOResultSet mockTaxonTORs = mock(MySQLTaxonTOResultSet.class);
        when(mockManager.mockTaxonDAO.getAllTaxa()).thenReturn(mockTaxonTORs);
        // Determine the behavior of consecutive calls to getTO().
        // The taxon Sauria is present in the fakeOMA file, and not in this list.
        when(mockTaxonTORs.getTO()).thenReturn(
                new TaxonTO("9604", null, null, null, null, null, null),
                new TaxonTO("33213", null, null, null, null, null, null),
                new TaxonTO("32523", null, null, null, null, null, null),
                new TaxonTO("32524", null, null, null, null, null, null),
                new TaxonTO("32525", null, null, null, null, null, null),
                new TaxonTO("117571", null, null, null, null, null, null),
                new TaxonTO("186625", null, null, null, null, null, null),
                new TaxonTO("1206794", null, null, null, null, null, null));
        // Determine the behavior of consecutive calls to next().
        when(mockTaxonTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 7;
            }
        });
        return mockTaxonTORs;
    }

    /**
     * Define a mock MySQLGeneTOResultSet to mock the return of getAllGenes.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private MySQLGeneTOResultSet mockGetAllGenes(MockDAOManager mockManager) {

        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mock(MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getAllGenes()).thenReturn(mockGeneTORs);
        // Determine the behavior of consecutive calls to getTO(). 
        // Some genes are presents in the fakeOMA file, and not in this list.
        when(mockGeneTORs.getTO()).thenReturn(
                new GeneTO("ACAG00000010079", null, null, null, null, null, null),
                new GeneTO("ENSBTAG00000019302", null, null, null, null, null, null),
                new GeneTO("ENSGALG00000012885", null, null, null, null, null, null),
                new GeneTO("ENSDARG00000089109", null, null, null, null, null, null),
                new GeneTO("ENSDARG00000025613", null, null, null, null, null, null),
                new GeneTO("ENSDARG00000087888", null, null, null, null, null, null),
                new GeneTO("FBgn0003721", null, null, null, null, null, null),
                new GeneTO("ENSGGOG00000000790", null, null, null, null, null, null),
                new GeneTO("ENSGGOG00000002173", null, null, null, null, null, null),
                new GeneTO("ENSG00000268179", null, null, null, null, null, null),
                new GeneTO("ENSG00000171791", null, null, null, null, null, null),
                new GeneTO("ENSG00000027681", null, null, null, null, null, null),
                new GeneTO("ENSG00000005242", null, null, null, null, null, null),
                new GeneTO("ENSG00000029527", null, null, null, null, null, null),
                new GeneTO("ENSMMUG00000006577", null, null, null, null, null, null),
                new GeneTO("ENSMUSG00000057329", null, null, null, null, null, null),
                new GeneTO("ENSPTRG00000010079", null, null, null, null, null, null),
                new GeneTO("PPAG00000010079", null, null, null, null, null, null),
                new GeneTO("ENSSSCG00000004895", null, null, null, null, null, null),
                new GeneTO("PPYG00000009212", null, null, null, null, null, null),
                new GeneTO("PPYG00000014510", null, null, null, null, null, null),
                new GeneTO("ENSRNOG00000002791", null, null, null, null, null, null),
                new GeneTO("ENSTNIG00000000982", null, null, null, null, null, null),
                new GeneTO("ENSDARG00000024124", null, null, null, null, null, null));
        // Determine the behavior of consecutive calls to next().
        when(mockGeneTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is geneTO to return 
                return counter++ < 23;
            }
        });
        return mockGeneTORs;
    }
}
