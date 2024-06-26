package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the class {@link Entity}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EntityTest extends TestAncestor {
	/**
     * Log4j2 <code>Logger/code> of this class.
     */
    private final static Logger log = 
            LogManager.getLogger(EntityTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 
    
    /**
     * Test the overridden implementations {@link Entity#hashCode()} and 
     * {@link Entity#equals(Object)}, that is solely based on Entities' ID.
     */
    @Test
    public void testEqualsHashCode() {
    	//Create a private class extending the abstract class Entity
    	class MyEntity extends Entity {
			public MyEntity(String id) throws IllegalArgumentException {
				super(id);
			}
    	}
    	//And another one to test the equals method.
    	class MyEntity2 extends Entity {
			public MyEntity2(String id) throws IllegalArgumentException {
				super(id);
			}
    	}
    	
    	MyEntity entity1 = new MyEntity("ID1");
    	MyEntity entity1bis = new MyEntity("ID1");
    	MyEntity entity2 = new MyEntity("ID2");
    	
    	assertTrue("An Entity was not equal to itself!", entity1.equals(entity1));
    	
    	assertTrue("Two entities from the same class with the same ID were not equal", 
    			entity1.equals(entity1bis));
    	assertTrue("Two entities from the same class with the same ID were not equal", 
    			entity1bis.equals(entity1));
    	assertEquals("Two entities from the same class with the same ID " +
    			"do not have the same hashCode", entity1.hashCode(), entity1bis.hashCode());

    	assertFalse("Two entities with different IDs were equal", entity1.equals(entity2));
    	assertFalse("Two entities with different IDs were equal", entity2.equals(entity1));
    	//of note, it is not mandatory for non-equal Objects to have different hashCodes, 
    	//only the opposite is true. So here, we do not test hashCodes.
    	
    	MyEntity2 entity3 = new MyEntity2("ID1");
    	//entity1 and entity3 have a same ID so their hashcode should be equal, 
    	//but the equals method should return false
    	assertEquals("Two entities from different class but with same ID have " +
    			"different hashCodes", entity1.hashCode(), entity3.hashCode());
    	assertFalse("Two entities from different class were equal", 
    			entity1.equals(entity3));
    }
}
