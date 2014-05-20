package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLGeneDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */

public class MySQLGeneDAOIT extends MySQLITAncestor {
	
    private final static Logger log = LogManager.getLogger(MySQLGeneDAOIT.class.getName());
    
    /**
     * A {@code List} of {@code String}s that are the names of the tables into which data 
     * are inserted during testing of {@link MySQLGeneDAO} methods inserting data. 
     * They are ordered according to the order tables should be emptied. 
     */
    private final static List<String> UPDATEDTABLENAMES = Arrays.asList("gene");

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
//    @Test
    public void testGetAllGenes() throws SQLException {
    	log.entry();
    	this.getMySQLDAOManager().setDatabaseToUse(System.getProperty(POPULATEDDBKEYKEY));
    	try {
			// Generate result with the method
    		MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
    		dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
			GeneTOResultSet methResults = dao.getAllGenes();

    		// Generate manually expected result
    		GeneTO geneTO1 = new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true);
    		GeneTO geneTO2 = new GeneTO("ID2", "genN2", "genDesc2", 21, 0, 0, true);
    		GeneTO geneTO3 = new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false);

    		if (methResults.next()) {
    			if (!areGeneTOsEqual(methResults.getTO(), geneTO1)) {
    				throw log.throwing(new AssertionError(
    						"Incorrect generated TO"));
    			}
    		}
    		if (methResults.next()) {
    			if (!areGeneTOsEqual(methResults.getTO(), geneTO2)) {
    				throw log.throwing(new AssertionError(
    						"Incorrect generated TO"));
    			}
    		}
    		if (methResults.next()) {
    			if (!areGeneTOsEqual(methResults.getTO(), geneTO3)) {
    				throw log.throwing(new AssertionError(
    						"Incorrect generated TO"));
    			}
    		}
    		
    	} finally {
    		this.deleteFromTablesAndUseDefaultDB(UPDATEDTABLENAMES);
    	}
        log.exit();
    }

    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality
     * of each attribute.
     * 
     * @param geneTO1	A {@code GeneTO} to be compared to {@code geneTO2}.
     * @param geneTO2	A {@code GeneTO} to be compared to {@code geneTO1}.
     * @return	{@code true} if {@code geneTO1} and {@code geneTO2} has all attributes equal.
     */
    private boolean areGeneTOsEqual(GeneTO geneTO1, GeneTO geneTO2) {
    	log.entry(geneTO1, geneTO2);
    	if(!geneTO1.getId().equals(geneTO2.getId()) ||
    			!geneTO1.getName().equals(geneTO2.getName()) ||
    			(geneTO1.getDescription() != null &&
    			!geneTO1.getDescription().equals(geneTO2.getDescription())) ||
    			geneTO1.getSpeciesId( ) != geneTO2.getSpeciesId() ||
    			geneTO1.getGeneBioTypeId() != geneTO2.getGeneBioTypeId() ||
    			geneTO1.getOMAParentNodeId() != geneTO2.getOMAParentNodeId() ||
    			geneTO1.isEnsemblGene() != geneTO2.isEnsemblGene()){
    		log.debug("Genes are not equivalent {}", geneTO1.getOMAParentNodeId());
    		return log.exit(false);
    	}
    	return log.exit(true);
    }
    
    /**
     * Test the select method {@link MySQLGeneDAO#updateOMAGroupIDs()}.
     * @throws SQLException 
     */
//    @Test
    public void testUpdateOMAGroupIDs() throws SQLException {
    	log.entry();
    	this.getMySQLDAOManager().setDatabaseToUse(System.getProperty(POPULATEDDBKEYKEY));

    	Collection<GeneTO> geneTOs = new ArrayList<GeneTO>();
    	geneTOs.add(new GeneTO("ID1", null, null, 0, 0, 7, true));
    	geneTOs.add(new GeneTO("ID2", null, null, 0, 0, 6, true));

    	try {
    		MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
    		assertEquals("Incorrect number of rows inserted", 2, 
    				dao.updateOMAGroupIDs(geneTOs));

    		try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
    				prepareStatement("select 1 from gene where " +
    						"geneID = ? and OMAParentNodeId= ?")) {

    			stmt.setString(1, "ID1");
    			stmt.setString(2, "7");
    			assertTrue("GeneTO incorrectly updated", 
    					stmt.getRealPreparedStatement().executeQuery().next());

    			stmt.setString(1, "ID2");
    			stmt.setString(2, "6");
    			assertTrue("GeneTO incorrectly updated", 
    					stmt.getRealPreparedStatement().executeQuery().next());
    		}    
    	} finally {
    		this.deleteFromTablesAndUseDefaultDB(UPDATEDTABLENAMES);
    	}
    	log.exit();
    }
}
