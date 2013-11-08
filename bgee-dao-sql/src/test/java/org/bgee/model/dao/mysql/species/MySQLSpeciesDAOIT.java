package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSpeciesDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLSpeciesDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLSpeciesDAOIT.class.getName());
    
    private final static String INSERTDBNAME = MySQLSpeciesDAOIT.class.getSimpleName();
    
    public MySQLSpeciesDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLSpeciesDAO#insertSpecies(Collection)}.
     */
    @Test
    public void shouldInsertSpecies() throws SQLException {
        this.createAndUseDatabase(INSERTDBNAME);
        
        this.dropDatabase(INSERTDBNAME);
    }
}
