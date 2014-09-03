package org.bgee.pipeline;

import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneOntologyDAO;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO;
import org.bgee.model.dao.mysql.source.MySQLSourceDAO;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO;
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
	 * Default Constructor. 
	 */
	public TestAncestor()
	{
		
	}
	/**
	 * A {@code TestWatcher} to log starting, succeeded and failed tests. 
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
	 * @return 	A {@code Logger}
	 */
	protected abstract Logger getLogger();
	
	/**
	 * A mock {@code MySQLDAOManager} used for unit testing. Its attributes are 
	 * public mock objects that can be used to specify the behavior of this 
	 * {@code MockDAOManager}. 
	 */
	protected class MockDAOManager extends MySQLDAOManager {
	    public final MySQLDAOManager mockManager = mock(MySQLDAOManager.class);
	    public final BgeeConnection mockConnection = mock(BgeeConnection.class);
	    public final MySQLSourceDAO mockSourceDAO = mock(MySQLSourceDAO.class);
        public final MySQLSpeciesDAO mockSpeciesDAO = mock(MySQLSpeciesDAO.class);
        public final MySQLTaxonDAO mockTaxonDAO = mock(MySQLTaxonDAO.class);
        public final MySQLGeneOntologyDAO mockGeneOntologyDAO = mock(MySQLGeneOntologyDAO.class);
        public final MySQLGeneDAO mockGeneDAO = mock(MySQLGeneDAO.class);
        public final MySQLHierarchicalGroupDAO mockHierarchicalGroupDAO = 
        		mock(MySQLHierarchicalGroupDAO.class);
        public final MySQLStageDAO mockStageDAO = mock(MySQLStageDAO.class);
        public final MySQLRelationDAO mockRelationDAO = mock(MySQLRelationDAO.class);
        
        public MockDAOManager() {
            
        }

        @Override
        protected void closeDAOManager() throws DAOException {
            //nothing here
        }
        @Override
        protected void killDAOManager() throws DAOException {
            //nothing here
        }
        @Override
        protected void shutdown() throws DAOException {
            //nothing here
        }
        
        @Override
        public void setParameters(Properties props)
                throws IllegalArgumentException {
            this.mockManager.setParameters(props);
        }
        @Override
        public BgeeConnection getConnection() {
            return this.mockConnection;
        }

        @Override
        protected MySQLSourceDAO getNewSourceDAO() {
            return this.mockSourceDAO;
        }
        @Override
        protected MySQLSpeciesDAO getNewSpeciesDAO() {
            return this.mockSpeciesDAO;
        }
        @Override
        protected MySQLTaxonDAO getNewTaxonDAO() {
            return this.mockTaxonDAO;
        }
        @Override
        protected MySQLGeneOntologyDAO getNewGeneOntologyDAO() {
            return this.mockGeneOntologyDAO;
        }
        @Override
        protected MySQLGeneDAO getNewGeneDAO() {
            return this.mockGeneDAO;
        }
        @Override
        protected MySQLHierarchicalGroupDAO getNewHierarchicalGroupDAO() {
            return this.mockHierarchicalGroupDAO;
        }
        @Override
        protected MySQLStageDAO getNewStageDAO() {
            return this.mockStageDAO;
        }
        @Override
        protected MySQLRelationDAO getNewRelationDAO() {
            return this.mockRelationDAO;
        }
	}
}
