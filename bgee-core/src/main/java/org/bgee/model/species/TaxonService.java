package org.bgee.model.species;


import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;

/**
 * A {@code Service} to obtain {@link Taxon} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TaxonService extends Service {

    /**
     * 0-arg constructor that will cause this {@code Service} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #Service(DAOManager)
     */
    public TaxonService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code Service} 
     *                      to obtain {@code DAO}s.
     */
    public TaxonService(DAOManager daoManager) {
        super(daoManager);
    }
}
