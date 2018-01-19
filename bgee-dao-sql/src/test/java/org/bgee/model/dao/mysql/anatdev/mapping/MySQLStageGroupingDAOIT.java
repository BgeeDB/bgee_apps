package org.bgee.model.dao.mysql.anatdev.mapping;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLStageGroupingDAO}, performed on a real MySQL 
 * database. See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Sept. 2015
 * @since   Bgee 13
 */
public class MySQLStageGroupingDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLStageGroupingDAOIT.class.getName());

    public MySQLStageGroupingDAOIT() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLStageGroupingDAO#getGroupToStage()}.
     */
    @Test
    public void shouldGetGroupToStage() throws SQLException {
        
        this.useSelectDB();

        MySQLStageGroupingDAO dao = new MySQLStageGroupingDAO(this.getMySQLDAOManager());
        List<GroupToStageTO> expectedTOs = Arrays.asList(
                new GroupToStageTO("Stage_id1", "Stage_id1"),
                new GroupToStageTO("Stage_id10", "Stage_id10"),
                new GroupToStageTO("Stage_id11", "Stage_id11"),
                new GroupToStageTO("Stage_id12", "Stage_id12"),
                new GroupToStageTO("Stage_id13", "Stage_id13"),
                new GroupToStageTO("Stage_id15", "Stage_id15"),
                new GroupToStageTO("Stage_id8", "Stage_id8"));
        assertTrue("AnatEntityTOs incorrectly retrieved",
                TOComparator.areTOCollectionsEqual(
                        dao.getGroupToStage(null, null).getAllTOs(), expectedTOs));
        //as of Bgee 13, there is no mapping between stages, so we basically 
        //simply retrieve grouping stages existing in all the provided species, 
        //we don't use the ancestralTaxonId.

        expectedTOs = Arrays.asList(
                new GroupToStageTO("Stage_id1", "Stage_id1"),
                new GroupToStageTO("Stage_id15", "Stage_id15"),
                new GroupToStageTO("Stage_id8", "Stage_id8"));
        assertTrue("AnatEntityTOs incorrectly retrieved", 
                TOComparator.areTOCollectionsEqual(
                        dao.getGroupToStage(null, new HashSet<>(Arrays.asList(11))).getAllTOs(),
                        expectedTOs));
    }
}