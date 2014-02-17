package org.bgee.model.dao.api;

import java.util.Properties;

import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneOntologyDAO;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.TaxonDAO;

import static org.mockito.Mockito.mock;

/**
 * A class to simulate an implementation of {@link DAOManager}, that is discovered 
 * by the {@code Service Loader} thanks to the test file 
 * {@code META-INF/services/org.bgee.model.dao.api.DAOManager}. 
 * <p>
 * All methods delegate to the mocked {@code DAOManager} stored 
 * in the public attribute {@link mockManager}, which should thus be used to specify 
 * the expected behaviors to mock. 
 * <p>
 * This class is the same than {@link MockDAOManager}, but is used to test the behavior 
 * when the {@code ServiceLoader} discovers several service providers. 
 * This provider should be the second one loaded by the {@code ServiceLoader}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MockDAOManager2 extends DAOManager {
	
	/**
	 * If {@code true}, an {@code Error} is thrown when the default constructor 
	 * is called. 
	 */
	protected static boolean thrownInstantiationException = false;
	
	/**
	 * This static mocked {@code DAOManager} is needed because we sometimes 
	 * need to specify mocked behavior before acquiring a instance 
	 * (notably to test {@link DAOManager#gtDAOManager()}).
	 */
	public static final DAOManager mockManager = mock(DAOManager.class);
	/**
	 * This mocked {@code DAOManager} is needed because we sometimes
	 * need to specify or verify different behavior from different instances. 
	 */
	public final DAOManager instanceMockManager = mock(DAOManager.class);
	
	/**
	 * Default constructor used by the service loader.
	 */
	public MockDAOManager2() {
		if (TestAncestor.thrownInstantiationException || 
				MockDAOManager2.thrownInstantiationException) {
			throw new RuntimeException("Mocked instantiation error on purpose");
		}
	}

	@Override
	protected void closeDAOManager() throws DAOException {
		this.instanceMockManager.closeDAOManager();
	}

	@Override
	protected void killDAOManager() throws DAOException {
		this.instanceMockManager.killDAOManager();
	}

    @Override
    protected void shutdown() throws DAOException {
        this.instanceMockManager.shutdown();
    }

	@Override
	public void setParameters(Properties props)
			throws IllegalArgumentException {
		MockDAOManager2.mockManager.setParameters(props);
	}

	@Override
	protected SourceDAO getNewSourceDAO() {
		return this.instanceMockManager.getNewSourceDAO();
	}
    @Override
    protected SpeciesDAO getNewSpeciesDAO() {
        return this.instanceMockManager.getNewSpeciesDAO();
    }
    @Override
    protected TaxonDAO getNewTaxonDAO() {
        return this.instanceMockManager.getNewTaxonDAO();
    }
    @Override
    protected GeneOntologyDAO getNewGeneOntologyDAO() {
        return this.instanceMockManager.getNewGeneOntologyDAO();
    }

}
