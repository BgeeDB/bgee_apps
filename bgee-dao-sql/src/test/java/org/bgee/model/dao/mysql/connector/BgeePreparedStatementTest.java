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
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
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
        //Regression test for when size is 0. The method used to throw a NegativeArraySizeException
        //in that case. It should now return an empty String.
        assertEquals("Incorrect parameterized query string generated", "", 
                BgeePreparedStatement.generateParameterizedQueryString(0));
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
     * Test {@link BgeePreparedStatement#setStrings(int, List, boolean)}.
     */
    @Test
    public void shouldSetStrings() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setStrings(4, Arrays.asList("test1"), false);
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setStrings(4, Arrays.asList("test1", "test3", "test2", null), true);
        
        verify(MockDriver.getMockStatement()).setString(4, "test1");
        verify(MockDriver.getMockStatement()).setString(5, "test2");
        verify(MockDriver.getMockStatement()).setString(6, "test3");
        verify(MockDriver.getMockStatement()).setNull(7, Types.VARCHAR);
    }

    /**
     * Test {@link BgeePreparedStatement#setStringsToIntegers(int, List, boolean)}.
     */
    @Test
    public void shouldSetStringsToIntegers() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setStringsToIntegers(4, Arrays.asList("2"), false);
        
        verify(MockDriver.getMockStatement()).setInt(4, 2);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setStringsToIntegers(4, Arrays.asList("11", "2", null, "3"), true);
        
        verify(MockDriver.getMockStatement()).setInt(4, 2);
        verify(MockDriver.getMockStatement()).setInt(5, 3);
        verify(MockDriver.getMockStatement()).setInt(6, 11);
        verify(MockDriver.getMockStatement()).setNull(7, Types.INTEGER);
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
     * Test {@link BgeePreparedStatement#setIntegers(int, List, boolean)}.
     */
    @Test
    public void shouldSetIntegers() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setIntegers(2, Arrays.asList(10), false);
        
        verify(MockDriver.getMockStatement()).setInt(2, 10);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setIntegers(2, Arrays.asList(null, 10, 17, 14), true);
        
        verify(MockDriver.getMockStatement()).setInt(2, 10);
        verify(MockDriver.getMockStatement()).setInt(3, 14);
        verify(MockDriver.getMockStatement()).setInt(4, 17);
        verify(MockDriver.getMockStatement()).setNull(5, Types.INTEGER);
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
        stmt.setLongs(2, Arrays.asList(10L), true);
        
        verify(MockDriver.getMockStatement()).setLong(2, 10L);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setLongs(2, Arrays.asList(null, 10L, 14L, 17L), true);
        
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
     * Test {@link BgeePreparedStatement#setBooleans(int, List, boolean)}.
     */
    @Test
    public void shouldSetBooleans() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setBooleans(2, Arrays.asList(true), false);
        
        verify(MockDriver.getMockStatement()).setBoolean(2, true);
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBooleans(2, Arrays.asList(true, false, false, null), false);

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
     * Test {@link BgeePreparedStatement#setEnumDAOFields(int, List, boolean)}.
     */
    @Test
    public void shouldSetEnumDAOFields() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setEnumDAOFields(2, Arrays.asList(RelationType.ISA_PARTOF), false);
        
        verify(MockDriver.getMockStatement()).setString(2, 
                RelationType.ISA_PARTOF.getStringRepresentation());
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setEnumDAOFields(2, Arrays.asList(RelationType.ISA_PARTOF, null, 
                RelationType.DEVELOPSFROM), false);

        verify(MockDriver.getMockStatement()).setString(2, 
                RelationType.ISA_PARTOF.getStringRepresentation());
        verify(MockDriver.getMockStatement()).setNull(3, Types.VARCHAR);
        verify(MockDriver.getMockStatement()).setString(4, 
                RelationType.DEVELOPSFROM.getStringRepresentation());

    }
    
    /**
     * Test {@link BgeePreparedStatement#setBigDecimal(int, String)}.
     */
    @Test
    public void shouldSetBigDecimalString() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBigDecimal(2, "1.088736433");
        
        verify(MockDriver.getMockStatement()).setBigDecimal(2, new BigDecimal("1.088736433"));
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBigDecimal(2, (String) null);

        verify(MockDriver.getMockStatement()).setNull(2, Types.DECIMAL);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setBigDecimal(int, BigDecimal)}.
     */
    @Test
    public void shouldSetBigDecimal() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBigDecimal(2, new BigDecimal("1.088736433"));
        
        verify(MockDriver.getMockStatement()).setBigDecimal(2, new BigDecimal("1.088736433"));
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBigDecimal(2, (BigDecimal) null);

        verify(MockDriver.getMockStatement()).setNull(2, Types.DECIMAL);
    }
    
    /**
     * Test {@link BgeePreparedStatement#setBigDecimals(int, Collection, boolean)}.
     */
    @Test
    public void shouldSetBigDecimals() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = mock(BgeeConnection.class);
        BgeePreparedStatement stmt = new BgeePreparedStatement(con, 
                MockDriver.getMockStatement());
        stmt.setBigDecimals(2, Arrays.asList("1.0888996"), false);
        
        verify(MockDriver.getMockStatement()).setBigDecimal(2, new BigDecimal("1.0888996"));
        
        MockDriver.initialize();
        con = mock(BgeeConnection.class);
        stmt = new BgeePreparedStatement(con, MockDriver.getMockStatement());
        stmt.setBigDecimals(2, Arrays.asList("3.6", "18.01", "3.14", null), true);

        verify(MockDriver.getMockStatement()).setBigDecimal(2, new BigDecimal("3.14"));
        verify(MockDriver.getMockStatement()).setBigDecimal(3, new BigDecimal("3.6"));
        verify(MockDriver.getMockStatement()).setBigDecimal(4, new BigDecimal("18.01"));
        verify(MockDriver.getMockStatement()).setNull(5, Types.DECIMAL);
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
