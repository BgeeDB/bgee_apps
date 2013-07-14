package org.bgee.model.dao.api;

import static org.testng.AssertJUnit.assertEquals;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.testng.annotations.*;

public class MultiThreadTest extends TestAncestor {
	/**
     * <code>Logger</code> of the class. 
     */
    private final static Logger log = 
    		LogManager.getLogger(MultiThreadTest.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Launch all tests from {@link ManagerLoadAndReleaseTest} in a multi-threaded 
	 * way thanks to testNG. Only tests not modifying static attributes are used 
	 * (so tests that can be run in parallel without affecting other tests running).
	 */
	@Test(threadPoolSize = 30, invocationCount = 300,  timeOut = 180000)
	public void launchAllTest() throws Exception {
		ManagerLoadAndReleaseTest test = new ManagerLoadAndReleaseTest();
		test.shouldCloseDAOManager();
		test.shouldGetDAOManager();
		test.shouldHaveDAOManager();
		test.shouldKillManager();
		test.shouldGetDAOs();
	}
	
	/**
	 * Check that there is no <code>DAOManager</code> left in the pool 
	 * after all tests. A <code>AfterClass</code> is not supposed to be used 
	 * for that purpose, but that will be good enough. 
	 */
	@SuppressWarnings("unchecked")
	@AfterClass
	public static void checkNoManagerRemaining() throws NoSuchFieldException, 
	    SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = DAOManager.class.getDeclaredField("managers");
		field.setAccessible(true);
		Object managers = field.get(null);
		assertEquals("There are managers left", 0, 
				((ConcurrentMap<Long, DAOManager>) managers).size());
	}
}
