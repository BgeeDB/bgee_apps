package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO;
import org.junit.Test;

import static org.junit.Assert.*;

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
        } finally {
            this.emptyAndUseDefaultDB();
        }
	}
}