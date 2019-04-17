package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLGeneDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @see org.bgee.model.dao.api.gene.GeneDAO
 * @since   Bgee 13, May 2014
 */

public class MySQLGeneDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLGeneDAOIT.class.getName());

    public MySQLGeneDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getAllGenes()}.
     */
    // TODO add test on retrieving ANCESTRAL_OMA_NODE_ID and ANCESTRAL_OMA_TAXON_ID
    // when it will be implemented
    @Test
    public void shouldGetAllGenes() throws SQLException {
        
        this.useSelectDB();

        // Generate result with the method
        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.values()));
        List<GeneTO> methGenes = dao.getAllGenes().getAllTOs();

        // Generate manually expected result
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", "genN1", "genDesc1", 11, 12, 5, true, 1), 
                new GeneTO(2, "ID2", "genN2", "genDesc2", 21, 0, 2, true, 1), 
                new GeneTO(3, "ID3", "genN3", "genDesc3", 31, 0, 3, false, 1), 
                new GeneTO(4, "ID4", "genN4", "genDesc4", 21, 0, 0, true, 1)); 
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // without declared attribute should return same TOs that with all attributes 
        dao.clearAttributes();
        methGenes = dao.getAllGenes().getAllTOs();
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // Generate manually expected result
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        methGenes = dao.getAllGenes().getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", null, null, null, null, null, null, null), 
                new GeneTO(2, "ID2", null, null, null, null, null, null, null), 
                new GeneTO(3, "ID3", null, null, null, null, null, null, null),
                new GeneTO(4, "ID4", null, null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getGenesBySpeciesIds(Collection)}.
     */
    @Test
    public void shouldGetGenesBySpeciesIds() throws SQLException {
        
        this.useSelectDB();

        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

        // Without specified species IDs and gene IDs
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        List<GeneTO> methGenes = dao.getGenesBySpeciesIds(null).getAllTOs();
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", null, null, null, null, null, null, null), 
                new GeneTO(2, "ID2", null, null, null, null, null, null, null), 
                new GeneTO(3, "ID3", null, null, null, null, null, null, null),
                new GeneTO(4, "ID4", null, null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // With specified species IDs without gene IDs
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.addAll(Arrays.asList(11, 31, 44));
        dao.clearAttributes();
        methGenes = dao.getGenesBySpeciesIds(speciesIds).getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", "genN1", "genDesc1", 11, 12, 5, true, 1), 
                new GeneTO(3, "ID3", "genN3", "genDesc3", 31, 0, 3, false, 1)); 
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getGenesWithDataBySpeciesIds(Collection)}.
     */
    @Test
    //TODO: implement
    public void shouldGetGenesWithDataBySpeciesIds() throws SQLException {
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getGenesByIds(Collection)}.
     */
    @Test
    public void shouldGetGenesByIds() throws SQLException {
        
        this.useSelectDB();

        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

        // Without specified species IDs and gene IDs
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        List<GeneTO> methGenes = dao.getGenesByIds(null).getAllTOs();
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", null, null, null, null, null, null, null), 
                new GeneTO(2, "ID2", null, null, null, null, null, null, null), 
                new GeneTO(3, "ID3", null, null, null, null, null, null, null),
                new GeneTO(4, "ID4", null, null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // With specified gene IDs without species IDs
        // TODO: should test with several genes mapped to a same Ensembl gene ID
        dao.clearAttributes();
        dao.setAttributes(GeneDAO.Attribute.NAME);
        Set<String> geneIds = new HashSet<String>();
        geneIds.addAll(Arrays.asList("ID1", "ID2", "ID4"));
        methGenes = dao.getGenesByIds(geneIds).getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(null, null, "genN1", null, null, null, null, null, null), 
                new GeneTO(null, null, "genN2", null, null, null, null, null, null), 
                new GeneTO(null, null, "genN4", null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getGenesBySpeciesAndGeneIds(Map)}.
     */
    @Test
    public void shouldGetGenesBySpeciesAndGeneIds() throws SQLException {
        
        this.useSelectDB();

        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

        // Without specified species IDs and gene IDs
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        List<GeneTO> methGenes = dao.getGenesBySpeciesAndGeneIds(null).getAllTOs();
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", null, null, null, null, null, null, null), 
                new GeneTO(2, "ID2", null, null, null, null, null, null, null), 
                new GeneTO(3, "ID3", null, null, null, null, null, null, null),
                new GeneTO(4, "ID4", null, null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // With specified species IDs without gene IDs
        Set<Integer> speciesIds = new HashSet<>();
        speciesIds.addAll(Arrays.asList(11, 31, 44));
        dao.clearAttributes();
        methGenes = dao.getGenesBySpeciesAndGeneIds(speciesIds.stream().collect(
                Collectors.toMap(id -> id, id -> null))).getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", "genN1", "genDesc1", 11, 12, 5, true, 1), 
                new GeneTO(3, "ID3", "genN3", "genDesc3", 31, 0, 3, false, 1)); 
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
        
        // With specified species IDs AND gene IDs
        // TODO: should test with several genes mapped to a same Ensembl gene ID
        dao.setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.DESCRIPTION);
        Set<String> geneIds = new HashSet<String>();
        geneIds.addAll(Arrays.asList("ID1"));
        Map<Integer, Set<String>> speMap = new HashMap<>();
        speMap.put(11, geneIds);
        methGenes = dao.getGenesBySpeciesAndGeneIds(speMap).getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(1, "ID1", null, "genDesc1", null, null, null, null, null)); 
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }
    
    /**
     * Test the select method {@link MySQLGeneDAO#getGenesByBgeeIds(Collection)}.
     */
    @Test
    public void shouldGetGenesByBgeeIds() throws SQLException {

        this.useSelectDB();

        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

        // Without specified Bgee geneID
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID, GeneDAO.Attribute.SPECIES_ID));
        List<GeneTO> methGenes = dao.getGenesByBgeeIds(null).getAllTOs();
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO(1, null, null, null, 11, null, null, null, null),
                new GeneTO(2, null, null, null, 21, null, null, null, null),
                new GeneTO(3, null, null, null, 31, null, null, null, null),
                new GeneTO(4, null, null, null, 21, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // With specified Bgee gene IDs
        dao.clearAttributes();
        dao.setAttributes(GeneDAO.Attribute.DESCRIPTION);
        Set<Integer> bgeeGeneIds = new HashSet<>(Arrays.asList(1, 2, 3));
        methGenes = dao.getGenesByBgeeIds(bgeeGeneIds).getAllTOs();
        expectedGenes = Arrays.asList(
                new GeneTO(null, null, null, "genDesc1", null, null, null, null, null),
                new GeneTO(null, null, null, "genDesc2", null, null, null, null, null),
                new GeneTO(null, null, null, "genDesc4", null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the update method {@link MySQLGeneDAO#updateGenes(Collection, Collection)}.
     */
    @Test
    public void shouldUpdateGenes() throws SQLException {

        this.useEmptyDB();
        this.populateAndUseDatabase();

        Collection<GeneTO> geneTOs = Arrays.asList(
                new GeneTO(1, "ID1", "GNMod1", "DescMod1", 31, 12, 7, true, 1),
                new GeneTO(2, "ID2", "GNMod2", "DescMod2", 11, 12, 6, false, 1));
        
        Collection<GeneDAO.Attribute> attributesToUpdate1 = Arrays.asList(
                GeneDAO.Attribute.OMA_PARENT_NODE_ID);
        Collection<GeneDAO.Attribute> attributesToUpdate2 = Arrays.asList(
                GeneDAO.Attribute.NAME, GeneDAO.Attribute.DESCRIPTION,
                GeneDAO.Attribute.SPECIES_ID, GeneDAO.Attribute.GENE_BIO_TYPE_ID,
                GeneDAO.Attribute.OMA_PARENT_NODE_ID, GeneDAO.Attribute.ENSEMBL_GENE);
        
        try {
            //Test with only one Attribute
            MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateGenes(geneTOs, attributesToUpdate1));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from gene where geneId = ? " +
                                     "and OMAParentNodeId = ?")) {
                stmt.setString(1, "ID1");
                stmt.setInt(2, 7);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "ID2");
                stmt.setInt(2, 6);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            //Test with all Attributes
            assertEquals("Incorrect number of rows updated", 2, 
                    dao.updateGenes(geneTOs, attributesToUpdate2));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from gene where " +  
                            "geneId = ? and geneName = ? and geneDescription = ? and " +
                            "speciesId = ? and geneBioTypeId = ? and OMAParentNodeId= ? and " +
                            "ensemblGene = ?")) {
                stmt.setString(1, "ID1");
                stmt.setString(2, "GNMod1");
                stmt.setString(3, "DescMod1");
                stmt.setInt(4, 31);
                stmt.setInt(5, 12);
                stmt.setInt(6, 7);
                stmt.setBoolean(7, true);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "ID2");
                stmt.setString(2, "GNMod2");
                stmt.setString(3, "DescMod2");
                stmt.setInt(4, 11);
                stmt.setInt(5, 12);
                stmt.setInt(6, 6);
                stmt.setBoolean(7, false);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            try {
                dao.updateGenes(new HashSet<GeneTO>(), attributesToUpdate2);
                //test failed
                throw new AssertionError(
                        "updateGenes did not throw an IllegalArgumentException as expected");
            } catch (IllegalArgumentException e) {
                // test passed, do nothing
            }
            
//            try {
//                dao.updateGenes(new HashSet<GeneTO>(), 
//                        Arrays.asList(GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID));
//                //test failed
//                throw new AssertionError(
//                        "updateGenes did not throw an IllegalArgumentException as expected");
//            } catch (IllegalArgumentException e) {
//                // test passed, do nothing
//            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
