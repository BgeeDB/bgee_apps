package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLStageDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLStageDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLStageDAOIT.class.getName());
        
    public MySQLStageDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLStageDAO#insertStages(Collection)}.
     */
    @Test
    public void shouldInsertTaxa() throws SQLException {
        this.useEmptyDB();
        //create a Collection of TaxonTOs to be inserted
        Collection<StageTO> stageTOs = new ArrayList<StageTO>();
        stageTOs.add(new StageTO("stId1", "name 1", "desc 1", 1, 6, 1, false, true));
        stageTOs.add(new StageTO("stId2", "name 2", "desc 2", 2, 3, 2, true, false));
        stageTOs.add(new StageTO("stId3", "name 3", "desc 3", 4, 5, 2, false, false));
        try {
            MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertStages(stageTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from stage where stageId = ? and " +
                            "stageName = ? and stageDescription = ? and " +
                            "stageLeftBound = ? and stageRightBound = ? and stageLevel = ? " +
                            "and tooGranular = ? and groupingStage = ?")) {
                
                stmt.setString(1, "stId1");
                stmt.setString(2, "name 1");
                stmt.setString(3, "desc 1");
                stmt.setInt(4, 1);
                stmt.setInt(5, 6);
                stmt.setInt(6, 1);
                stmt.setBoolean(7, false);
                stmt.setBoolean(8, true);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "stId2");
                stmt.setString(2, "name 2");
                stmt.setString(3, "desc 2");
                stmt.setInt(4, 2);
                stmt.setInt(5, 3);
                stmt.setInt(6, 2);
                stmt.setBoolean(7, true);
                stmt.setBoolean(8, false);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "stId3");
                stmt.setString(2, "name 3");
                stmt.setString(3, "desc 3");
                stmt.setInt(4, 4);
                stmt.setInt(5, 5);
                stmt.setInt(6, 2);
                stmt.setBoolean(7, false);
                stmt.setBoolean(8, false);
                assertTrue("StageTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
