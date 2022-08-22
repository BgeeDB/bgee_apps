package org.bgee.pipeline;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO;
import org.bgee.model.dao.mysql.anatdev.MySQLTaxonConstraintDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLRawSimilarityAnnotationDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLStageGroupingDAO;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLSummarySimilarityAnnotationDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.MySQLConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.MySQLDiffExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.MySQLExperimentExpressionDAO;
import org.bgee.model.dao.mysql.expressiondata.MySQLRawExpressionCallDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.file.MySQLDownloadFileDAO;
import org.bgee.model.dao.mysql.file.MySQLSpeciesDataGroupDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneOntologyDAO;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO;
import org.bgee.model.dao.mysql.keyword.MySQLKeywordDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLCIOStatementDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLEvidenceOntologyDAO;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO;

/**
 * Parent class of all classes needing to use the MySQL Bgee database. 
 * It notably allows to acquire {@code MySQLDAO}s. A {@code DAOManager} will be 
 * acquired, but {@code DAO}s returned by it will be casted into {@code MySQLDAO}s. 
 * It is the responsibility of the pipeline to provide the proper parameters so 
 * that the {@code bgee-dao-api} module actually uses a {@code MySQLDAOManager}. 
 * This is done through System properties, or property file in classpath (see 
 * {@link org.bgee.model.dao.api.DAOManager} documentation for details about 
 * how to provide parameters through System properties or property file; see 
 * {@link org.bgee.model.dao.mysql.connector.MySQLDAOManager} static attributes 
 * for details about accepted parameters).
 * <p>
 * As of Bgee 13, the data store used by Bgee is a MySQL database. The classes 
 * of the pipeline will directly use {@code MySQLDAO}s from the module 
 * {@code bgee-dao-sql}, rather than the API provided by the module {@code bgee-dao-api}, 
 * as the Bgee application normally should. This is because they will use methods 
 * that should not be exposed by the API, such as insertion or update methods. 
 * <p>
 * The disadvantage is that if the data store used by Bgee was changed, then a lot 
 * of the classes extending this class should be modified. The advantages are that 
 * we will not exposed unnecessary methods to users of the API, and we will not have 
 * to develop another DAO service provider mechanism for the pipeline. This approach 
 * was chosen because we do not expect the Bgee pipeline to be able to use different 
 * data stores (while the Bgee application should be able to use different DAOs, 
 * with no code modifications, for instance MySQL DAOs vs. webservice DAOs).
 * As long as the new DAOs have the same interface as the MySQL DAOs, modifications 
 * should be minor. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class MySQLDAOUser {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLDAOUser.class.getName());
    
    /**
     * The {@code MySQLDAOManager} used to acquire {@code MySQLDAO}s 
     * ({@code DAO}s from the {@code bgee-dao-sql} module).
     */
    private final MySQLDAOManager manager;
    
    /**
     * Default constructor, acquiring a {@code MySQLDAOManager}. If parameters 
     * providing through System properties or property file do not allow to acquire 
     * a {@code MySQLDAOManager} from the {@code org.bgee.model.dao.api.DAOManager}, 
     * a {@code ClassCastException} is thrown. 
     * 
     * @throws IllegalStateException    if the properties provided do not allow the 
     *                                  {@code DAOManager} to return a valid 
     *                                  {@code MySQLDAOManager}.
     *                              
     */
    public MySQLDAOUser() throws IllegalStateException {
        this(null);
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} used by this object. 
     * This is useful for unit testing, where you can provide a mock 
     * {@code MySQLDAOManager}. Subclasses willing to provide this feature needs 
     * to override this constructor to make it public.
     * 
     * @param providedManager   The {@code MySQLDAOManager} that will be used by this object.
     */
    protected MySQLDAOUser(MySQLDAOManager providedManager) {
        if (providedManager == null) {
            String exceptMsg = "The properties provided " +
                    "either through System properties, or through a property file, " +
                    "did not allow to obtain a valid MySQLDAOManager";
            try {
                MySQLDAOManager manager = (MySQLDAOManager) DAOManager.getDAOManager();
                if (manager == null) {
                    throw log.throwing(new IllegalStateException(exceptMsg));
                }
                this.manager = manager;
            } catch (Throwable e) {
                throw log.throwing(new IllegalStateException(exceptMsg, e));
            }
        } else {
            this.manager = providedManager;
        }
    }

    /**
     * @return  A {@code MySQLKeywordDAO}.
     */
    protected MySQLKeywordDAO getKeywordDAO() {
        return (MySQLKeywordDAO) this.manager.getKeywordDAO();
    }
    /**
     * @return  A {@code MySQLSpeciesDAO}.
     */
    protected MySQLSpeciesDAO getSpeciesDAO() {
        return (MySQLSpeciesDAO) this.manager.getSpeciesDAO();
    }
    /**
     * @return  A {@code MySQLTaxonDAO}.
     */
    protected MySQLTaxonDAO getTaxonDAO() {
        return (MySQLTaxonDAO) this.manager.getTaxonDAO();
    }
    /**
     * @return  A {@code MySQLTaxonConstraintDAO}.
     */
    protected MySQLTaxonConstraintDAO getTaxonConstraintDAO() {
        return (MySQLTaxonConstraintDAO) this.manager.getTaxonConstraintDAO();
    }
    /**
     * @return  A {@code MySQLGeneOntologyDAO}.
     */
    protected MySQLGeneOntologyDAO getGeneOntologyDAO() {
        return (MySQLGeneOntologyDAO) this.manager.getGeneOntologyDAO();
    }
    /**
     * @return  A {@code MySQLGeneDAO}.
     */
    protected MySQLGeneDAO getGeneDAO() {
        return (MySQLGeneDAO) this.manager.getGeneDAO();
    }
    /**
     * @return  A {@code MySQLHierarchicalGroupDAO}.
     */
    protected MySQLHierarchicalGroupDAO getHierarchicalGroupDAO() {
        return (MySQLHierarchicalGroupDAO) this.manager.getHierarchicalGroupDAO();
    }
    /**
     * @return  A {@code MySQLStageDAO}.
     */
    protected MySQLStageDAO getStageDAO() {
        return (MySQLStageDAO) this.manager.getStageDAO();
    }
    /**
     * @return  A {@code MySQLRelationDAO}.
     */
    protected MySQLRelationDAO getRelationDAO() {
        return (MySQLRelationDAO) this.manager.getRelationDAO();
    }
    /**
     * @return  A {@code MySQLConditionDAO}.
     */
    protected MySQLConditionDAO getConditionDAO() {
        return (MySQLConditionDAO) this.manager.getConditionDAO();
    }
    /**
     * @return  A {@code MySQLDiffExpressionCallDAO}.
     */
    protected MySQLDiffExpressionCallDAO getDiffExpressionCallDAO() {
        return (MySQLDiffExpressionCallDAO) this.manager.getDiffExpressionCallDAO();
    }
    /**
     * @return  A {@code MySQLNoExpressionCallDAO}.
     */
    protected MySQLRawExpressionCallDAO getNewRawExpressionCallDAO() {
        return (MySQLRawExpressionCallDAO) this.manager.getRawExpressionCallDAO();
    }
    /**
     * @return  A {@code MySQLExperimentExpressionDAO}.
     */
    protected MySQLExperimentExpressionDAO getExperimentExpressionDAO() {
        return (MySQLExperimentExpressionDAO) this.manager.getExperimentExpressionDAO();
    }
    /**
     * @return  A {@code MySQLAnatEntityDAO}.
     */
    protected MySQLAnatEntityDAO getAnatEntityDAO() {
        return (MySQLAnatEntityDAO) this.manager.getAnatEntityDAO();
    }
    /**
     * @return  A {@code MySQLSpeciesDataGroupDAO}.
     */
    protected MySQLSpeciesDataGroupDAO getSpeciesDataGroupDAO() {
        return (MySQLSpeciesDataGroupDAO) this.manager.getSpeciesDataGroupDAO();
    }
    /**
     * @return  A {@code MySQLDownloadFileDAO}.
     */
    protected MySQLDownloadFileDAO getDownloadFileDAO() {
        return (MySQLDownloadFileDAO) this.manager.getDownloadFileDAO();
    }
    /**
     * @return  A {@code MySQLAffymetrixProbesetDAO}.
     */
    protected MySQLAffymetrixProbesetDAO getAffymetrixProbesetDAO() {
        return (MySQLAffymetrixProbesetDAO) this.manager.getAffymetrixProbesetDAO();
    }
    /**
     * @return  A {@code MySQLInSituSpotDAO}.
     */
    protected MySQLInSituSpotDAO getInSituSpotDAO() {
        return (MySQLInSituSpotDAO) this.manager.getInSituSpotDAO();
    }
    /**
     * @return  A {@code MySQLRNASeqResultDAO}.
     */
    protected MySQLRNASeqResultAnnotatedSampleDAO getRNASeqResultAnnotatedSampleDAO() {
        return (MySQLRNASeqResultAnnotatedSampleDAO) this.manager
                .getRNASeqResultAnnotatedSampleDAO();
    }
    /**
     * @return  A {@code MySQLCIOStatementDAO}.
     */
    protected MySQLCIOStatementDAO getCIOStatementDAO() {
        return (MySQLCIOStatementDAO) this.manager.getCIOStatementDAO();
    }
    /**
     * @return  A {@code MySQLEvidenceOntologyDAO}.
     */
    protected MySQLEvidenceOntologyDAO getEvidenceOntologyDAO() {
        return (MySQLEvidenceOntologyDAO) this.manager.getEvidenceOntologyDAO();
    }
    /**
     * @return  A {@code SummarySimilarityAnnotationDAO}.
     */
    protected MySQLSummarySimilarityAnnotationDAO getSummarySimilarityAnnotationDAO() {
        return (MySQLSummarySimilarityAnnotationDAO) 
                this.manager.getSummarySimilarityAnnotationDAO();
    }
    /**
     * @return  A {@code RawSimilarityAnnotationDAO}.
     */
    protected MySQLRawSimilarityAnnotationDAO getRawSimilarityAnnotationDAO() {
        return (MySQLRawSimilarityAnnotationDAO) 
                this.manager.getRawSimilarityAnnotationDAO();
    }
    /**
     * @return  A {@code SummarySimilarityAnnotationDAO}.
     */
    protected MySQLStageGroupingDAO getStageGroupingDAO() {
        return (MySQLStageGroupingDAO) 
                this.manager.getStageGroupingDAO();
    }
    /**
     * Start a transaction with the MySQL database. We wrap the potential 
     * {@code SQLException} into a {@code DAOException}, because we do not want 
     * the whole pipeline to be that dependent to the use of JDBC. 
     * <p>
     * To commit the transaction, call {@code #commit()}. There is no {@code rollback} 
     * method available, because closing the {@code manager} will rollback any 
     * ongoing transaction. So you just need to make sure to call {@link #closeDAO()} 
     * in a {@code finally} block.
     * 
     * @throws IllegalStateException    If a transaction was already ongoing.
     * @throws DAOException             If an error occurred while starting 
     *                                  the transaction.
     * @see #commit()
     * @see #closeDAO()
     */
    protected void startTransaction() throws IllegalStateException, DAOException {
        log.traceEntry();
        try {
            this.manager.getConnection().startTransaction();
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        log.traceExit();
    }
    
    /**
     * Commit an ongoing transaction with the MySQL database. We wrap the potential 
     * {@code SQLException} into a {@code DAOException}, because we do not want 
     * the whole pipeline to be that dependent to the use of JDBC. 
     * <p>
     * If {@link #startTransaction()} was not called prior to calling this method, 
     * an {@code IllegalStateException} is thrown.
     * 
     * @throws IllegalStateException    If no transaction was ongoing.
     * @throws DAOException             If an error occurred while starting 
     *                                  the transaction.
     * @see #startTransaction()
     */
    protected void commit() throws IllegalStateException, DAOException {
        log.traceEntry();
        try {
            this.manager.getConnection().commit();
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        log.traceExit();
    }
    
    /**
     * Closes the {@code MySQLDAOManager} used by this {@code MySQLDAOUser}. 
     * This will also rollback any ongoing transaction.
     */
    protected void closeDAO() {
        log.traceEntry();
        this.manager.close();
        log.traceExit();
    }
    
    public MySQLDAOManager getManager() {
        return this.manager;
    }
}
