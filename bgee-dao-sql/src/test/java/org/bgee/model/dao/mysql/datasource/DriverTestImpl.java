package org.bgee.model.dao.mysql.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.bgee.model.dao.mysql.MockDriverUtils;

/**
 * A minimal implementation of a Driver 
 * used for DataSource test purpose
 *  
 * @author Mathieu Seppey
 * @version Bgee 13, May 2013
 * @since Bgee 13
 */

public class DriverTestImpl implements Driver {
    public static MockDriverUtils driverUtils;
    /**
     * Default constructor.
     */
    public DriverTestImpl() {
    }

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
        return driverUtils.getMockDriver().acceptsURL(url);
    }

    @Override
    //suppress warnings present because we follow the overridden method signature.
    @SuppressWarnings("unused")
    public Connection connect(String url, Properties info) throws SQLException
    {
        return driverUtils.getMockConnection();
    }

    @Override
    //suppress warnings present because we follow the overridden method signature.
    @SuppressWarnings("unused")
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    //suppress warnings present because we follow the overridden method signature.
    @SuppressWarnings("unused")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }
}
