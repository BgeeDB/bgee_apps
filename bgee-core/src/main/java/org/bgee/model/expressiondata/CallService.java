package org.bgee.model.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;

/**
 * A {@link Service} to obtain {@link Call} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code CallService}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class CallService extends Service {
    private final static Logger log = LogManager.getLogger(CallService.class.getName());
    
    //XXX: Enum class for fields of Call to populate? 
    //(e.g., GENE, ANAT_ENTITY, STAGE, DATA). But this means that we once again 
    //"duplicate" the concepts in the Condition class. 
    

    /**
     * 0-arg constructor that will cause this {@code CallService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #CallService(DAOManager)
     */
    public CallService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code CallService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public CallService(DAOManager daoManager) {
        super(daoManager);
    }
    
    
}
