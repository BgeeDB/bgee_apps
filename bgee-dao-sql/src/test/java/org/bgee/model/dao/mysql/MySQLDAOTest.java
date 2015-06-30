package org.bgee.model.dao.mysql;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Unit tests for the class {@link MySQLDAO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLDAOTest extends TestAncestor
{
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOTest.class.getName());
    
    /**
     * Default constructor.
     */
    public MySQLDAOTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    @Test
    public void shouldConvertToIntList() {
        assertEquals("Incorrect convertion to List of Integers", Arrays.asList(10, 24, 32), 
                MySQLDAO.convertToOrderedIntList(Arrays.asList("10", "24", "32")));

        assertEquals("Incorrect convertion to List of Integers", Arrays.asList(10, 24, 32), 
                MySQLDAO.convertToOrderedIntList(Arrays.asList("24", "32", "10")));

        //test throwing of NumberFormatException
        try {
            MySQLDAO.convertToOrderedIntList(Arrays.asList("10", "24a", "32"));
            //should have thrown an exception, test failed
            throw new AssertionError("No exception was thrown when converting " +
            		"unparsable String into Integer");
        } catch (NumberFormatException e) {
            //test passed
        }
    }
}
