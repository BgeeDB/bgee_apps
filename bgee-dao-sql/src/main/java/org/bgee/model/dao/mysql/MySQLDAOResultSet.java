package org.bgee.model.dao.mysql;

import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;

public abstract class MySQLDAOResultSet implements DAOResultSet {

    @Override
    public boolean next() throws DAOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() throws DAOException {
        // TODO Auto-generated method stub
        
    }
    
}
