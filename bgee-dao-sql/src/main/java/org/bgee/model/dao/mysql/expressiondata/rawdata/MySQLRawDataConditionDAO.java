package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public class MySQLRawDataConditionDAO extends MySQLDAO<RawDataConditionDAO.Attribute> implements RawDataConditionDAO {
    private final static Logger log = LogManager.getLogger(MySQLRawDataConditionDAO.class.getName());

    public MySQLRawDataConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIds(Collection<Integer> arg0,
            Collection<org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.Attribute> arg1)
            throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIdsAndConditionFilters(Collection<Integer> arg0,
            Collection<DAORawDataConditionFilter> arg1,
            Collection<org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.Attribute> arg2)
            throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }
}
