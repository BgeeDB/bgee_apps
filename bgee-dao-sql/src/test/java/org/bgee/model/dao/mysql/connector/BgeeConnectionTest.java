package org.bgee.model.dao.mysql.connector;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.TestAncestor;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.mock.MockDriver;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BgeeConnectionTest extends TestAncestor
{
	private final static Logger log = LogManager.getLogger(BgeeConnectionTest.class.getName());
	
	/**
	 * Default constructor.
	 */
	public BgeeConnectionTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
    
    /**
     * Test {@link BgeeConnection#prepareStatement(String)}
     */
    @Test
    public void shouldPrepareStatement() throws SQLException {
        MockDriver.initialize();
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        assertNotNull("Could not acquire a BgeePreparedStatement", 
                con.prepareStatement("test"));
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                1, con.getStatementCount());
        assertNotNull("Could not acquire a BgeePreparedStatement", 
                con.prepareStatement("test"));
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                2, con.getStatementCount());

        MockDriver.initialize();
    }
	
	/**
	 * Test {@link BgeeConnection#close()}
	 */
	@Test
	public void shouldClose() throws SQLException {
	    MockDriver.initialize();
	    MySQLDAOManager manager = new MySQLDAOManager();
	    manager = spy(manager);
	    
	    BgeeConnection con = new BgeeConnection(manager, MockDriver.getMockConnection(), 
	            "ID1");
	    //to test that the connection closes its statements
	    con.prepareStatement("test");
        con.prepareStatement("test");
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                2, con.getStatementCount());
        
        con.close();
        verify(MockDriver.getMockConnection()).close();
        verify(MockDriver.getMockStatement(), times(2)).close();
        verify(manager).connectionClosed("ID1");
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                0, con.getStatementCount());
        //as there was no ongoing transaction, rollback should not have been called
        verify(MockDriver.getMockConnection(), never()).rollback();
        
        //now we test that if a connection is closed during a transaction, 
        //the transaction should be rollback
        con = new BgeeConnection(manager, MockDriver.getMockConnection(), 
                "ID1");
        con.startTransaction();
        con.close();
        verify(MockDriver.getMockConnection()).rollback();
        
        MockDriver.initialize();
	}
    
    /**
     * Test {@link BgeeConnection#kill()}
     */
    @Test
    public void shouldKill() throws SQLException {
        MockDriver.initialize();
        MySQLDAOManager manager = new MySQLDAOManager();
        manager = spy(manager);
        
        BgeeConnection con = new BgeeConnection(manager, MockDriver.getMockConnection(), 
                "ID1");
        //to test that the connection closes its statements
        con.prepareStatement("test");
        con.prepareStatement("test");
        
        con.kill();
        verify(MockDriver.getMockConnection()).close();
        verify(MockDriver.getMockStatement(), times(2)).cancel();
        verify(manager).connectionClosed("ID1");
        
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeeConnection#statementClosed(BgeePreparedStatement)}.
     */
    @Test
    public void testStatementClosed() throws SQLException {
        MockDriver.initialize();
        
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        //to test that the connection closes its statements
        BgeePreparedStatement stmt = con.prepareStatement("test");
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                1, con.getStatementCount());
        con.statementClosed(stmt);
        assertEquals("Incorrect number of BgeePreparedStatement held by the BgeeConnection", 
                0, con.getStatementCount());
        
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeeConnection#getRealConnection()}.
     */
    @Test
    public void shouldGetRealConnection() {
        MockDriver.initialize();
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        assertSame("Incorrect real Connection returned", MockDriver.getMockConnection(), 
                con.getRealConnection());
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeeConnection#getId()}.
     */
    @Test
    public void shouldGetId() {
        BgeeConnection con = new BgeeConnection(null, null, "ID1");
        assertEquals("Incorrect ID returned", "ID1", con.getId());
    }
	
	/**
	 * Test {@link BgeeConnection#isClosed()}.
	 */
	@Test
	public void testIsClosed() throws SQLException {
	    //BgeeConnection should simply forward the call to the real connection
	    MockDriver.initialize();
        MySQLDAOManager manager = new MySQLDAOManager();
        manager = spy(manager);
        
        BgeeConnection con = new BgeeConnection(manager, MockDriver.getMockConnection(), 
                "ID1");
        when(MockDriver.getMockConnection().isClosed()).thenReturn(true);
        assertTrue("Incorrect value returned by isClosed", con.isClosed());
        
        MockDriver.initialize();
	}
	
	/**
	 * Test {@link BgeeConnection#startTransaction()}.
	 */
	@Test
	public void shouldStartTransaction() throws IllegalStateException, SQLException {
        MockDriver.initialize();
        
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        assertFalse("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        con.startTransaction();
        verify(MockDriver.getMockConnection()).setAutoCommit(false);
        assertTrue("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        //calling startTransaction before closing the transaction should throw 
        //an IllegalStateException
        try {
            con.startTransaction();
            //if we reach that point, test failed
            throw new AssertionError("startTransaction did not throw an " +
            		"IllegalStateException as expected");
        } catch (IllegalStateException e) {
            //test passed.
        }
        assertTrue("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        
        //now end the transaction to try to start a new one
        con.commit();
        con.startTransaction();
        //check that it also work with rollback
        con.rollback();
        con.startTransaction();
        
        MockDriver.initialize();
	}
    
    /**
     * Test {@link BgeeConnection#commit()}.
     */
    @Test
    public void shouldCommit() throws IllegalStateException, SQLException {
        MockDriver.initialize();
        
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        //if we try to commit while there was no ongoing transaction, 
        //an IllegalStateException should be thrown
        try {
            con.commit();
            //if we reach that point, test failed
            throw new AssertionError("commit did not throw an " +
                    "IllegalStateException as expected");
        } catch (IllegalStateException e) {
            //test passed.
        }
        
        assertFalse("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        con.startTransaction();
        con.commit();
        verify(MockDriver.getMockConnection()).setAutoCommit(true);
        verify(MockDriver.getMockConnection()).commit();
        assertFalse("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        
        //trying to commit again with no ongoing transaction, 
        //an IllegalStateException should be thrown
        try {
            con.commit();
            //if we reach that point, test failed
            throw new AssertionError("commit did not throw an " +
                    "IllegalStateException as expected");
        } catch (IllegalStateException e) {
            //test passed.
        }
        
        MockDriver.initialize();
    }
    
    /**
     * Test {@link BgeeConnection#rollback()}.
     */
    @Test
    public void shouldRollback() throws IllegalStateException, SQLException {
        MockDriver.initialize();
        
        BgeeConnection con = new BgeeConnection(null, MockDriver.getMockConnection(), 
                "ID1");
        //if we try to commit while there was no ongoing transaction, 
        //an IllegalStateException should be thrown
        try {
            con.rollback();
            //if we reach that point, test failed
            throw new AssertionError("commit did not throw an " +
                    "IllegalStateException as expected");
        } catch (IllegalStateException e) {
            //test passed.
        }
        
        assertFalse("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        con.startTransaction();
        con.rollback();
        verify(MockDriver.getMockConnection()).setAutoCommit(true);
        verify(MockDriver.getMockConnection()).rollback();
        assertFalse("Incorrect value returned by isOngoingTransaction", 
                con.isOngoingTransaction());
        
        //trying to commit again with no ongoing transaction, 
        //an IllegalStateException should be thrown
        try {
            con.rollback();
            //if we reach that point, test failed
            throw new AssertionError("commit did not throw an " +
                    "IllegalStateException as expected");
        } catch (IllegalStateException e) {
            //test passed.
        }
        
        MockDriver.initialize();
    }
}
