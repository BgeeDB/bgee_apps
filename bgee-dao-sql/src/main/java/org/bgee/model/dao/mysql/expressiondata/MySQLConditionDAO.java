package org.bgee.model.dao.mysql.expressiondata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * An {@code ConditionDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @see org.bgee.model.dao.api.anatdev.ConditionDAO.ConditionTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLConditionDAO extends MySQLDAO<ConditionDAO.Attribute> implements ConditionDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLConditionDAO.class.getName());

    
    public MySQLConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ConditionTOResultSet getConditionsBySpeciesIds(Collection<String> speciesIds,
        Collection<ConditionDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, attributes);
        throw log.throwing(new UnsupportedOperationException("Retrieve of conditions not implemented yet"));
    }

}
