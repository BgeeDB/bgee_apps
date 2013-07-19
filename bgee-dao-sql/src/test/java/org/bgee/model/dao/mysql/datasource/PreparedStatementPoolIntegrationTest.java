package org.bgee.model.dao.mysql.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.BgeeConnection;
import org.bgee.model.dao.mysql.BgeeDataSource;
import org.bgee.model.dao.mysql.BgeePreparedStatement;
import org.junit.Test;


/**
 * This class allows to test the <code>PreparedStatement</code> pool functionality of 
 * {@link org.bgee.mode.data.BgeeConnection BgeeConnection}. 
 *
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @see DataSourceDriverManagerTest
 * @see BgeeConnection
 * @since Bgee 13
 */
public class PreparedStatementPoolIntegrationTest extends DataSourceDriverManagerTest
{
    private final static Logger log = LogManager.getLogger(
            PreparedStatementPoolIntegrationTest.class.getName());

    /**
     * Default Constructor. 
     */
    public PreparedStatementPoolIntegrationTest()
    {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    /**
     * This method tests the <code>PreparedStatement</code> pool by 
     * creating situations where the returned <code>PreparedStatement</code> 
     * has to be a new one and other cases where it has to be fetched in the pool.
     * 
     * @throws SQLException
     */
    @Test
    public void poolTest() throws SQLException {

        // Get a connection
        BgeeConnection con = BgeeDataSource.getBgeeDataSource().getConnection();

        String sql1 = "SELECT * FROM matable WHERE col1 = ?";
        String sql2 = "SELECT * FROM matable WHERE col2 = ?";

        // Test 1
        // Create a prepared Statement with a sql string 1
        BgeePreparedStatement ps1 = con.prepareStatement(sql1);
        // Close it
        ps1.close();
        // Create another prepared Statement with a sql string 1
        BgeePreparedStatement ps2 = con.prepareStatement(sql1); 
        // Check that both BgeePreparedStatement are the same object
        assertEquals("The BgeePreparedStatement pool" +
        		"did not provide the already created BgeePreparedStatement",ps1,ps2);

        // Test 2
        // Create a prepared Statement with a sql string 1
        BgeePreparedStatement ps3 = con.prepareStatement(sql1);
        // Do not close it
        // Create another prepared Statement with a sql string 1
        BgeePreparedStatement ps4 = con.prepareStatement(sql1); 
        // Check that both BgeePreparedStatement are not the same object
        assertNotEquals("The BgeePreparedStatement pool did provide an already " +
                "created BgeePreparedStatement when it has to be not available",ps3,ps4);

        // Test 3
        // Create a prepared Statement with a sql string 1
        BgeePreparedStatement ps5 = con.prepareStatement(sql1);
        // Close it
        ps1.close();
        // Create another prepared Statement with a sql string 2
        BgeePreparedStatement ps6 = con.prepareStatement(sql2); 
        // Check that both BgeePreparedStatement are not the same object
        assertNotEquals("The BgeePreparedStatement pool did provide an already " +
                "created BgeePreparedStatement corresponding to the wrong key",ps5,ps6);

    }
}
