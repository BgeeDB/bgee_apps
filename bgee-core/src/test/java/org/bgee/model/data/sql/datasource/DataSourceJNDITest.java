package org.bgee.model.data.sql.datasource;

/**
 * Test the loading of {@link org.bgee.mode.data.BgeeDataSource BgeeDataSource} 
 * when using a <code>DataSource</code> loaded using JNDI,
 * to acquire a <code>Connection</code>. 
 * The loading when using a <code>DriverManager</code> is tested in a separated class: 
 * <code>BgeeDataSource</code> parameters are set only once at class loading, 
 * so only once for a given <code>ClassLoader</code>, so it has to be done 
 * in different classes. See {@link DataSourceDriverManagerTest}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @see DataSourceDriverManagerTest
 * @since Bgee 13
 */
public class DataSourceJNDITest 
{

}
