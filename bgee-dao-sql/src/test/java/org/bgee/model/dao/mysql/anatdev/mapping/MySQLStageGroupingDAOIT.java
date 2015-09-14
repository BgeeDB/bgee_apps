package org.bgee.model.dao.mysql.anatdev.mapping;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    //TODO: integration test
    @Test
    public void shouldGetGroupToStage() throws SQLException {
        
        this.useSelectDB();

        
    }
}