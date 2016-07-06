package org.bgee.model.expressiondata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Unit tests for {@link Condition}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 June 2016
 * @since Bgee 13 June 2016
 */
public class ConditionTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ConditionTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test that equals/hashCode/compareTo are consistent for {@code Condition} class.
     */
    @Test
    public void testEqualsHashCodeCompareTo() {
        Condition c1 = new Condition("Anat1", "stage1");
        Condition c2 = new Condition("Anat1", "stage1");
        assertTrue("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect hashCode", c1.hashCode(), c2.hashCode());
        assertEquals("Incorrect compareTo", 0, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 0, c2.compareTo(c1));
        
        c1 = new Condition(null, "stage1");
        c2 = new Condition(null, "stage1");
        assertTrue("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect hashCode", c1.hashCode(), c2.hashCode());
        assertEquals("Incorrect compareTo", 0, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 0, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", null);
        c2 = new Condition("Anat1", null);
        assertTrue("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect hashCode", c1.hashCode(), c2.hashCode());
        assertEquals("Incorrect compareTo", 0, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 0, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", "stage1");
        c2 = new Condition("Anat2", "stage1");
        assertFalse("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect compareTo", -1, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 1, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", "stage2");
        c2 = new Condition("Anat2", "stage1");
        assertFalse("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect compareTo", -1, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 1, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", "stage2");
        c2 = new Condition(null, "stage1");
        assertFalse("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect compareTo", -1, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 1, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", "stage1");
        c2 = new Condition("Anat1", "stage2");
        assertFalse("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect compareTo", -1, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 1, c2.compareTo(c1));
        
        c1 = new Condition("Anat1", "stage1");
        c2 = new Condition("Anat1", null);
        assertFalse("Incorrect equals", c1.equals(c2));
        assertEquals("Incorrect compareTo", -1, c1.compareTo(c2));
        assertEquals("Incorrect compareTo", 1, c2.compareTo(c1));
    }
}
