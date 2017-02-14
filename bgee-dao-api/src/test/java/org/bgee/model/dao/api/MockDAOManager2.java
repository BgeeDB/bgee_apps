package org.bgee.model.dao.api;

import java.util.Properties;

import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.affymetrix.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.TaxonDAO;

import static org.mockito.Mockito.mock;

/**
 * A class to simulate an implementation of {@link DAOManager}, that is discovered 
 * by the {@code Service Loader} thanks to the test file 
 * {@code META-INF/services/org.bgee.model.dao.api.DAOManager}. 
 * <p>
 * All methods delegate to the mocked {@code DAOManager} stored 
 * in the public attribute {@code mockManager}, which should thus be used to specify
 * the expected behaviors to mock. 
 * <p>
 * This class is the same than {@link MockDAOManager}, but is used to test the behavior 
 * when the {@code ServiceLoader} discovers several service providers. 
 * This provider should be the second one loaded by the {@code ServiceLoader}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, July 2013
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
	 * (notably to test {@link DAOManager#getDAOManager()}).
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
    protected TaxonConstraintDAO getNewTaxonConstraintDAO() {
        return this.instanceMockManager.getNewTaxonConstraintDAO();
    }
    @Override
    protected GeneOntologyDAO getNewGeneOntologyDAO() {
        return this.instanceMockManager.getNewGeneOntologyDAO();
    }
    @Override
    protected GeneDAO getNewGeneDAO() {
        return this.instanceMockManager.getNewGeneDAO();
    }
    @Override
    protected HierarchicalGroupDAO getNewHierarchicalGroupDAO() {
        return this.instanceMockManager.getNewHierarchicalGroupDAO();
    }
    @Override
    protected StageDAO getNewStageDAO() {
        return this.instanceMockManager.getNewStageDAO();
    }
    @Override
    protected RelationDAO getNewRelationDAO() {
        return this.instanceMockManager.getNewRelationDAO();
    }
    @Override
    protected ConditionDAO getNewConditionDAO() {
        return this.instanceMockManager.getNewConditionDAO();
    }
    @Override
    protected ExpressionCallDAO getNewExpressionCallDAO() {
        return this.instanceMockManager.getNewExpressionCallDAO();
    }
    @Override
    protected NoExpressionCallDAO getNewNoExpressionCallDAO() {
        return this.instanceMockManager.getNewNoExpressionCallDAO();
    }
    @Override
    protected RawExpressionCallDAO getNewRawExpressionCallDAO() {
        return this.instanceMockManager.getNewRawExpressionCallDAO();
    }
    @Override
    protected ExperimentExpressionDAO getNewExperimentExpressionDAO() {
        return this.instanceMockManager.getNewExperimentExpressionDAO();
    }
    @Override
    protected DiffExpressionCallDAO getNewDiffExpressionCallDAO() {
        return this.instanceMockManager.getNewDiffExpressionCallDAO();
    }
    @Override
    protected AnatEntityDAO getNewAnatEntityDAO() {
        return this.instanceMockManager.getNewAnatEntityDAO();
    }
    @Override
    protected AffymetrixProbesetDAO getNewAffymetrixProbesetDAO() {
        return this.instanceMockManager.getNewAffymetrixProbesetDAO();
    }
    @Override
    protected InSituSpotDAO getNewInSituSpotDAO() {
        return this.instanceMockManager.getNewInSituSpotDAO();
    }
    @Override
    protected RNASeqResultDAO getNewRNASeqResultDAO() {
        return this.instanceMockManager.getNewRNASeqResultDAO();
    }
    @Override
    protected CIOStatementDAO getNewCIOStatementDAO() {
        return this.instanceMockManager.getNewCIOStatementDAO();
    }
    @Override
    protected EvidenceOntologyDAO getNewEvidenceOntologyDAO() {
        return this.instanceMockManager.getNewEvidenceOntologyDAO();
    }
    @Override
    protected SummarySimilarityAnnotationDAO getNewSummarySimilarityAnnotationDAO() {
        return this.instanceMockManager.getNewSummarySimilarityAnnotationDAO();
    }
    @Override
    protected RawSimilarityAnnotationDAO getNewRawSimilarityAnnotationDAO() {
        return this.instanceMockManager.getNewRawSimilarityAnnotationDAO();
    }
    @Override
    protected StageGroupingDAO getNewStageGroupingDAO() {
        return this.instanceMockManager.getNewStageGroupingDAO();
    }

    @Override
    protected DownloadFileDAO getNewDownloadFileDAO() {
        return instanceMockManager.getNewDownloadFileDAO();
    }

    @Override
    protected SpeciesDataGroupDAO getNewSpeciesDataGroupDAO() {
        return instanceMockManager.getNewSpeciesDataGroupDAO();
    }

    @Override
    public void releaseResources() {
        this.instanceMockManager.releaseResources();
    }

	@Override
	protected KeywordDAO getNewKeywordDAO() {
		return instanceMockManager.getNewKeywordDAO();
	}

	@Override
	protected GeneNameSynonymDAO getNewGeneNameSynonymDAO() {
		return instanceMockManager.getNewGeneNameSynonymDAO();
	}

    @Override
    protected SourceToSpeciesDAO getNewSourceToSpeciesDAO() {
        return instanceMockManager.getNewSourceToSpeciesDAO();
    }
}
