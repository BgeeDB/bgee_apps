package org.bgee.model.dao.api;

import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * A class to simulate an implementation of {@link DAOManager}, that is discovered 
 * by the <code>Service Loader</code> thanks to the test file 
 * <code>META-INF/services/org.bgee.model.dao.api.DAOManager</code>. 
 * <p>
 * All methods delegate to the mocked <code>DAOManager</code> stored 
 * in the public attribute {@link mockManager}, which should thus be used to specify 
 * the expected behaviors to mock. 
 * <p>
 * This class is the same than {@link MockDAOManager}, but is used to test the behavior 
 * when the <code>ServiceLoader</code> discovers several service providers. 
 * This provider should be the second one loaded by the <code>ServiceLoader</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MockDAOManager2 extends DAOManager {
	
	/**
	 * If <code>true</code>, an <code>Error</code> is thrown when the default constructor 
	 * is called. 
	 */
	protected static boolean thrownInstantiationException = false;
	
	public final DAOManager mockManager;
	
	/**
	 * Default constructor used by the service loader.
	 */
	public MockDAOManager2() {
		if (TestAncestor.thrownInstantiationException || 
				MockDAOManager2.thrownInstantiationException) {
			throw new RuntimeException("Mocked instantiation error on purpose");
		}
		this.mockManager = mock(DAOManager.class);
	}

	@Override
	protected void closeDAOManager() {
		this.mockManager.closeDAOManager();
	}

	@Override
	protected void killDAOManager() {
		this.mockManager.killDAOManager();
	}

	@Override
	public void setParameters(Map<String, String> parameters)
			throws IllegalArgumentException {
		this.mockManager.setParameters(parameters);
	}

}
