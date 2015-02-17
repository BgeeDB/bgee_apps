package org.bgee.pipeline;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallParams;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLTaxonConstraintDAO;
import org.bgee.model.dao.mysql.connector.BgeeConnection;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.MySQLDiffExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.affymetrix.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultDAO;
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
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	 * Create a mock {@code DAOResultSet} {@code code V} containing the provided {@code List} of 
	 * {@code T} as results, used for unit testing.
	 * 
	 * @param resultSetContent A {@code List} of {@code T}s that is result of the 
	 *                         {@code DAOResultSet} {@code code V}. 
     * @param type             The desired returned type of values.
	 * @return                 A mock {@code DAOResultSet} {@code code V} containing containing 
	 *                         the provided {@code List} of {@code T} as results.
     * @param <T>              A {@code TransferObject} type parameter.
     * @param <V>              A {@code DAOResultSet} of {@code T}s type parameter.
	 */
	protected <T extends TransferObject, V extends DAOResultSet<T>> 
	                V createMockDAOResultSet(List<T> resultSetContent, Class<V> type) {
	    this.getLogger().entry(resultSetContent, type);
	    
        V mockResultSet = Mockito.mock(type);
        
        // Determine the behavior of consecutive calls to next().
        final int resultSetSize = resultSetContent.size();
        when(mockResultSet.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                getLogger().entry(invocationOnMock);
                // Return true while there is a result to return 
                return getLogger().exit(counter++ < resultSetSize);
            }
        });
  
        // Determine the behavior of consecutive calls to getTO().
        final List<T> listTO = new ArrayList<T>(resultSetContent);
        when(mockResultSet.getTO()).thenAnswer(new Answer<T>() {
            int counter = 0;
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                getLogger().entry(invocationOnMock);
                // Return true while there is a listTO to return 
                return getLogger().exit(listTO.get(counter++));
            }
        });
  
        // Determine the behavior of call to getAllTOs().
        when(mockResultSet.getAllTOs()).thenCallRealMethod();
        
        // Determine the behavior of call to close().
        doNothing().when(mockResultSet).close();
        
        return this.getLogger().exit(mockResultSet);
	}
	
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
        public final MySQLTaxonConstraintDAO mockTaxonConstraintDAO = 
                mock(MySQLTaxonConstraintDAO.class);
        public final MySQLGeneOntologyDAO mockGeneOntologyDAO = mock(MySQLGeneOntologyDAO.class);
        public final MySQLGeneDAO mockGeneDAO = mock(MySQLGeneDAO.class);
        public final MySQLHierarchicalGroupDAO mockHierarchicalGroupDAO = 
        		mock(MySQLHierarchicalGroupDAO.class);
        public final MySQLStageDAO mockStageDAO = mock(MySQLStageDAO.class);
        public final MySQLRelationDAO mockRelationDAO = mock(MySQLRelationDAO.class);
        public final MySQLExpressionCallDAO mockExpressionCallDAO = 
                mock(MySQLExpressionCallDAO.class);
        public final MySQLNoExpressionCallDAO mockNoExpressionCallDAO = 
                mock(MySQLNoExpressionCallDAO.class);
        public final MySQLDiffExpressionCallDAO mockDiffExpressionCallDAO = 
                mock(MySQLDiffExpressionCallDAO.class);
        public final MySQLAnatEntityDAO mockAnatEntityDAO = mock(MySQLAnatEntityDAO.class);
        public final MySQLAffymetrixProbesetDAO mockAffymetrixProbesetDAO = 
                mock(MySQLAffymetrixProbesetDAO.class);
        public final MySQLInSituSpotDAO mockInSituSpotDAO = mock(MySQLInSituSpotDAO.class);
        public final MySQLRNASeqResultDAO mockRNASeqResultDAO = mock(MySQLRNASeqResultDAO.class);
        
        public MockDAOManager() {
            
        }


        @Override
        public void releaseResources() throws DAOException {
            this.mockManager.releaseResources();
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
        protected MySQLTaxonConstraintDAO getNewTaxonConstraintDAO() {
            return this.mockTaxonConstraintDAO;
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
        @Override
        protected MySQLExpressionCallDAO getNewExpressionCallDAO() {
            return this.mockExpressionCallDAO;
        }
        @Override
        protected MySQLNoExpressionCallDAO getNewNoExpressionCallDAO() {
            return this.mockNoExpressionCallDAO;
        }
        @Override
        protected MySQLDiffExpressionCallDAO getNewDiffExpressionCallDAO() {
            return this.mockDiffExpressionCallDAO;
        }
        @Override
        protected MySQLAnatEntityDAO getNewAnatEntityDAO() {
            return this.mockAnatEntityDAO;
        }
        @Override
        protected MySQLAffymetrixProbesetDAO getNewAffymetrixProbesetDAO() {
            return this.mockAffymetrixProbesetDAO;
        }
        @Override
        protected MySQLInSituSpotDAO getNewInSituSpotDAO() {
            return this.mockInSituSpotDAO;
        }
        @Override
        protected MySQLRNASeqResultDAO getNewRNASeqResultDAO() {
            return this.mockRNASeqResultDAO;
        }
	}


    /**
     * Custom matcher for verifying IDs of species allowing to filter 
     * the calls to use of actual and expected {@code CallParams}.
     */
	//TODO: we use this design because CallParams is not yet stabilized, and we need 
	//to think about the implementation of its equals/hashCode methods.
	//To remove when it will be implemented. 
    private static class CallParamsMatcher extends ArgumentMatcher<CallParams> {
        
        private final CallParams expected;
        
        public CallParamsMatcher(CallParams expected) {
            this.expected = expected;
        }
        
        @Override
        public boolean matches(Object actual) {
            if (actual == null && expected == null)
                return true;
            if (actual == null && expected != null)
                return false;
            if (!actual.getClass().equals(expected.getClass()))
                return false;
            
            if (!((CallParams) actual).getSpeciesIds().equals(expected.getSpeciesIds())) {
                return false;
            }
            if (actual instanceof ExpressionCallParams && 
                    (((ExpressionCallParams) actual).isIncludeSubstructures() != 
                        ((ExpressionCallParams) expected).isIncludeSubstructures() || 
                    ((ExpressionCallParams) actual).isIncludeSubStages() != 
                        ((ExpressionCallParams) expected).isIncludeSubStages())) {
                return false;
            }
            if (actual instanceof DiffExpressionCallParams) {
                DiffExpressionCallParams tmpActual = (DiffExpressionCallParams) actual;
                DiffExpressionCallParams tmpExpected = (DiffExpressionCallParams) expected;
                if (tmpActual.getComparisonFactor() != tmpExpected.getComparisonFactor() ||
                    !tmpActual.getAffymetrixDiffExprCallTypes().equals(
                            tmpExpected.getAffymetrixDiffExprCallTypes()) ||
                    tmpActual.isIncludeAffymetrixTypes() != tmpExpected.isIncludeAffymetrixTypes() ||
                    !tmpActual.getRNASeqDiffExprCallTypes().equals(
                            tmpExpected.getRNASeqDiffExprCallTypes()) ||
                    tmpActual.isIncludeRNASeqTypes() != tmpExpected.isIncludeRNASeqTypes() ||
                    tmpActual.isSatisfyAllCallTypeCondition() != 
                            tmpExpected.isSatisfyAllCallTypeCondition()){
                    return false;
                }
            }
            if (actual instanceof NoExpressionCallParams && 
                    ((NoExpressionCallParams) actual).isIncludeParentStructures() != 
                    ((NoExpressionCallParams) expected).isIncludeParentStructures()) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Convenience factory method for using the custom {@code CallParams} matcher.
     * 
     *  @param expected  A {@code CallParams} that is the argument to be verified.
     */
    protected static CallParams valueCallParamEq(CallParams params) {
        return argThat(new CallParamsMatcher(params));
    }
}
