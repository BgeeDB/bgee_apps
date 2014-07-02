package org.bgee.model.dao.mysql.source;

import java.util.Collection;
import java.util.List;

import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public class MySQLSourceDAO extends MySQLDAO<SourceDAO.Attribute> implements SourceDAO {

    public MySQLSourceDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public Collection<SourceTO> getAllDataSources() throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TransferObject> getDisplayableDataSources() throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceTO getDataSourceById(String dataSourceId) throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

}
