package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for {@link MySQLGeneDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.GeneDAO
 * @since Bgee 13
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLGeneDAO#getAllGenes()}.
     */
    @Test
    public void shouldGetAllGenes() throws SQLException {
        
        this.useSelectDB();

        // Generate result with the method
        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.values()));
        List<GeneTO> methGenes = dao.getAllGenes().getAllTOs();

        // Generate manually expected result
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true), 
                new GeneTO("ID2", "genN2", "genDesc2", 21, 0, 0, true), 
                new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false)); 
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
                new GeneTO("ID1", null, null, null, null, null, null), 
                new GeneTO("ID2", null, null, null, null, null, null), 
                new GeneTO("ID3", null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getGenes()}.
     */
    @Test
    public void shouldGetGenes() throws SQLException {
        
        this.useSelectDB();

        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

        // Without specified species IDs
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        List<GeneTO> methGenes = dao.getGenes(null).getAllTOs();
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO("ID1", null, null, null, null, null, null), 
                new GeneTO("ID2", null, null, null, null, null, null), 
                new GeneTO("ID3", null, null, null, null, null, null));
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));

        // With specified species IDs
        Set<String> speciesIds = new HashSet<String>();
        speciesIds.addAll(Arrays.asList("11", "31", "44"));
        dao.clearAttributes();
        methGenes = dao.getGenes(speciesIds).getAllTOs();

        // Generate manually expected result
        expectedGenes = Arrays.asList(
                new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true), 
                new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false)); 
        //Compare
        assertTrue("GeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(methGenes, expectedGenes));
    }

    /**
     * Test the update method {@link MySQLGeneDAO#updateGenes()}.
     */
    @Test
    public void shouldUpdateGenes() throws SQLException {

        this.useEmptyDB();
        this.populateAndUseDatabase();

        Collection<GeneTO> geneTOs = Arrays.asList(
                new GeneTO("ID1", "GNMod1", "DescMod1", 31, 12, 7, true),
                new GeneTO("ID2", "GNMod2", "DescMod2", 11, 12, 6, false));
        
        Collection<GeneDAO.Attribute> attributesToUpdate1 = Arrays.asList(
                GeneDAO.Attribute.OMAPARENTNODEID);
        Collection<GeneDAO.Attribute> attributesToUpdate2 = Arrays.asList(
                GeneDAO.Attribute.NAME, GeneDAO.Attribute.DESCRIPTION,
                GeneDAO.Attribute.SPECIESID, GeneDAO.Attribute.GENEBIOTYPEID,
                GeneDAO.Attribute.OMAPARENTNODEID, GeneDAO.Attribute.ENSEMBLGENE);
        
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
            
            this.thrown.expect(IllegalArgumentException.class);
            this.thrown.expectMessage("No gene is given, then no gene is updated");
            dao.updateGenes(new HashSet<GeneTO>(), attributesToUpdate2);
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
