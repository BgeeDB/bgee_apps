package org.bgee.model.properties;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Class testing the functionalities of 
 * {@link org.bgee.model.BgeeProperties BgeeProperties}. 
 * <p>
 * The test about <b>the loading</b> of <code>BgeeProperties</code> 
 * are performed in {@link PropertiesFromFileTest}, {@link PropertiesFromSystemTest}, 
 * and {@link PropertiesMixLoadingTest}.
 * It has to be done in a different classes, 
 * as the properties are read only once at class loading, so only once 
 * for a given <code>ClassLoader</code>.
 * <p>
 * This class does not test the loading behavior, it test the actual functionalities 
 * of <code>BgeeProperties</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeePropertiesTest extends TestAncestor
{
    private final static Logger log = LogManager.getLogger(BgeePropertiesTest.class.getName());
	
	/**
	 * Default Constructor. 
	 */
	public BgeePropertiesTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Class testing how to obtain a same instance, and different instances 
	 * of <code>BgeeProperties</code> from one thread.
	 */
	@Test
	public void shouldAcquirePropsInOneThread()
	{
		//acquire a first instance of BgeeProperties
		BgeeProperties prop1  = BgeeProperties.getBgeeProperties();
		//a consecutive call should return the same instance
		BgeeProperties prop2 = BgeeProperties.getBgeeProperties();
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				prop1, prop2);
		//so of course any change to one will be reflected on the other
		prop1.setJdbcDriver("mytest");
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				prop1.getJdbcDriver(), prop2.getJdbcDriver());
		
		//but if we release the BgeeProperties, we acquire a new instance
		prop1.release();
		BgeeProperties newProp1 = BgeeProperties.getBgeeProperties();
		assertNotEquals("The BgeeProperties was not correctly released", 
				prop1, newProp1);
		//so of course any change to one will not be seen by the other
		prop1.setJdbcDriver("mytest");
		assertNotEquals("The BgeeProperties was not correctly released", 
				prop1.getJdbcDriver(), newProp1.getJdbcDriver());
		
		//then, we keep getting the same instance if we call getBgeeProperties() again
		BgeeProperties newProp2 = BgeeProperties.getBgeeProperties();
		assertEquals("A same thread acquired two instances of BgeeProperties", 
				newProp1, newProp2);
	}
}
