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
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.junit.Test;
import org.mockito.Mockito;

import com.mysql.jdbc.ResultSetMetaData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

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
    private class FakeTO extends TransferObject {
        private static final long serialVersionUID = 1L;

        public FakeTO() {
            super();
        }
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
        public FakeDAOResultSet(List<BgeePreparedStatement> statements, 
                boolean filterDuplicates) {
            super(statements, filterDuplicates);
        }
        public FakeDAOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
                int rowCountParamIndex, int rowCount, boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }
        public FakeDAOResultSet(BgeePreparedStatement statement, int offsetParamIndex, 
                int rowCountParamIndex, int rowCount, int stepCount, boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, 
                    rowCount, stepCount, filterDuplicates);
        }
        @Override
        public TransferObject getNewTO() throws DAOException {
            try {
                //just a hack to use mocked ResultSet
                return this.getCurrentResultSet().unwrap(FakeTO.class);
            } catch (SQLException e) {
                throw new DAOException(e);
            }
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
        //we will test a situation where mockStatement3 and mockStatement4 return no results. 
        //mockStatement5 should be executed despite that. 
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement4 = mock(BgeePreparedStatement.class);
        BgeePreparedStatement mockStatement5 = mock(BgeePreparedStatement.class);
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
        ResultSet mockRs4 = mock(ResultSet.class);
        when(mockRs4.getMetaData()).thenReturn(metaData);
        when(mockStatement4.executeQuery()).thenReturn(mockRs4);
        ResultSet mockRs5 = mock(ResultSet.class);
        when(mockRs5.getMetaData()).thenReturn(metaData);
        when(mockStatement5.executeQuery()).thenReturn(mockRs5);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(Arrays.asList(
                mockStatement, mockStatement2, mockStatement3, mockStatement4, mockStatement5));
        verify(mockStatement).executeQuery();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        verify(mockStatement4, never()).executeQuery();
        verify(mockStatement5, never()).executeQuery();
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertFalse("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        

        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs).next();
        verify(mockRs2, never()).next();
        verify(mockRs3, never()).next();
        verify(mockRs4, never()).next();
        verify(mockRs5, never()).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs, times(2)).next();
        verify(mockRs2, never()).next();
        verify(mockRs3, never()).next();
        verify(mockRs4, never()).next();
        verify(mockRs5, never()).next();
        
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
        verify(mockRs4, never()).next();
        verify(mockRs5, never()).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the first ResultSet was used
        verify(mockRs2, times(2)).next();
        verify(mockRs3, never()).next();
        verify(mockRs4, never()).next();
        verify(mockRs5, never()).next();
        
        //now we test a situation where the next statements has no result. The one afterwards 
        //should be used immediately. 
        when(mockRs2.next()).thenReturn(false);
        when(mockRs3.next()).thenReturn(false);
        when(mockRs4.next()).thenReturn(false);
        when(mockRs5.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that the second and third statements were closed after the call to next
        verify(mockRs2, times(3)).next();
        verify(mockStatement2).close();
        verify(mockRs3, times(1)).next();
        verify(mockStatement3).close();
        verify(mockRs4, times(1)).next();
        verify(mockStatement4).close();
        //check that only the last ResultSet was used
        verify(mockRs5).next();
        //try again
        assertTrue("Incorrect value returend by next", myRs.next());
        //check that only the last ResultSet was used
        verify(mockRs5, times(2)).next();
        
        //we pretend we iterated all results from all statements
        when(mockRs5.next()).thenReturn(false);
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs5, times(3)).next();
        verify(mockStatement5).close();
        assertFalse("Incorrect value returend by next", myRs.next());
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} when duplicate filtering is requested.
     */
    @Test
    public void testNextWithFiltering() throws SQLException {
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

        //4 results returned by first resultset, the last 2 will be discarded 
        //because equal to first ones, and will move to next query. 3 results returned 
        //by second resultset, second one discarded because equal to another one, 
        //move to the next query. 1 results returned by it.
        when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).
            thenReturn(false);
        when(mockRs2.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockRs3.next()).thenReturn(true).thenReturn(false);
        FakeTO fakeTO1 = new FakeTO();
        FakeTO fakeTO2 = new FakeTO();
        FakeTO fakeTO3 = new FakeTO();
        FakeTO fakeTO4 = new FakeTO();
        FakeTO fakeTO5 = new FakeTO();
        when(mockRs.unwrap(FakeTO.class)).thenReturn(fakeTO1).thenReturn(fakeTO2).
            thenReturn(fakeTO1).thenReturn(fakeTO2);
        when(mockRs2.unwrap(FakeTO.class)).thenReturn(fakeTO3).thenReturn(fakeTO1).
            thenReturn(fakeTO4);
        when(mockRs3.unwrap(FakeTO.class)).thenReturn(fakeTO5);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(Arrays.asList(
                mockStatement, mockStatement2, mockStatement3), true);
        verify(mockStatement).executeQuery();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertFalse("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(1)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(2)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        //OK, here we should discard the last two results, and move to next query
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO3, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(1)).next();
        //next TO discarded, move to the next one
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO4, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(3)).next();
        //no more result, move to next query
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO5, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(4)).next();
        verify(mockStatement2, times(1)).close();
        verify(mockRs2, times(1)).close();
        verify(mockRs3, times(1)).next();
        //end of all results
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(4)).next();
        verify(mockStatement2, times(1)).close();
        verify(mockRs2, times(1)).close();
        verify(mockRs3, times(2)).next();
        verify(mockStatement3, times(1)).close();
        verify(mockRs3, times(1)).close();

        //make sure there is no additional calls to getTO, as we use this trick 
        //of using the unwrap method to generate the TO
        verify(mockRs, times(4)).unwrap(FakeTO.class);
        verify(mockRs2, times(3)).unwrap(FakeTO.class);
        verify(mockRs3, times(1)).unwrap(FakeTO.class);
        
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} with duplicates not filtered.
     */
    @Test
    public void testNextWithDuplicatesWithoutFiltering() throws SQLException {
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

        //4 results returned by first resultset, the last 2 will be discarded 
        //because equal to first ones, and will move to next query. 3 results returned 
        //by second resultset, second one discarded because equal to another one, 
        //move to the next query. 1 results returned by it.
        when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).
            thenReturn(false);
        when(mockRs2.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockRs3.next()).thenReturn(true).thenReturn(false);
        FakeTO fakeTO1 = new FakeTO();
        FakeTO fakeTO2 = new FakeTO();
        FakeTO fakeTO3 = new FakeTO();
        FakeTO fakeTO4 = new FakeTO();
        FakeTO fakeTO5 = new FakeTO();
        when(mockRs.unwrap(FakeTO.class)).thenReturn(fakeTO1).thenReturn(fakeTO2).
            thenReturn(fakeTO1).thenReturn(fakeTO2);
        when(mockRs2.unwrap(FakeTO.class)).thenReturn(fakeTO3).thenReturn(fakeTO1).
            thenReturn(fakeTO4);
        when(mockRs3.unwrap(FakeTO.class)).thenReturn(fakeTO5);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(Arrays.asList(
                mockStatement, mockStatement2, mockStatement3), false);
        verify(mockStatement).executeQuery();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertFalse("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(1)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(2)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(3)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(4)).next();
        verify(mockStatement2, never()).executeQuery();
        verify(mockStatement3, never()).executeQuery();
        
        //no more result, move to next query
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO3, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(1)).next();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(2)).next();
        verify(mockStatement3, never()).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO4, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(3)).next();
        verify(mockStatement3, never()).executeQuery();
        
        //next query
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO5, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(4)).next();
        verify(mockStatement2, times(1)).close();
        verify(mockRs2, times(1)).close();
        verify(mockRs3, times(1)).next();
        
        //end of all results
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(1)).close();
        verify(mockStatement2, times(1)).executeQuery();
        verify(mockRs2, times(4)).next();
        verify(mockStatement2, times(1)).close();
        verify(mockRs2, times(1)).close();
        verify(mockRs3, times(2)).next();
        verify(mockStatement3, times(1)).close();
        verify(mockRs3, times(1)).close();

        //make sure there is no additional calls to getTO, as we use this trick 
        //of using the unwrap method to generate the TO
        verify(mockRs, times(4)).unwrap(FakeTO.class);
        verify(mockRs2, times(3)).unwrap(FakeTO.class);
        verify(mockRs3, times(1)).unwrap(FakeTO.class);
        
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} using the limit feature without defining 
     * a maximum number of iterations, and requesting to filter duplicates.
     */
    @Test
    public void testNextWithLimitAndFilterDuplicates() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        //3 results returned by first resultset, the second one will be discarded 
        //because equal to first one. 2 results returned by second resultset, 
        //first one discarded because equal to another one. A third query will be made 
        //to make sure there are no more results.
        when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false).
            thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(false);
        FakeTO fakeTO1 = new FakeTO();
        FakeTO fakeTO2 = new FakeTO();
        FakeTO fakeTO3 = new FakeTO();
        when(mockRs.unwrap(FakeTO.class)).thenReturn(fakeTO1).thenReturn(fakeTO1).
            thenReturn(fakeTO2).thenReturn(fakeTO2).thenReturn(fakeTO3);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(mockStatement, 
                2, 3, 20, true);
        verify(mockStatement).setInt(2, 0);
        verify(mockStatement).setInt(3, 20);
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertTrue("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertTrue("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(1)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        //next result redundant, skip to 3rd result
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(3)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        //end of first limit query, move to next query. The first result is redundant 
        //and is discarded
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO3, myRs.getTO());
        verify(mockRs, times(6)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockRs, times(1)).close();
        
        //end of second query - an additional third query should be run to make sure 
        //there are no more results
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(8)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(3)).setInt(3, 20);
        verify(mockStatement, times(6)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(3)).executeQuery();
        verify(mockRs, times(3)).close();
        verify(mockStatement, times(1)).close();
        //make sure there is no additional calls to getTO, as we use this trick 
        //of using the unwrap method to generate the TO
        verify(mockRs, times(5)).unwrap(FakeTO.class);
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} using the limit feature without defining 
     * a maximum number of iterations, and with duplicates not filtered.
     */
    @Test
    public void testNextWithLimitAndDuplicatesWithoutFiltering() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        //3 results returned by first resultset, the second one will be discarded 
        //because equal to first one. 2 results returned by second resultset, 
        //first one discarded because equal to another one. A third query will be made 
        //to make sure there are no more results.
        when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false).
            thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(false);
        FakeTO fakeTO1 = new FakeTO();
        FakeTO fakeTO2 = new FakeTO();
        FakeTO fakeTO3 = new FakeTO();
        when(mockRs.unwrap(FakeTO.class)).thenReturn(fakeTO1).thenReturn(fakeTO1).
            thenReturn(fakeTO2).thenReturn(fakeTO2).thenReturn(fakeTO3);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(mockStatement, 
                2, 3, 20, false);
        verify(mockStatement).setInt(2, 0);
        verify(mockStatement).setInt(3, 20);
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertTrue("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(1)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO1, myRs.getTO());
        verify(mockRs, times(2)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(3)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        //end of first limit query, move to next query. The first result is redundant 
        //and is discarded
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO2, myRs.getTO());
        verify(mockRs, times(5)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockRs, times(1)).close();
        assertTrue("Incorrect value returend by next", myRs.next());
        assertEquals("Incorrect TO returned", fakeTO3, myRs.getTO());
        verify(mockRs, times(6)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockRs, times(1)).close();
        
        //end of second query - an additional third query should be run to make sure 
        //there are no more results
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(8)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(3)).setInt(3, 20);
        verify(mockStatement, times(6)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(3)).executeQuery();
        verify(mockRs, times(3)).close();
        verify(mockStatement, times(1)).close();
        //make sure there is no additional calls to getTO, as we use this trick 
        //of using the unwrap method to generate the TO
        verify(mockRs, times(5)).unwrap(FakeTO.class);
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} using the limit feature without defining 
     * a maximum number of iterations.
     */
    @Test
    public void testNextWithLimitWithoutMax() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(mockStatement, 
                2, 3, 20, false);
        verify(mockStatement).setInt(2, 0);
        verify(mockStatement).setInt(3, 20);
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertTrue("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(1)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        assertTrue("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(2)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        //OK, move to next iteration of the limit clause: no more result for this iteration 
        //(first thenReturn), results for the next iteration (second thenReturn)
        when(mockRs.next()).thenReturn(false).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(4)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockRs, times(1)).close();
        
        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(5)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        assertTrue("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(6)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        
        //OK, move to next iteration of the limit clause, that should return no results
        when(mockRs.next()).thenReturn(false).thenReturn(false);
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(8)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(3)).setInt(3, 20);
        verify(mockStatement, times(6)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(3)).executeQuery();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(3)).close();
        assertFalse("Incorrect value returend by next", myRs.next());
        verify(mockRs, times(8)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(3)).setInt(3, 20);
        verify(mockStatement, times(6)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(3)).executeQuery();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(3)).close();
    }
    
    /**
     * Test {@link MySQLDAOManager#next()} using the limit feature and defining 
     * a maximum number of iterations.
     */
    @Test
    public void testNextWithLimitWithMax() throws SQLException {
        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(0);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        MySQLDAOResultSet<TransferObject> myRs = new FakeDAOResultSet(mockStatement, 
                2, 3, 20, 2, false);
        verify(mockStatement).setInt(2, 0);
        verify(mockStatement).setInt(3, 20);
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertTrue("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(1)).next();
        //the statement should not have been queried again
        verify(mockStatement, times(2)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(1)).executeQuery();
        
        //OK, move to next iteration of the limit clause: no more result for this iteration 
        //(first thenReturn), results for the next iteration (second thenReturn)
        when(mockRs.next()).thenReturn(false).thenReturn(true);
        assertTrue("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(3)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockRs, times(1)).close();
        
        //OK, move to next iteration of the limit clause; even if there are more results, 
        //we reached the max number of iterations
        when(mockRs.next()).thenReturn(false).thenReturn(true);
        assertFalse("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(4)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(2)).close();
        assertFalse("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(4)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(2)).setInt(3, 20);
        verify(mockStatement, times(4)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(2)).executeQuery();
        verify(mockStatement, times(1)).close();
        verify(mockRs, times(2)).close();
        
        
        //OK, now we test when there are no more results before reaching the maximum number 
        //of iterations. We should still continue iterations (when the LIMIT is in a sub-query, 
        //there is no guarantee to retrieve results at each iteration; we can have results 
        //after an iteration with no result; it is then mandatory to provide the stepCount)
        mockStatement = mock(BgeePreparedStatement.class);
        mockRs = mock(ResultSet.class);
        when(mockRs.getMetaData()).thenReturn(metaData);
        when(mockStatement.executeQuery()).thenReturn(mockRs);
        
        myRs = new FakeDAOResultSet(mockStatement, 2, 3, 20, 4, false);
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertTrue("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        
        when(mockRs.next()).thenReturn(true);
        assertTrue("Incorrect value returned by next", myRs.next());
        
        //OK, move to next iteration of the limit clause: no more result for this iteration 
        //(first thenReturn); start the second iteration, no results as well 
        //(second thenReturn); start the third iteration, there is a result (third thenReturn); 
        //there is only one result (fourth thenReturn returns false); 
        //start the last requested iteration (fifth thenReturn), there is no result.
        when(mockRs.next()).thenReturn(false).thenReturn(false).thenReturn(true).
            thenReturn(false).thenReturn(false);
        assertTrue("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(4)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(3)).setInt(3, 20);
        verify(mockStatement, times(6)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(3)).executeQuery();
        verify(mockRs, times(2)).close();
        assertFalse("Incorrect value returned by next", myRs.next());
        verify(mockRs, times(6)).next();
        verify(mockStatement, times(1)).setInt(2, 0);
        verify(mockStatement, times(1)).setInt(2, 20);
        verify(mockStatement, times(1)).setInt(2, 40);
        verify(mockStatement, times(1)).setInt(2, 60);
        verify(mockStatement, times(4)).setInt(3, 20);
        verify(mockStatement, times(8)).setInt(anyInt(), anyInt());
        verify(mockStatement, times(4)).executeQuery();
        verify(mockRs, times(4)).close();
        verify(mockStatement, times(1)).close();
    }
    
    /**
     * Test {@link MySQLDAOResultSet#getAllTOs()}.
     */
    @Test
    public void testGetAllTOs() {
        MySQLRelationTOResultSet myRs = Mockito.mock(MySQLRelationTOResultSet.class);
        
        List<RelationTO> expectedTOs = 
                Arrays.asList(new RelationTO("1","2"), new RelationTO("3","4"));
        
        when(myRs.getTO()).thenReturn(expectedTOs.get(0), expectedTOs.get(1));
        when(myRs.next()).thenReturn(true, true, false);

        when(myRs.getAllTOs()).thenCallRealMethod();
        
        assertEquals("Incorrect retried TOs by getAllTOs", expectedTOs, myRs.getAllTOs());
        verify(myRs).close();
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
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertFalse("Incorrect use of limit feature", myRs.isUsingLimitFeature());     

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
        assertFalse("Incorrect use of duplicate filtering", myRs.isFilterDuplicates());
        assertFalse("Incorrect use of limit feature", myRs.isUsingLimitFeature());
        myRs.close();
        verify(mockStatement).close();
        verify(mockStatement2).close();
        verify(mockStatement3).close();
        assertEquals("Incorrect number of statements returned", 0, myRs.getStatementCount());
    }
    
//    /**
//     * Test {@link MySQLDAOResultSet#addStatement(BgeePreparedStatement)} and 
//     * {@link MySQLDAOResultSet#addAllStatements(List)}
//     * @throws SQLException 
//     */
//    @Test
//    public void shouldAddStatement() throws SQLException {
//        BgeePreparedStatement mockStatement = mock(BgeePreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
//        when(mockRs.getMetaData()).thenReturn(metaData);
//        when(metaData.getColumnCount()).thenReturn(0);
//        when(mockStatement.executeQuery()).thenReturn(mockRs);
//        
//        MySQLDAOResultSet<TransferObject> rs = new FakeDAOResultSet(mockStatement);
//        //first statement is immediately executed, so we do not count it
//        assertEquals("Incorrect number of BgeePreparedStatement", 0, 
//                rs.getStatementCount());
//        BgeePreparedStatement mockStatement2 = mock(BgeePreparedStatement.class);
//        when(mockStatement2.executeQuery()).thenReturn(mockRs);
//        rs.addStatement(mockStatement2);
//        assertEquals("Incorrect number of BgeePreparedStatement", 1, 
//                rs.getStatementCount());
//        BgeePreparedStatement mockStatement3 = mock(BgeePreparedStatement.class);
//        when(mockStatement3.executeQuery()).thenReturn(mockRs);
//        BgeePreparedStatement mockStatement4 = mock(BgeePreparedStatement.class);
//        when(mockStatement4.executeQuery()).thenReturn(mockRs);
//        rs.addAllStatements(Arrays.asList(mockStatement3, mockStatement4));
//        assertEquals("Incorrect number of BgeePreparedStatement", 3, 
//                rs.getStatementCount());
//        
//        //test if it correctly detects when a BgeePreparedStatement was already executed
//        BgeePreparedStatement mockStatementTested = mock(BgeePreparedStatement.class);
//        when(mockStatementTested.isExecuted()).thenReturn(true);
//        
//        try {
//            BgeePreparedStatement mockStatement6 = mock(BgeePreparedStatement.class);
//            when(mockStatement6.executeQuery()).thenReturn(mockRs);
//            BgeePreparedStatement mockStatement7 = mock(BgeePreparedStatement.class);
//            when(mockStatement7.executeQuery()).thenReturn(mockRs);
//            rs.addAllStatements(Arrays.asList(mockStatement6, 
//                    mockStatementTested, 
//                    mockStatement7));
//            //if we reach that point, test failed
//            throw new AssertionError("A BgeePreparedStatement already executed should not " +
//            		"be accepted");
//        } catch (IllegalArgumentException e) {
//            //test passed
//        }
//    }
    
}
