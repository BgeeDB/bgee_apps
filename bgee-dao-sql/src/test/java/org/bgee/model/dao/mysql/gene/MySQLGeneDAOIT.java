package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
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
	
    private final static Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());
    
    /**
     * A {@code List} of {@code String}s that are the names of the tables into which data 
     * are inserted during testing of {@link MySQLGeneDAO} methods inserting data. 
     * They are ordered according to the order tables should be emptied. 
     */
    private final static String INSERTTABLENAME = "gene";
    
    public MySQLGeneDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the insertion method {@link MySQLGeneDAO#insertTerms(Collection)}.
     */
    public void testGetEnsemblGeneIDs() throws SQLException {
        this.useEmptyDB();
        
        String sql = "INSERT INTO gene (geneId, geneName, speciesId, ensemblGene) "
        		   + "VALUES (?, ?, ?, ?) ";

        try (BgeePreparedStatement stmt = 
        		this.getMySQLDAOManager().getConnection().prepareStatement(sql)) {
        	//insert some genes
            stmt.setString(1, "ID1");
            stmt.setString(2, "GeneName1");
            stmt.setInt(3, 11);
            stmt.setBoolean(4, true);
            stmt.executeUpdate();
            stmt.clearParameters();
            stmt.setString(1, "ID2");
            stmt.setString(2, "GeneName2");
            stmt.setInt(3, 12);
            stmt.setBoolean(4, false);
            stmt.executeUpdate();
            stmt.clearParameters();
            stmt.setString(1, "ID3");
            stmt.setString(2, "GeneName3");
            stmt.setInt(3, 13);
            stmt.setBoolean(4, true);
            stmt.executeUpdate();
            stmt.clearParameters();
            stmt.setString(1, "ID4");
            stmt.setString(2, "GeneName4");
            stmt.setInt(3, 14);
            stmt.setBoolean(4, false);
            stmt.executeUpdate();
            stmt.clearParameters();
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        // create manually expected gene IDs list (with ensemblGene at true)
        List<String> expectedIDs = Arrays.asList("ID1", "ID3");
        
        try {
            MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect list returned", expectedIDs, dao.getAllGenes());
            
        } finally {
            this.deleteFromTableAndUseDefaultDB(INSERTTABLENAME);
        }
    }

}
