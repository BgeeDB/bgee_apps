package org.bgee.model.dao.mysql.connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
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
        when(MockDriver.getMockStatement().executeQuery()).then(new Answer<Object>() {
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
    
    /**
     * Test {@link BgeePreparedStatement#generateParameterizedQueryString(int)}.
     */
    @Test
    public void shouldGenerateParameterizedQueryString() {
        assertEquals("Incorrect parameterized query string generated", "?", 
                BgeePreparedStatement.generateParameterizedQueryString(1));
        assertEquals("Incorrect parameterized query string generated", "?, ?, ?", 
                BgeePreparedStatement.generateParameterizedQueryString(3));
    }
    
    /**
     * Test {@link BgeePreparedStatement#setStrings(int, List)}.
     */
    @Test
    public void shouldSetStrings() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setStrings(4, Arrays.asList("test1"));
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setStrings(4, Arrays.asList("test1", "test2", "test3"));
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        verify(MockDriver.getMockStatement()).setString(5, "test2");
        verify(MockDriver.getMockStatement()).setString(6, "test3");
    }
    
    /**
     * Test {@link BgeePreparedStatement#setIntegers(int, List)}.
     */
    @Test
    public void shouldSetIntegers() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setIntegers(2, Arrays.asList(10));
        
        verify(MockDriver.getMockStatement()).setInt(2, 10);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setIntegers(2, Arrays.asList(10, 14, 17));
        
        verify(MockDriver.getMockStatement()).setInt(2, 10);
        verify(MockDriver.getMockStatement()).setInt(3, 14);
        verify(MockDriver.getMockStatement()).setInt(4, 17);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setBooleans(int, List)}.
     */
    @Test
    public void shouldSetBooleans() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setBooleans(2, Arrays.asList(true));
        
        verify(MockDriver.getMockStatement()).setBoolean(2, true);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBooleans(2, Arrays.asList(true, false, false));

        verify(MockDriver.getMockStatement()).setBoolean(2, true);
        verify(MockDriver.getMockStatement()).setBoolean(3, false);
        verify(MockDriver.getMockStatement()).setBoolean(4, false);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setEnumDAOFields(int, List)}.
     */
    @Test
    public void shouldSetEnumDAOFields() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setEnumDAOFields(2, Arrays.asList(RelationType.ISA_PARTOF));
        
        verify(MockDriver.getMockStatement()).setString(2, 
                RelationType.ISA_PARTOF.getStringRepresentation());
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setEnumDAOFields(2, Arrays.asList(RelationType.ISA_PARTOF, 
                RelationType.DEVELOPSFROM));

        verify(MockDriver.getMockStatement()).setString(2, 
                RelationType.ISA_PARTOF.getStringRepresentation());
        verify(MockDriver.getMockStatement()).setString(3, 
                RelationType.DEVELOPSFROM.getStringRepresentation());
    }
    
}
