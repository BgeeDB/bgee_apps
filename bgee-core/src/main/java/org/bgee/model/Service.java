package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;

/**
 * Parent class of all services used in Bgee. 
 * Services are designed to provide objects from one specific class, retrieved using DAOs.
 * <p>
 * This parent class is responsible for obtaining 
 * a {@link org.bgee.model.dao.api.DAOManager DAOManager}, that will then be used 
 * by each factory to obtain appropriate {@code DAO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 01
 */
public abstract class Service {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(Service.class.getName());
    
	/**
	 * @see #getDAOManager()
	 */
	private final DAOManager daoManager;
	
	/**
	 * 0-arg constructor that will cause this {@code Service} to use 
	 * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
	 * 
	 * @see #Service(DAOManager)
	 */
    protected Service() {
    	this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code Service} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    protected Service(DAOManager daoManager) {
        if (daoManager == null) {
            throw log.throwing(new IllegalArgumentException("The provided DAOManager cannot be null"));
        }
        this.daoManager = daoManager;
    }
    
    /**
     * @return  the {@code DAOManager} that this {@code Service} must use to obtain {@code DAO}s.
     */
    protected DAOManager getDaoManager() {
        return daoManager;
    }
}
