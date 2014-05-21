package org.bgee.model.dao.mysql.connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.mysql.TestAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.junit.Test;

import com.mysql.jdbc.ResultSetMetaData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MySQLDAOResultSetTest extends TestAncestor
{
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOResultSetTest.class.getName());
    
    /**
     * Default constructor.
     */
    public MySQLDAOResultSetTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Extends {@code MySQLDAOResultSet}, which is abstract, to perform 
     * unit tests using it.
     */
    private class FakeDAOResultSet extends MySQLDAOResultSet<TransferObject> {
        public FakeDAOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        public FakeDAOResultSet(List<BgeePreparedStatement> statements) {
            super(statements);
        }
        @Override
        public TransferObject getTO() throws DAOException {
            return null;
        }
        
    }
    
    /**
     * Test that the first {@code BgeePreparedStatement} provided at instantiation 
     * of a {@code MySQLDAOResultSet} is immediately executed.
     */
    @Test
    public void shouldInstantiate() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
        
        ResultSet realRs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(realRs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(0);
        when(mockStatement.executeQuery()).thenReturn(realRs);
        when(mockStatement2.executeQuery()).thenReturn(realRs);
        when(mockStatement3.executeQuery()).thenReturn(realRs);
        
        new FakeDAOResultSet(Arrays.asList(mockStatement, mockStatement2, mockStatement3));
        verify(mockStatement).executeQuery();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        
        when(mockStatement.isExecuted()).thenReturn(true);
        try {
            new FakeDAOResultSet(mockStatement);
            //if we reach that point, test failed
            throw new AssertionError("A BgeePreparedStatement already executed should not " +
                    "be accepted");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
    
    /**
     * Test {@link MySQLDAOResultSet#executeNextStatementQuery()}.
     */
    @Test
    public void shouldExecuteNextStatementQuery() throws SQLException, 
        NoSuchMethodException, SecurityException, IllegalAccessException, 
        IllegalArgumentException, InvocationTargetException {
        
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSet realRs = mock(ResultSet.class);
        when(mockStatement.executeQuery()).thenReturn(realRs);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(realRs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(eq(1))).thenReturn("column1");
        
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        ResultSet realRs2 = mock(ResultSet.class);
        when(mockStatement2.executeQuery()).thenReturn(realRs2);
        ResultSetMetaData metaData2 = mock(ResultSetMetaData.class);
        when(realRs2.getMetaData()).thenReturn(metaData2);
        when(metaData2.getColumnCount()).thenReturn(2);
        when(metaData2.getColumnLabel(eq(1))).thenReturn("column2-1");
        when(metaData2.getColumnLabel(eq(2))).thenReturn("column2-2");
        
        MySQLDAOResultSet<TransferObject> rs = new FakeDAOResultSet(
                Arrays.asList(mockStatement, mockStatement2));
        //first mockStatement should have been executed right away.
        //check column labels
        Map<Integer, String> expectedColumnLabels = new HashMap<Integer, String>();
        expectedColumnLabels.put(1, "column1");
        assertEquals("Incorrect column labels", expectedColumnLabels, rs.getColumnLabels());
        
        //the call to executeNextStatementQuery should lose the first one and 
        //execute the second one
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement, never()).close();
        
        Method method = MySQLDAOResultSet.class.getDeclaredMethod(
                "executeNextStatementQuery");
        method.setAccessible(true);
        method.invoke(rs);
        
        verify(mockStatement).close();
        verify(mockStatement2).executeQuery();
        verify(mockStatement2, never()).close();
        expectedColumnLabels = new HashMap<Integer, String>();
        expectedColumnLabels.put(1, "column2-1");
        expectedColumnLabels.put(2, "column2-2");
        assertEquals("Incorrect column labels", expectedColumnLabels, rs.getColumnLabels());
        
        method.invoke(rs);
        verify(mockStatement2).close();
    }
    
    /**
     * Test the behavior of {@link MySQLDAOResultSet#executeNextStatementQuery()} 
     * when the query is interrupted.
     */
    @Test
    public void interruptdExecuteNextStatementQuery() throws SQLException, 
        NoSuchMethodException, SecurityException, IllegalAccessException, 
        IllegalArgumentException {
        
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        when(mockStatement2.isCanceled()).thenReturn(true);
        
        ResultSet realRs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(realRs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(0);
        when(mockStatement.executeQuery()).thenReturn(realRs);
        when(mockStatement2.executeQuery()).thenReturn(realRs);
        
        MySQLDAOResultSet<TransferObject> rs = new FakeDAOResultSet(
                Arrays.asList(mockStatement, mockStatement2));
        //first mockStatement should have been executed right away.
        //the call to executeNextStatementQuery should lose the first one and 
        //execute the second one
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement, never()).close();
        
        Method method = MySQLDAOResultSet.class.getDeclaredMethod(
                "executeNextStatementQuery");
        method.setAccessible(true);
        try {
            //the method should throw a QueryInterruptedException
            method.invoke(rs);
            //if we reach that point, test failed
            throw new AssertionError("a QueryInterruptedException should have been thrown");
        } catch (InvocationTargetException  e) {
            if (!(e.getCause() instanceof QueryInterruptedException)) {
                    throw new AssertionError("a QueryInterruptedException should have " +
                    		"been thrown");
            }
        }
    }
    
    /**
     * Test {@link MySQLDAOManager#next()}.
     */
    @Test
    public void testNext() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        ResultSet mockRs2 = mock(ResultSet.class);
        when(mockRs2.getMetaData()).thenReturn(metaData);
        when(mockStatement2.executeQuery()).thenReturn(mockRs2);
        ResultSet mockRs3 = mock(ResultSet.class);
        when(mockRs3.getMetaData()).thenReturn(metaData);
        when(mockStatement3.executeQuery()).thenReturn(mockRs3);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(
                Arrays.asList(mockStatement, mockStatement2, mockStatement3));
        verify(mockStatement).executeQuery();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        

        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs).next();
        verify(mockRs2, never()).next();
        verify(mockRs3, never()).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs, times(2)).next();
        verify(mockRs2, never()).next();
        verify(mockRs3, never()).next();
        
        //if the first ResultSet has no more result, the next statement should be executed
        when(mockRs.next()).thenReturn(false);
        when(mockRs2.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that the first statement was closed after its call to next
        verify(mockRs, times(3)).next();
        verify(mockStatement).close();
        //check that only the second ResultSet was used
        verify(mockRs2).next();
        verify(mockRs3, never()).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs2, times(2)).next();
        verify(mockRs3, never()).next();
        
        //OK, we move the the last statement
        when(mockRs2.next()).thenReturn(false);
        when(mockRs3.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that the second statement was closed after its call to next
        verify(mockRs2, times(3)).next();
        verify(mockStatement2).close();
        //check that only the second ResultSet was used
        verify(mockRs3).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs3, times(2)).next();
        
        //we pretend we iterated all results from all statements
        when(mockRs3.next()).thenReturn(false);
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs3, times(3)).next();
        verify(mockStatement3).close();
    }
    
    /**
     * Test the behavior of {@link MySQLDAOManager#next()} when the query 
     * is interrupted.
     */
    @Test
    public void shouldInterruptNext() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(0);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(mockStatement);        

        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //let's pretend that the statement was interrupted
        when(mockStatement.isCanceled()).thenReturn(true);
        try {
            //the method should throw a QueryInterruptedException
            myRs.next();
            //if we reach that point, test failed
            throw new AssertionError("a QueryInterruptedException should have been thrown");
        } catch (QueryInterruptedException  e) {
            //test passed
        }
        
        //statement should have been closed
        verify(mockStatement).close();
    }
    
    /**
     * Test {@link MySQLDAOResultSet#close()}.
     */
    @Test
    public void shouldClose() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(
                Arrays.asList(mockStatement, mockStatement2, mockStatement3));
        myRs.close();
        verify(mockStatement).close();
        verify(mockStatement2).close();
        verify(mockStatement3).close();
        assertEquals("Incorrect number of statements returned", 0, myRs.getStatementCount());
    }
    
    /**
     * Test {@link MySQLDAOResultSet#addStatement(BgeePreparedStatement)} and 
     * {@link MySQLDAOResultSet#addAllStatements(List)}
     * @throws SQLException 
     */
    @Test
    public void shouldAddStatement() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(0);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        MySQLDAOResultSet<TransferObject> rs = new FakeDAOResultSet(mockStatement);
        //first statement is immediately executed, so we do not count it
        assertEquals("Incorrect number of BgeePreparedStatement", 0, 
                rs.getStatementCount());
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        when(mockStatement2.executeQuery()).thenReturn(mockRs);
        rs.addStatement(mockStatement2);
        assertEquals("Incorrect number of BgeePreparedStatement", 1, 
                rs.getStatementCount());
        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
        when(mockStatement3.executeQuery()).thenReturn(mockRs);
        BgeePreparedStatement mockStatement4 = mock(BgeePreparedStatement.class);
        when(mockStatement4.executeQuery()).thenReturn(mockRs);
        rs.addAllStatements(Arrays.asList(mockStatement3, mockStatement4));
        assertEquals("Incorrect number of BgeePreparedStatement", 3, 
                rs.getStatementCount());
        
        //test if it correctly detects when a BgeePreparedStatement was already executed
        BgeePreparedStatement mockStatementTested = mock(BgeePreparedStatement.class);
        when(mockStatementTested.isExecuted()).thenReturn(true);
        
        try {
            BgeePreparedStatement mockStatement6 = mock(BgeePreparedStatement.class);
            when(mockStatement6.executeQuery()).thenReturn(mockRs);
            BgeePreparedStatement mockStatement7 = mock(BgeePreparedStatement.class);
            when(mockStatement7.executeQuery()).thenReturn(mockRs);
            rs.addAllStatements(Arrays.asList(mockStatement6, 
                    mockStatementTested, 
                    mockStatement7));
            //if we reach that point, test failed
            throw new AssertionError("A BgeePreparedStatement already executed should not " +
            		"be accepted");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
    
}
