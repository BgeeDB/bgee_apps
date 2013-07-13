package org.bgee.model.dao.api;

import java.util.Map;

import org.bgee.model.dao.api.source.SourceDAO;

import static org.mockito.Mockito.mock;

/**
 * A class to simulate an implementation of {@link DAOManager}, that is discovered 
 * by the <code>Service Loader</code> thanks to the test file 
 * <code>META-INF/services/org.bgee.model.dao.api.DAOManager</code>. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MockDAOManager extends DAOManager {
	
	/**
	 * If <code>true</code>, an <code>Error</code> is thrown when the default constructor 
	 * is called. 
	 */
	protected static boolean thrownInstantiationException = false;
	/**
	 * This static mocked <code>DAOManager</code> is needed because we sometimes 
	 * need to specify mocked behavior before acquiring a instance 
	 * (notably to test {@link DAOManager#gtDAOManager()}).
	 */
	public static final DAOManager mockManager = mock(DAOManager.class);
	/**
	 * This mocked <code>DAOManager</code> is needed because we sometimes
	 * need to specify or verify different behavior from different instances. 
	 */
	public final DAOManager instanceMockManager = mock(DAOManager.class);
	
	/**
	 * Default constructor used by the service loader.
	 */
	public MockDAOManager() {
		if (TestAncestor.thrownInstantiationException || 
				MockDAOManager.thrownInstantiationException) {
			throw new RuntimeException("Mocked instantiation error on purpose");
		}
	}

	@Override
	protected void closeDAOManager() {
		this.instanceMockManager.closeDAOManager();
	}

	@Override
	protected void killDAOManager() {
		this.instanceMockManager.killDAOManager();
	}

	@Override
	public void setParameters(Map<String, String> parameters)
			throws IllegalArgumentException {
		MockDAOManager.mockManager.setParameters(parameters);
	}

	@Override
	protected SourceDAO getNewSourceDAO() {
		return this.instanceMockManager.getNewSourceDAO();
	}

}
