package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;

/**
 * Parent class of all services used in Bgee. 
 * Services are designed to provide objects from one specific class, retrieved using DAOs, 
 * as well as other {@code Service}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2016
 * @since Bgee 01
 */
public abstract class Service {
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
     * See {@link #getServiceFactory()}.
     */
    private final ServiceFactory serviceFactory;

    /**
     * @param serviceFactory    The {@code ServiceFactory} that instantiated this {@code Service}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    protected Service(ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("The provided ServiceFactory cannot be null"));
        }
        this.serviceFactory = serviceFactory;
    }
    
   /**
    * @return   The {@code ServiceFactory} that instantiated this {@code Service}.
    */
    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }
    /**
     * @return  the {@code DAOManager} that this {@code Service} must use to obtain {@code DAO}s.
     */
    protected DAOManager getDaoManager() {
        return serviceFactory.getDAOManager();
    }
}
