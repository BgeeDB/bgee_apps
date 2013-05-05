package org.bgee.model.data.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A minimal implementation of a Driver 
 * used for DataSource test purpose
 *  
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */

public class DriverTestImpl implements Driver
{

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public boolean jdbcCompliant()
    {
        return true;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException
    {
        return true;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException
    {
        return new MockDriverUtils().getMockConnection();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }
}
