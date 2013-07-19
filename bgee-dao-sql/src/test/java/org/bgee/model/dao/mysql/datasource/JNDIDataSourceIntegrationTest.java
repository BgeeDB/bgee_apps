package org.bgee.model.data.sql.datasource;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.data.sql.BgeeDataSource;
import org.junit.Test;


/**
 * Test the loading of {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource} 
 * when using a <code>DataSource</code> loaded using JNDI
 * to acquire a <code>Connection</code>. 
 * The loading when using a <code>DriverManager</code> is tested in a separated class: 
 * <code>BgeeDataSource</code> parameters are set only once at class loading, 
 * so only once for a given <code>ClassLoader</code>, so it has to be done 
 * in different classes. See {@link DataSourceDriverManagerTest}.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @see DataSourceDriverManagerTest
 * @since Bgee 13
 */
public class JNDIDataSourceIntegrationTest extends DataSourceDriverManagerTest
{
    private final static Logger log = LogManager.getLogger(
            JNDIDataSourceIntegrationTest.class.getName());

    /**
     * Default Constructor. 
     */
    public JNDIDataSourceIntegrationTest()
    {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    @Test
    /**
     * Check if a <code>DataSource</code> can be obtained
     */
    public void newDataSource() throws NoSuchFieldException, SecurityException,
                    IllegalArgumentException, IllegalAccessException, SQLException{

        Field field = BgeeDataSource.class.getDeclaredField("realDataSource");

        field.setAccessible(true);

        assertNotNull("No real DataSource has been obtained by the BgeeDataSource as expected",
                field.get(BgeeDataSource.getBgeeDataSource()));
    }
    @Test
    /**
     * Check if a <code>Connection</code> can be obtained from the DataSource
     */
    public void getConnection() throws SQLException {

        assertNotNull("No BgeeConnection has been returned by the BgeeDataSource as expected",
                BgeeDataSource.getBgeeDataSource().getConnection());
        
    }   
}
