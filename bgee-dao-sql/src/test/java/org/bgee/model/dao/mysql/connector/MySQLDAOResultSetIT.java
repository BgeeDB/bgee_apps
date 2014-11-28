package org.bgee.model.dao.mysql.connector;

import static org.junit.Assert.assertSame;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLDAOResultSet}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLDAOResultSetIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLDAOResultSetIT.class.getName());
        
    public MySQLDAOResultSetIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link MySQLDAOResultSet#getTO()} on a real implementation.
     */
    @Test
    public void shouldGetTO() throws SQLException {
        this.useSelectDB();
        
        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
        GeneTOResultSet rs = dao.getAllGenes();
        while (rs.next()) {
            //consecutive calls to getTO should return the same TO instance, 
            //not a newly instantiated one
            GeneTO to = rs.getTO();
            assertSame("Incorrect TO returned following consecutive calls to getTO", 
                    to, rs.getTO());
            assertSame("Incorrect TO returned following consecutive calls to getTO", 
                    to, rs.getTO());
        }

        //no more results, calling getTO should throw an Exception
        try {
            rs.getTO();
            //test failed, an exception should have been thrown
            throw log.throwing(new AssertionError("Calling getTO when no more results " +
                    "are available should throw an exception"));
        } catch (Exception e) {
            //test passed.
        }
    }
}
