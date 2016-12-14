package org.bgee.model.dao.mysql.expressiondata;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

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
    public ExperimentExpressionTOResultSet getAffymetrixExperimentExpressions(
        Collection<ExperimentExpressionDAO.Attribute> attributes, 
        LinkedHashMap<ExperimentExpressionDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
            throws DAOException {
        log.entry(attributes, orderingAttributes);
        return log.exit(this.getExperimentExpressions(attributes, orderingAttributes));
    }

    @Override
    public ExperimentExpressionTOResultSet getESTExperimentExpressions(
        Collection<ExperimentExpressionDAO.Attribute> attributes, 
        LinkedHashMap<ExperimentExpressionDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
            throws DAOException {
        log.entry(attributes, orderingAttributes);
        return log.exit(this.getExperimentExpressions(attributes, orderingAttributes));
    }

    @Override
    public ExperimentExpressionTOResultSet getInSituExperimentExpressions(
        Collection<ExperimentExpressionDAO.Attribute> attributes, 
        LinkedHashMap<ExperimentExpressionDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
            throws DAOException {
        log.entry(attributes, orderingAttributes);
        return log.exit(this.getExperimentExpressions(attributes, orderingAttributes));
    }

    @Override
    public ExperimentExpressionTOResultSet getRNASeqExperimentExpressions(
        Collection<ExperimentExpressionDAO.Attribute> attributes, 
        LinkedHashMap<ExperimentExpressionDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
            throws DAOException {
        log.entry(attributes, orderingAttributes);
        return log.exit(this.getExperimentExpressions(attributes, orderingAttributes));
    }
    
    private ExperimentExpressionTOResultSet getExperimentExpressions(
        Collection<ExperimentExpressionDAO.Attribute> attributes, 
        LinkedHashMap<ExperimentExpressionDAO.OrderingAttribute, DAO.Direction> orderingAttributes) 
            throws DAOException{
        log.entry(attributes, orderingAttributes);
        throw log.throwing(new UnsupportedOperationException(
            "Retrieval of experiment expressions not yet implemented."));
    }
}
