package org.bgee.model.dao.api;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.junit.Test;

/**
 * Unit tests testing the loading and closing of {@link DAOManager}.

 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ManagerLoadAndReleaseTest extends TestAncestor {
	/**
     * <code>Logger</code> of the class. 
     */
    private final static Logger log = LogManager.getLogger(ManagerLoadAndReleaseTest.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
	@Test
	public void shouldLoadManager() {
		assertNotNull("Failed to obtain a default DAOManager", DAOManager.getDAOManager());
	}
}
