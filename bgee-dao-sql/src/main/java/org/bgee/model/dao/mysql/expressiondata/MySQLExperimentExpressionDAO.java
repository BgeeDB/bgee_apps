package org.bgee.model.dao.mysql.expressiondata;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code ExperimentExpressionDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @see org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO
 * @since   Bgee 14, Dec. 2016
 */
public class MySQLExperimentExpressionDAO extends MySQLDAO<ExperimentExpressionDAO.Attribute> 
                                            implements ExperimentExpressionDAO  {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLExperimentExpressionDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLExperimentExpressionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ExperimentExpressionTOResultSet getAffymetrixExpExprsOrderedByGeneIdAndExprId(int speciesId,
        Collection<ConditionDAO.Attribute> condParam) throws DAOException {
        log.entry(speciesId, condParam);
        return log.exit(getExperimentExpressions(speciesId, condParam));
    }

    @Override
    public ExperimentExpressionTOResultSet getESTExpExprsOrderedByGeneIdAndExprId(int speciesId,
        Collection<ConditionDAO.Attribute> condParam) throws DAOException {
        log.entry(speciesId, condParam);
        return log.exit(getExperimentExpressions(speciesId, condParam));
    }

    @Override
    public ExperimentExpressionTOResultSet getInSituExpExprsOrderedByGeneIdAndExprId(int speciesId,
        Collection<ConditionDAO.Attribute> condParam) throws DAOException {
        log.entry(speciesId, condParam);
        return log.exit(getExperimentExpressions(speciesId, condParam));
    }

    @Override
    public ExperimentExpressionTOResultSet getRNASeqExpExprsOrderedByGeneIdAndExprId(int speciesId,
        Collection<ConditionDAO.Attribute> condParam) throws DAOException {
        log.entry(speciesId, condParam);
        return log.exit(getExperimentExpressions(speciesId, condParam));
    }
    
    private ExperimentExpressionTOResultSet getExperimentExpressions(int speciesId,
        Collection<ConditionDAO.Attribute> condParam) 
            throws DAOException{
        log.entry(speciesId, condParam);
        throw log.throwing(new UnsupportedOperationException(
            "Retrieval of experiment expressions not yet implemented."));
    }
}
