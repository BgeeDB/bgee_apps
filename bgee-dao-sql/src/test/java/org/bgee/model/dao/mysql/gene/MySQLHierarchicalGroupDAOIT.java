package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link MySQLHierarchicalGroupDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author 
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.HierarchicalGroupDAO
 * @since Bgee 13
 */
public class MySQLHierarchicalGroupDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLHierarchicalGroupDAOIT.class.getName());

    public MySQLHierarchicalGroupDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
        
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the select method {@link MySQLHierarchicalGroupDAO#getGroupToGene()}.
     */
    @Test
    public void shouldGetGroupToGene() throws SQLException {

        this.useSelectDB();

        MySQLHierarchicalGroupDAO dao = new MySQLHierarchicalGroupDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(HierarchicalGroupDAO.Attribute.values()));
        int taxonId = -1;
        Set<Integer> speciesIds = new HashSet<>();
        
        // No taxon ID
        try {
            dao.getGroupToGene(taxonId, speciesIds).getAllTOs();
            fail("No IllegalArgumentException was thrown while taxon ID is null"); 
        } catch (IllegalArgumentException e) {
            // Test passed
        }
        
        // Taxon ID without species ID (taxonId = 111)
        taxonId = 111;
        List<HierarchicalGroupToGeneTO> expectedGroupToGene = Arrays.asList(
                new HierarchicalGroupToGeneTO("TO1", 2, 1),
                new HierarchicalGroupToGeneTO("TO1", 3, 1), 
                new HierarchicalGroupToGeneTO("TO5", 1, 1));
        List<HierarchicalGroupToGeneTO> actualGroupToGene = 
                dao.getGroupToGene(taxonId, speciesIds).getAllTOs();
        assertTrue("HierarchicalGroupToGeneTOs incorrectly retrieved: actualGroupToGene=" +
                actualGroupToGene + ", and expectedGroupToGene=" + expectedGroupToGene, 
                TOComparator.areTOCollectionsEqual(actualGroupToGene, expectedGroupToGene));

        // Taxon ID without species ID (taxonId = 211)
        taxonId = 211;
        expectedGroupToGene = Arrays.asList(new HierarchicalGroupToGeneTO("TO2", 2, 1));
        actualGroupToGene = dao.getGroupToGene(taxonId, speciesIds).getAllTOs();
        assertTrue("HierarchicalGroupToGeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualGroupToGene, expectedGroupToGene));
        
        // Taxon ID with one species ID (taxonId = 111 & species ID = 31)
        taxonId = 111;
        speciesIds.add(31);
        expectedGroupToGene = Arrays.asList(new HierarchicalGroupToGeneTO("TO1", 3, 1));
        actualGroupToGene = dao.getGroupToGene(taxonId, speciesIds).getAllTOs();
        assertTrue("HierarchicalGroupToGeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualGroupToGene, expectedGroupToGene));
        
        // Taxon ID with two species IDs (taxonId = 111 & species ID = 31 + 11)
        speciesIds.add(11);
        expectedGroupToGene = Arrays.asList(
                new HierarchicalGroupToGeneTO("TO1", 3, 1), 
                new HierarchicalGroupToGeneTO("TO5", 1, 1));        
        actualGroupToGene = dao.getGroupToGene(taxonId, speciesIds).getAllTOs();
        assertTrue("HierarchicalGroupToGeneTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(actualGroupToGene, expectedGroupToGene));
    }
    
    /**
     * Test the select method {@link MySQLHierarchicalGroupDAO#insertHierarchicalGroups()}.
     */
    @Test
	public void shouldInsertHierarchicalGroups() throws SQLException {
        this.useEmptyDB();
        //create a Collection of HierarchicalGroupTOs to be inserted
        Collection<HierarchicalGroupTO> hgTOs = new ArrayList<HierarchicalGroupTO>();

        hgTOs.add(new HierarchicalGroupTO(1, "HOG:TOTO1", 1, 6, 10));
        hgTOs.add(new HierarchicalGroupTO(2, "HOG:TOTO2", 2, 3, 10));
        hgTOs.add(new HierarchicalGroupTO(3, "HOG:TOTO3", 4, 5, 0));
        try {
            MySQLHierarchicalGroupDAO dao = new MySQLHierarchicalGroupDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertHierarchicalGroups(hgTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from OMAHierarchicalGroup where "+
                            "OMANodeId = ? and OMAGroupId = ? and OMANodeLeftBound = ? " +
                            "and OMANodeRightBound = ? and taxonId = ?")) {
                
                stmt.setInt(1, 1);
                stmt.setString(2, "HOG:TOTO1");
                stmt.setInt(3, 1);
                stmt.setInt(4, 6);
                stmt.setInt(5, 10);
                assertTrue("HierarchicalGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 2);
                stmt.setString(2, "HOG:TOTO2");
                stmt.setInt(3, 2);
                stmt.setInt(4, 3);
                stmt.setInt(5, 10);
                assertTrue("HierarchicalGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            // Test for when the taxon is null 
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from OMAHierarchicalGroup where "+
                            "OMANodeId = ? and OMAGroupId = ? and OMANodeLeftBound = ? " +
                            "and OMANodeRightBound = ? and taxonId is null")) {
                
                stmt.setInt(1, 3);
                stmt.setString(2, "HOG:TOTO3");
                stmt.setInt(3, 4);
                stmt.setInt(4, 5);
                assertTrue("HierarchicalGroupTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertHierarchicalGroups(new HashSet<HierarchicalGroupTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
	}
}