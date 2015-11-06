package org.bgee.model.gene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.species.Species;

/**
 * A {@link Service} to obtain {@link Gene} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code GeneService}s.
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 */
//TODO: unit tests, injecting a mock DAOManager, that will return mock DAOs, etc.
public class GeneService extends Service {
    
    private static final Logger log = LogManager.getLogger(GeneService.class.getName());
    
    /**
     * 0-arg constructor that will cause this {@code GeneService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #GeneService(DAOManager)
     */
    public GeneService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code GeneService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public GeneService(DAOManager daoManager) {
        super(daoManager);
    }
    
    /**
     * 
     * @param geneId
     * @return
     */
    public String retrieveSpeciesId(String geneId){
        return null;
    }

}
