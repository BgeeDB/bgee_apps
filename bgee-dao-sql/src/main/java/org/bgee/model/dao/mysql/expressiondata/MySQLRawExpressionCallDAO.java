package org.bgee.model.dao.mysql.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code RawExpressionCallDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @see     org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLRawExpressionCallDAO  extends MySQLDAO<RawExpressionCallDAO.Attribute> 
                                        implements RawExpressionCallDAO {

    public MySQLRawExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
        // TODO Auto-generated constructor stub
    }

    private final static Logger log = 
        LogManager.getLogger(MySQLRawExpressionCallDAO.class.getName());

    @Override
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(String speciesId) throws DAOException {
        log.entry(speciesId);
        throw log.throwing(new UnsupportedOperationException("Retrieve od expression calls not implemented yet"));
    }

}
