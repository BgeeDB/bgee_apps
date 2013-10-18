package org.bgee.model.dao.mysql.connector.mock;

import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import static org.mockito.Mockito.*;

/**
 * A mock {@code InitialContextFactory} in order to acquire a mock {@code DataSource} 
 * to perform unit testing.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MockInitialContextFactory implements InitialContextFactory {

    public Context getInitialContext(Hashtable<?, ?> arg0)
            throws NamingException {

        Context context = mock(Context.class);
        MockDataSource mockSource = null;
        try {
            mockSource = new MockDataSource();
        } catch (SQLException e) {
            throw new NamingException(e.getMessage());
        }
        when(context.lookup(MockDataSource.DATASOURCENAME)).thenReturn(mockSource);
        return context;
    }
}