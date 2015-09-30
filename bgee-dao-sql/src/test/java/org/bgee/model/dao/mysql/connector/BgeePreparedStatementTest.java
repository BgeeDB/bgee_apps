package org.bgee.model.dao.mysql.connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO.ConfidenceLevel;
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
 * @author Valentine Rech de Laval
 * @version Bgee 13 Sept. 2015
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
     * Test {@link BgeePreparedStatement#setString(int, String)}.
     */
    @Test
    public void shouldSetString() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setString(4, "test1");
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setString(5, null);
        
        verify(MockDriver.getMockStatement()).setNull(5, Types.VARCHAR);
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
        stmt.setStrings(4, Arrays.asList("test1", "test2", "test3", null));
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        verify(MockDriver.getMockStatement()).setString(5, "test2");
        verify(MockDriver.getMockStatement()).setString(6, "test3");
        verify(MockDriver.getMockStatement()).setNull(7, Types.VARCHAR);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setInteger(int, Integer)}.
     */
    @Test
    public void shouldSetInteger() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setInt(3, 10);
        
        verify(MockDriver.getMockStatement()).setInt(3, 10);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setInt(2, null);
        
        verify(MockDriver.getMockStatement()).setNull(2, Types.INTEGER);
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
        stmt.setIntegers(2, Arrays.asList(null, 10, 14, 17));
        
        verify(MockDriver.getMockStatement()).setNull(2, Types.INTEGER);
        verify(MockDriver.getMockStatement()).setInt(3, 10);
        verify(MockDriver.getMockStatement()).setInt(4, 14);
        verify(MockDriver.getMockStatement()).setInt(5, 17);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setLong(int, Long)}.
     */
    @Test
    public void shouldSetLong() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setLong(3, 10L);
        
        verify(MockDriver.getMockStatement()).setLong(3, 10L);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setLong(2, null);
        
        verify(MockDriver.getMockStatement()).setNull(2, Types.BIGINT);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setLongs(int, List)}.
     */
    @Test
    public void shouldSetLongs() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setLongs(2, Arrays.asList(10L));
        
        verify(MockDriver.getMockStatement()).setLong(2, 10L);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setLongs(2, Arrays.asList(null, 10L, 14L, 17L));
        
        verify(MockDriver.getMockStatement()).setNull(2, Types.BIGINT);
        verify(MockDriver.getMockStatement()).setLong(3, 10L);
        verify(MockDriver.getMockStatement()).setLong(4, 14L);
        verify(MockDriver.getMockStatement()).setLong(5, 17L);
    }

    /**
     * Test {@link BgeePreparedStatement#setBoolean(int, Boolean)}.
     */
    @Test
    public void shouldSetBoolean() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBoolean(2, true);
        
        verify(MockDriver.getMockStatement()).setBoolean(2, true);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBoolean(2, null);

        verify(MockDriver.getMockStatement()).setNull(2, Types.BOOLEAN);
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
        stmt.setBooleans(2, Arrays.asList(true, false, false, null));

        verify(MockDriver.getMockStatement()).setBoolean(2, true);
        verify(MockDriver.getMockStatement()).setBoolean(3, false);
        verify(MockDriver.getMockStatement()).setBoolean(4, false);
        verify(MockDriver.getMockStatement()).setNull(5, Types.BOOLEAN);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setEnumDAOField(int, EnumDAOField)}.
     */
    @Test
    public void shouldSetEnumDAOField() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setEnumDAOField(1, ConfidenceLevel.HIGH_CONFIDENCE);
        
        verify(MockDriver.getMockStatement()).setString(1, 
                ConfidenceLevel.HIGH_CONFIDENCE.getStringRepresentation());
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setEnumDAOField(9, null);

        verify(MockDriver.getMockStatement()).setNull(9, Types.VARCHAR);
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
        stmt.setEnumDAOFields(2, Arrays.asList(RelationType.ISA_PARTOF, null, 
                RelationType.DEVELOPSFROM));

        verify(MockDriver.getMockStatement()).setString(2, 
                RelationType.ISA_PARTOF.getStringRepresentation());
        verify(MockDriver.getMockStatement()).setNull(3, Types.VARCHAR);
        verify(MockDriver.getMockStatement()).setString(4, 
                RelationType.DEVELOPSFROM.getStringRepresentation());

    }
    
    /**
     * Test {@link BgeePreparedStatement#setFloat(int, Float)}.
     */
    @Test
    public void shouldSetFloat() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setFloat(2, 1.08f);
        
        verify(MockDriver.getMockStatement()).setFloat(2, 1.08f);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setFloat(2, null);

        verify(MockDriver.getMockStatement()).setNull(2, Types.REAL);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setFloats(int, List)}.
     */
    @Test
    public void shouldSetFloats() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setFloats(2, Arrays.asList(1.08f));
        
        verify(MockDriver.getMockStatement()).setFloat(2, 1.08f);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setFloats(2, Arrays.asList(3.6f, 18.01f, 3.14f, null));

        verify(MockDriver.getMockStatement()).setFloat(2, 3.6f);
        verify(MockDriver.getMockStatement()).setFloat(3, 18.01f);
        verify(MockDriver.getMockStatement()).setFloat(4, 3.14f);
        verify(MockDriver.getMockStatement()).setNull(5, Types.REAL);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setNull(int, Types)}.
     */
    @Test
    public void shouldSetNull() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setNull(2, Types.BOOLEAN);
        
        verify(MockDriver.getMockStatement()).setNull(2, Types.BOOLEAN);
    }

    /**
     * Test {@link BgeePreparedStatement#setDate(int, Date)}.
     */
    @Test
    public void shouldSetDate() throws SQLException {
        Date date = new java.sql.Date(12345);
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setDate(2, date);
        
        verify(MockDriver.getMockStatement()).setDate(2, date);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setDate(2, null);

        verify(MockDriver.getMockStatement()).setNull(2, Types.DATE);
    }
}
