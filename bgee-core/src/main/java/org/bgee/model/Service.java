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
     * Interface implemented by {@code Enum} classes allowing to select 
     * what are the fields to populate in objects returned by {@code Service}s.
     * Not all {@code Service}s define such an implementation.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     */
    public static interface Attribute {
        //nothing here, it is only used for typing the Enum classes.
    }
    /**
     * Interface implemented by {@code Enum} classes allowing to select 
     * the fields used to order the results of a query to a {@code Service}. 
     * This is a separate interface from {@link DAO.Attribute}, because 
     * the attributes to retrieve from a query, and the attributes to use to order 
     * the results of a query, are often different, and some attributes used for ordering 
     * can even be absent from the set of attributes retrievable from a query.
     * <p>
     * Note that {@code Service}s can still decide that the attributes to retrieve from a query
     * and the attributes used to order results are the same, in which case 
     * a same {@code Enum} class would be defined as implementing both {@code Service.Attribute} 
     * and {@code Service.OrderingAttribute}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     */
    public static interface OrderingAttribute {
        //nothing here, it is only used for typing the Enum classes.
    }
    /**
     * {@code Enum} used to specify the direction of the ordering, 
     * for a given {@link OrderingAttribute} a query is sorted by.
     * <ul>
     * <li>{@code ASC}: order by ascending order.
     * <li>{@code DESC}: order by descending order.
     * </ul>
     * @see OrderingAttribute
     */
    public static enum Direction {
        ASC, DESC;
    }

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
