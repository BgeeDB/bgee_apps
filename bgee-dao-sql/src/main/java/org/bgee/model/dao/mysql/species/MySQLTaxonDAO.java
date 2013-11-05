package org.bgee.model.dao.mysql.species;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonTO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code TaxonDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.TaxonTO
 * @since Bgee 13
 */
public class MySQLTaxonDAO extends MySQLDAO<TaxonDAO.Attribute> 
    implements TaxonDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLTaxonDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLTaxonDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    
    
    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    public void insertTaxa(Collection<TaxonTO> taxa) {
        log.entry(taxa);
        
        log.exit();
    }
}
