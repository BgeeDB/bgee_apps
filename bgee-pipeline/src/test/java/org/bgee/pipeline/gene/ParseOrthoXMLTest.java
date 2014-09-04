package org.bgee.pipeline.gene;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
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
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
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
        
        // Some species are presents in the fakeOMA file, and not in this list.
        when(mockSpeciesTORs.getTO()).thenReturn(
                new SpeciesTO("9606", "human", "Homo", "sapiens", "1", 
                        "path/file9606", "9606", ""),
                new SpeciesTO("7955", "zebrafish", "Danio", "rerio", "3", 
                        "path/file9606", "7955", ""),
                new SpeciesTO("7227", "fruitfly", "Drosophila", "melanogaster", "5", 
                        "path/file9606", "7227", ""),
                new SpeciesTO("9598", "chimpanzee", "Pan", "troglodytes", "11", 
                        "path/file9606", "9598", ""),
                new SpeciesTO("9597", "bonobo", "Pan", "paniscus", "12", 
                        "path/file9606", "9598", "PPAG"),
                new SpeciesTO("9600", "orangutan", "Pongo", "pygmaeus", "13", 
                        "path/file9606", "9601", "PPYG"),
                new SpeciesTO("28377", "anolis", "Anolis", "carolinensis", "16", 
                        "path/file9606", "9598", "ACAG"),
                new SpeciesTO("6239", "c.elegans", "Caenorhabditis", "elegans", "19", 
                        "path/file9606", "6239", ""));
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 7;
            }
        });
        
        // We need a mock MySQLTaxonTOResultSet to mock the return of getAllTaxa().
        MySQLTaxonTOResultSet mockTaxonTORs = mock(MySQLTaxonTOResultSet.class);
        when(mockManager.mockTaxonDAO.getAllTaxa()).thenReturn(mockTaxonTORs);
        // Determine the behavior of consecutive calls to getTO().
        // The taxon Sauria is present in the fakeOMA file, and not in this list.
        when(mockTaxonTORs.getTO()).thenReturn(
                new TaxonTO("9604", "Hominidae", "taxCName9604", 1, 10, 1, false),
                new TaxonTO("33213", "Bilateria", "taxCName33213", 2, 3, 2, true),
                new TaxonTO("32523", "Tetrapoda", "Tetrapoda", 7, 8, 1, true),
                new TaxonTO("32524", "Amniota", "taxCName32524", 7, 8, 1, true),
                new TaxonTO("32525", "Theria", "taxCName32525", 5, 6, 1, false),
                new TaxonTO("117571", "Euteleostomi", "taxCName117571", 7, 8, 1, true),
                new TaxonTO("186625", "Clupeocephala", "taxCName186625", 7, 8, 1, true),
                new TaxonTO("1206794", "Ecdysozoa", "taxCName1206794", 1, 10, 1, false));
        // Determine the behavior of consecutive calls to next().
        when(mockTaxonTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 7;
            }
        });


        // We need a mock MySQLGeneTOResultSet to mock the return of getAllGenes().
        MySQLGeneTOResultSet mockGeneTORs = mock(MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getAllGenes()).thenReturn(mockGeneTORs);
        // Determine the behavior of consecutive calls to getTO(). 
        // Some genes are presents in the fakeOMA file, and not in this list.
        when(mockGeneTORs.getTO()).thenReturn(
                new GeneTO("ACAG00000010079", "NAME10079", "DESC10079", 28377, 12, 0, true),
                new GeneTO("ENSBTAG00000019302", "NAME19302", "DESC19302", 9913, 12, 0, true),
                new GeneTO("ENSGALG00000012885", "NAME12885", "DESC12885", 9031, 12, 0, true),
                new GeneTO("ENSDARG00000089109", "NAME89109", "DESC89109", 7955, 12, 0, true),
                new GeneTO("ENSDARG00000025613", "NAME25613", "DESC25613", 7955, 12, 0, true),
                new GeneTO("ENSDARG00000087888", "NAME87888", "DESC87888", 7955, 12, 0, true),
                new GeneTO("FBgn0003721", "NAME3721", "DESC3721", 7227, 12, 0, true),
                new GeneTO("ENSGGOG00000000790", "NAME790", "DESC790", 9595, 12, 0, true),
                new GeneTO("ENSGGOG00000002173", "NAME2173", "DESC2173", 9595, 12, 0, true),
                new GeneTO("ENSG00000268179", "NAME268179", "DESC268179", 9606, 12, 0, true),
                new GeneTO("ENSG00000171791", "NAME171791", "DESC171791", 9606, 12, 0, true),
                new GeneTO("ENSG00000027681", "NAME27681", "DESC27681", 9606, 12, 0, true),
                new GeneTO("ENSG00000005242", "NAME05242", "DESC05242", 9606, 12, 0, true),
                new GeneTO("ENSG00000029527", "NAME29527", "DESC29527", 9606, 12, 0, true),
                new GeneTO("ENSMMUG00000006577", "NAME06577", "DESC06577", 9544, 12, 0, true),
                new GeneTO("ENSMUSG00000057329", "NAME57329", "DESC57329", 10090, 12, 0, true),
                new GeneTO("ENSPTRG00000010079", "NAME10079", "DESC10079", 9598, 12, 0, true),
                new GeneTO("PPAG00000010079", "NAME10079", "DESC10079", 9598, 12, 0, false),
                new GeneTO("ENSSSCG00000004895", "NAME04895", "DESC04895", 9823, 12, 0, true),
                new GeneTO("PPYG00000009212", "NAME09212", "DESC09212", 9601, 12, 0, false),
                new GeneTO("PPYG00000014510", "NAME14510", "DESC14510", 9601, 12, 0, false),
                new GeneTO("ENSRNOG00000002791", "NAME02791", "DESC02791", 10116, 12, 0, true),
                new GeneTO("ENSTNIG00000000982", "NAME00982", "DESC00982", 99883, 12, 0, true),
                new GeneTO("ENSDARG00000024124", "NAME24124", "DESC24124", 8364, 12, 0, true));
        // Determine the behavior of consecutive calls to next().
        when(mockGeneTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = -1;
            public Boolean answer(@SuppressWarnings("unused") InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is geneTO to return 
                return counter++ < 23;
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
        if (!TOComparator.areHGroupTOCollectionsEqual(expectedHGroupTOs, hGroupsTOsArg.getValue())) {
            throw new AssertionError("Incorrect HierarchicalGroupTOs generated to insert "
                    + "hierarchical groups, expected " + expectedHGroupTOs.toString() + 
                    ", but was " + hGroupsTOsArg.getValue());
        }
        
        // Generate the expected List of GeneTOs to verify the calls made to the DAO.
        List<GeneTO> expectedGeneTOs = Arrays.asList(
                new GeneTO("ENSDARG00000087888", "", "", 0, 0, 1, true),                
                new GeneTO("ENSG00000027681", "", "", 0, 0, 2, true),
                new GeneTO("ENSG00000029527", "", "", 0, 0, 2, true),                
                new GeneTO("PPYG00000014510", "", "", 0, 0, 3, true),
                new GeneTO("ENSPTRG00000010079", "", "", 0, 0, 3, true),
                new GeneTO("PPAG00000010079", "", "", 0, 0, 3, true),
                new GeneTO("ACAG00000010079", "", "", 0, 0, 3, true),
                new GeneTO("ENSDARG00000025613", "", "", 0, 0, 5, true),
                new GeneTO("ENSDARG00000089109", "", "", 0, 0, 6, true),
                new GeneTO("ENSDARG00000024124", "", "", 0, 0, 7, true),
                new GeneTO("ENSG00000171791", "", "", 0, 0, 7, true),
                new GeneTO("PPYG00000009212", "", "", 0, 0, 9, true),
                new GeneTO("ENSG00000005242", "", "", 0, 0, 9, true),
                new GeneTO("FBgn0003721", "", "", 0, 0, 11, true));

        ArgumentCaptor<Set> geneTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockGeneDAO).updateGenes(geneTOsArg.capture(), 
                eq(Arrays.asList(GeneDAO.Attribute.OMAPARENTNODEID)));
        if (!TOComparator.areGeneTOCollectionsEqual(expectedGeneTOs, geneTOsArg.getValue())) {
            throw new AssertionError("Incorrect GeneTOs generated to update genes, "+
                    "expected " + expectedGeneTOs + ", but was " + geneTOsArg.getValue());
        }
    }
}
