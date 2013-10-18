package org.bgee.model.dao.mysql.connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.mysql.TestAncestor;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.mock.MockDriver;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the class {@link BgeePreparedStatement}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeePreparedStatementTest extends TestAncestor
{
    private final static Logger log = 
            LogManager.getLogger(BgeePreparedStatementTest.class.getName());
    
    /**
     * Default constructor.
     */
    public BgeePreparedStatementTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link PreparedStatement#executeQuery()} and 
     * {@link PreparedStatement#isExecuted()}.
     */
    @Test
    public void shouldExecuteQuery() throws SQLException {
        MockDriver.initialize();
        ResultSet rs = mock(ResultSet.class);
        when(MockDriver.getMockStatement().executeQuery()).thenReturn(rs);
        BgeePreparedStatement stmt = new BgeePreparedStatement(null, 
                MockDriver.getMockStatement());
        
        assertSame("Incorrect ResultSet returned", rs, stmt.executeQuery());
        verify(MockDriver.getMockStatement()).executeQuery();
        assertTrue("Incorrect value returned by isExecuted", stmt.isExecuted());
        
        MockDriver.initialize();
    }
    
    /**
     * Test the behavior of {@link PreparedStatement#executeQuery()}  
     * when the query is interrupted.
     */
    @Test
    public void shouldInterruptExecuteQuery() throws SQLException, NoSuchMethodException, 
        SecurityException, IllegalAccessException, IllegalArgumentException, 
        InvocationTargetException {
        
        MockDriver.initialize();
        
        final BgeePreparedStatement stmt = new BgeePreparedStatement(null, 
                MockDriver.getMockStatement());
        //set the cancellation flag
        final Method method = BgeePreparedStatement.class.getDeclaredMethod(
                "setCanceled", boolean.class);
        method.setAccessible(true);
        method.invoke(stmt, true);
        
        //a QueryInterruptedException should be thrown before even executing the query 
        //on the real PreparedStatement
        try {
            stmt.executeQuery();
            //if we reach that point, test failed
            throw new AssertionError("QueryInterruptedException not thrown as expected");
        } catch (QueryInterruptedException e) {
            //test passed.
            //test that it was thrown before executing the query
            verify(MockDriver.getMockStatement(), never()).executeQuery();
        }
        
        method.invoke(stmt, false);
        //now, we set the flag while the query is being executed
        when(MockDriver.getMockStatement().executeQuery()).then(new Answer() {
            @SuppressWarnings("unused")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                method.invoke(stmt, true);
                return null;
            }
        });
        try {
            stmt.executeQuery();
            //if we reach that point, test failed
            throw new AssertionError("QueryInterruptedException not thrown as expected");
        } catch (QueryInterruptedException e) {
            //test passed.
            //test that it was thrown after executing the query
            verify(MockDriver.getMockStatement()).executeQuery();
        }
        
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeePreparedStatement#close()}.
     */
    @Test
    public void shouldClose() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.close();
        
        verify(MockDriver.getMockStatement()).close();
        verify(con).statementClosed(stmt);
    }
    
    /**
     * Test {@link BgeePreparedStatement#cancel()}.
     */
    @Test
    public void shouldCancel() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.cancel();
        
        verify(MockDriver.getMockStatement()).cancel();
        verify(con).statementClosed(stmt);
        assertTrue("Incorret value returned by isCanceled", stmt.isCanceled());
        
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeePreparedStatement#getRealPreparedStatement()}.
     */
    @Test
    public void shouldGetRealPreparedStatement() {
        MockDriver.initialize();
        BgeePreparedStatement stmt = new BgeePreparedStatement(null, 
                MockDriver.getMockStatement());
        assertSame("Incorrect real PreparedStatement returned", 
                MockDriver.getMockStatement(), stmt.getRealPreparedStatement());
    }
    
}
