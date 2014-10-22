package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.bgee.model.dao.api.anatdev.StageDAO;
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
     * Test the select method {@link MySQLStageDAO#getStages()}.
     */
    @Test
    public void shouldGetStages() throws SQLException {
        log.entry();

        this.useSelectDB();

        MySQLStageDAO dao = new MySQLStageDAO(this.getMySQLDAOManager());
        List<StageTO> allStageTOs = Arrays.asList(
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true), 
                new StageTO("Stage_id2", "stageN2", "stage Desc 2", 2, 7, 2, false, false), 
                new StageTO("Stage_id3", "stageN3", "stage Desc 3", 3, 4, 3, true, false), 
                new StageTO("Stage_id4", "stageN4", "stage Desc 4", 5, 6, 3, false, false), 
                new StageTO("Stage_id5", "stageN5", "stage Desc 5", 8, 17, 2, false, false), 
                new StageTO("Stage_id6", "stageN6", "stage Desc 6", 9, 10, 3, false, false), 
                new StageTO("Stage_id7", "stageN7", "stage Desc 7", 11, 16, 3, false, false), 
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true), 
                new StageTO("Stage_id9", "stageN9", "stage Desc 9", 14, 15, 4, false, false), 
                new StageTO("Stage_id10", "stageN10", "stage Desc 10", 18, 25, 2, false, true), 
                new StageTO("Stage_id11", "stageN11", "stage Desc 11", 19, 20, 3, false, true), 
                new StageTO("Stage_id12", "stageN12", "stage Desc 12", 21, 22, 3, false, true), 
                new StageTO("Stage_id13", "stageN13", "stage Desc 13", 23, 24, 3, false, true), 
                new StageTO("Stage_id14", "stageN14", "stage Desc 14", 26, 35, 2, false, false), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true), 
                new StageTO("Stage_id16", "stageN16", "stage Desc 16", 28, 29, 4, false, false), 
                new StageTO("Stage_id17", "stageN17", "stage Desc 17", 30, 31, 4, false, false), 
                new StageTO("Stage_id18", "stageN18", "stage Desc 18", 33, 34, 3, false, false));
        
        // Test recovery of all attributes without filter on species IDs
        List<StageTO> expectedStages = allStageTOs;
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getStages(null).getAllTOs(), expectedStages));
        
        // Test recovery of one attribute without filter on species IDs
        dao.setAttributes(Arrays.asList(StageDAO.Attribute.ID, StageDAO.Attribute.NAME));
        expectedStages = Arrays.asList(
                new StageTO("Stage_id1", "stageN1", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id2", "stageN2", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id3", "stageN3", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id4", "stageN4", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id5", "stageN5", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id6", "stageN6", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id7", "stageN7", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id8", "stageN8", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id9", "stageN9", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id10", "stageN10", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id11", "stageN11", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id12", "stageN12", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id13", "stageN13", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id14", "stageN14", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id15", "stageN15", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id16", "stageN16", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id17", "stageN17", null, 0, 0, 0, false, false), 
                new StageTO("Stage_id18", "stageN18", null, 0, 0, 0, false, false));
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(dao.getStages(null).getAllTOs(), expectedStages));

        // Test recovery of all attributes with filter on species IDs
        dao.clearAttributes();
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11","44"));
        expectedStages = Arrays.asList(
                new StageTO("Stage_id1", "stageN1", "stage Desc 1", 1, 36, 1, false, true), 
                new StageTO("Stage_id2", "stageN2", "stage Desc 2", 2, 7, 2, false, false), 
                new StageTO("Stage_id5", "stageN5", "stage Desc 5", 8, 17, 2, false, false), 
                new StageTO("Stage_id6", "stageN6", "stage Desc 6", 9, 10, 3, false, false), 
                new StageTO("Stage_id7", "stageN7", "stage Desc 7", 11, 16, 3, false, false), 
                new StageTO("Stage_id8", "stageN8", "stage Desc 8", 12, 13, 4, false, true), 
                new StageTO("Stage_id14", "stageN14", "stage Desc 14", 26, 35, 2, false, false), 
                new StageTO("Stage_id15", "stageN15", "stage Desc 15", 27, 32, 3, false, true), 
                new StageTO("Stage_id16", "stageN16", "stage Desc 16", 28, 29, 4, false, false), 
                new StageTO("Stage_id18", "stageN18", "stage Desc 18", 33, 34, 3, false, false));
        assertTrue("StageTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getStages(speciesIds).getAllTOs(), expectedStages));
        
        dao.clearAttributes();
        speciesIds = new HashSet<String>(Arrays.asList("11", "21", "31", "44"));
        expectedStages = allStageTOs;
        List<StageTO> retrievedStageTOs = dao.getStages(speciesIds).getAllTOs();
        assertTrue("StageTOs incorrectly retrieved, expected " + expectedStages + 
                " - but was: " + retrievedStageTOs,
                TOComparator.areTOCollectionsEqual(retrievedStageTOs, expectedStages));

        log.exit();
    }
    
    /**
     * Test the insertion method {@link MySQLStageDAO#insertStages(Collection)}.
     */
    @Test
    public void shouldInsertStages() throws SQLException {
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
