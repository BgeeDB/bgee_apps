package org.bgee.model.species;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;

/**
 * A {@link Service} to obtain {@link Taxon} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code TaxonService}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 */
public class TaxonService extends Service {

    private static final Logger log = LogManager.getLogger(TaxonService.class.getName());

    /**
     * 0-arg constructor that will cause this {@code TaxonService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #Service(DAOManager)
     */
    public TaxonService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code TaxonService} 
     *                      to obtain {@code DAO}s.
     */
    public TaxonService(DAOManager daoManager) {
        super(daoManager);
    }
}
