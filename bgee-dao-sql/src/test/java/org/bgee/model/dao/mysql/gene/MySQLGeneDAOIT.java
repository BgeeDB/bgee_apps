package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
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
	
    private final static Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());
    
    /**
     * A {@code List} of {@code String}s that are the names of the tables into which data 
     * are inserted during testing of {@link MySQLGeneDAO} methods inserting data. 
     * They are ordered according to the order tables should be emptied. 
     */
    
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
    		MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());

    		try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
    				prepareStatement("select geneId from gene order by geneId;")) {
        		// Generate manually expected result
    			MySQLGeneTOResultSet myResults = dao.new MySQLGeneTOResultSet(stmt);

    			// Generate result with the method
        		dao.setAttributesToGet(Arrays.asList(GeneDAO.Attribute.ID));
    			MySQLGeneTOResultSet methResults = dao.getAllGenes();

    			if (!areGeneTOResultSetsEqual(myResults, methResults)) {
    				throw log.throwing(new AssertionError(
    						"Incorrect generated selection from gene table"));
    			}
    		} 
    	} finally {
    		//TODO
    	}
        log.exit();
    }

    /**
     * Method to compare two {@code MySQLGeneTOResultSet}s, to check for complete equality
     * of each {@code GeneTO}s.
     * 
     * @param rs1	  A {@code MySQLGeneTOResultSet} to be compared to {@code rs2}.
     * @param rs2 A {@code MySQLGeneTOResultSet} to be compared to {@code rs1}.
	 * @return		{@code true} if {@code rs1} and {@code rs2} has same {@code GeneTO}s.
     */
    private boolean areGeneTOResultSetsEqual(
    		MySQLGeneTOResultSet rs1, MySQLGeneTOResultSet rs2) {
    	log.entry(rs1, rs2);
    	List<GeneTO> myList = new ArrayList<GeneTO>();
		while(rs1.next()){
			myList.add(rs1.getTO());
		}
    	List<GeneTO> methList = new ArrayList<GeneTO>();
		while(rs2.next()){
			methList.add(rs1.getTO());
		}
		for (GeneTO myGene: myList) {
        	boolean found = false;
			for (GeneTO methGene: methList) {
        		if (areGeneTOsEqual(myGene,methGene)) {
        			found = true;
        		}
			}
        	if (!found) {
        		log.debug("No equivalent gene found for {}", myGene.getId());
        		return log.exit(false);
        	}      
		}
		return log.exit(true);
	}

    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality
     * of each attribute.
     * 
     * @param geneTO1	A {@code GeneTO} to be compared to {@code geneTO2}.
     * @param geneTO2	A {@code GeneTO} to be compared to {@code geneTO1}.
     * @return		{@code true} if {@code geneTO1} and {@code geneTO2} has all attributes
     * 				equal as well as for child {@code GeneTO}s.
     */
    private boolean areGeneTOsEqual(GeneTO geneTO1, GeneTO geneTO2) {
    	log.entry(geneTO1, geneTO2);
    	if(!geneTO1.getId().equals(geneTO2.getId()) ||
    			!geneTO1.getName().equals(geneTO2.getName()) ||
    			(geneTO1.getDescription() != null &&
    			!geneTO1.getDescription().equals(geneTO2.getDescription())) ||
    			geneTO1.getSpeciesId( ) != geneTO2.getSpeciesId() ||
    			geneTO1.getGeneBioTypeId() != geneTO2.getGeneBioTypeId() ||
    			geneTO1.getOMANodeId() != geneTO2.getOMANodeId() ||
    			geneTO1.isEnsemblGene() != geneTO2.isEnsemblGene()){
    		log.debug("Nodes are not equivalent {}", geneTO1.getOMANodeId());
    		return log.exit(false);
    	}
    	return log.exit(true);
    }
}
