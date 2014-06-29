package org.bgee.model.dao.mysql.source;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO;

public class MySQLSourceDAO extends MySQLDAO<SourceDAO.Attribute> implements SourceDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLTaxonDAO.class.getName());
    
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

    @Override
    public String getLabel(SourceDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(SourceDAO.Attribute.ID)) {
            label = "dataSourceId";
        } else if (attribute.equals(SourceDAO.Attribute.NAME)) {
            label = "dataSourceName";
        } 
        
        return log.exit(label);
    }
    
    @Override
    public String getSQLExpr(SourceDAO.Attribute attribute) {
        log.entry(attribute);
        //no complex SQL expression in this DAO, we just build table_name.label
        return log.exit(MySQLDAO.SOURCE_TABLE_NAME + "." + this.getLabel(attribute));
    }
    
}
