package org.bgee.model.dao.mysql;

import java.util.Map;

import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO;

public class MySQLDAOManager extends DAOManager {
    
    /**
     * Default constructor. {@code DAOManager}s must provide a no-arguments public 
     * constructor, to be used as a {@code Service Provider}.
     */
    public MySQLDAOManager() {
        super();
    }

    @Override
    public void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void closeDAOManager() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void killDAOManager() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected SourceDAO getNewSourceDAO() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
