package org.bgee.model.dao.api;

import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Parent class of all classes implementing unit testing. 
 * It allows to automatically log starting, succeeded and failed tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public abstract class TestAncestor 
{
	/**
	 * If <code>true</code>, an <code>Error</code> is thrown when the default constructor 
	 * of <code>org.bgee.model.dao.api.MockDAOManager</code> is called. 
	 * We cannot store this information in the <code>MockDAOManager</code> class, 
	 * as it would load the <code>DAOManager</code> to set it, while we need to set 
	 * this attribute first. 
	 */
	protected static boolean thrownInstantiationException = false;
	
	/**
	 * Default Constructor. 
	 */
	public TestAncestor()
	{
		
	}
	/**
	 * A <code>TestWatcher</code> to log starting, succeeded and failed tests. 
	 */
	@Rule
	public TestWatcher watchman = new TestWatcher() {
	    @Override
	    protected void starting(Description description) {
	    	getLogger().info("Starting test: {}", description);
	    }
	    @Override
	    protected void failed(Throwable e, Description description) {
	    	if (getLogger().isErrorEnabled()) {
	    		getLogger().error("Test failed: " + description, e);
	    	}
	    }
	    @Override
	    protected void succeeded(Description description) {
	    	getLogger().info("Test succeeded: {}", description);
	    }
	};
	
	/**
	 * Return the logger of the class. 
	 * @return 	A <code>Logger</code>
	 */
	protected abstract Logger getLogger();
}
