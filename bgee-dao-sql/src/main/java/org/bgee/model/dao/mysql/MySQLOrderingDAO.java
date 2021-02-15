package org.bgee.model.dao.mysql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.OrderingDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * Parent class of all MySQL {@code DAO}s providing the capability of selecting 
 * how results retrieved should be ordered.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since Bgee 13
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 * @param <U>   The type of {@code OrderingDAO.OrderingAttribute} that can be used 
 *              with this {@code OrderingDAO}, to define what attributes to use 
 *              to order the results of a query to this {@code OrderingDAO}.
 */
public class MySQLOrderingDAO <T extends Enum<T> & DAO.Attribute, 
                               U extends Enum<U> & OrderingDAO.OrderingAttribute
                              > extends MySQLDAO<T> 
                              implements OrderingDAO<T, U> {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLOrderingDAO.class.getName());


    /**
     * A {@code LinkedHashMap} defining the attributes to use to order 
     * results of all the following calls to this {@code DAO}, 
     * with {@code OrderingAttribute}s as keys, associated to their 
     * ordering {@code Direction} as values.
     */
    private final LinkedHashMap<U, Direction> orderingAttributes;
    
    /**
     * Default constructor private, should not be used, a {@code MySQLDAOManager} 
     * should always be provided, see {@link #MySQLDAO(MySQLDAOManager)}.
     */
    @SuppressWarnings("unused")
    private MySQLOrderingDAO() {
        this(null);
    }
    /**
     * Default constructor providing the {@code MySQLDAOManager} that 
     * this {@code MySQLOrderingDAO} will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLOrderingDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
        this.orderingAttributes = new LinkedHashMap<U, Direction>();
    }
    
    @Override
    public void setOrderingAttributes(LinkedHashMap<U, Direction> attributesWithDir) 
            throws IllegalArgumentException {
        log.entry(attributesWithDir);
        if (attributesWithDir == null || attributesWithDir.isEmpty()) {
            throw log.throwing(
                    new IllegalArgumentException("OrderingAttributes must be provided"));
        }
        this.clearOrderingAttributes();
        this.orderingAttributes.putAll(attributesWithDir);
        log.traceExit();
    }
    
    @Override
    public void setOrderingAttributes(List<U> attributes) throws IllegalArgumentException {
        log.entry(attributes);
        if (attributes == null || attributes.isEmpty()) {
            throw log.throwing(
                    new IllegalArgumentException("OrderingAttributes must be provided"));
        }
        
        LinkedHashMap<U, Direction> newMap = new LinkedHashMap<U, Direction>();
        for (U attribute: attributes) {
            newMap.put(attribute, Direction.ASC);
        }
        this.setOrderingAttributes(newMap);
        
        log.traceExit();
    }
    
    /*
     * (non-Javadoc)
     * suppress warning because this method is robust to heap pollution, it only depends 
     * on the fact that the array will contain {@code Entry} elements, not on the fact 
     * that it is an array of {@code Entry}s; we can add the @SafeVarargs annotation. 
     * See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
     */
    @SafeVarargs
    @Override
    public final void setOrderingAttributes(Entry<U, Direction>... attributes) {
        log.entry((Object[]) attributes);
        
        LinkedHashMap<U, Direction> newMap = new LinkedHashMap<U, Direction>();
        for (int i = 0; i < attributes.length; i++) {
            newMap.put(attributes[i].getKey(), attributes[i].getValue());
        }
        this.setOrderingAttributes(newMap);
        
        log.traceExit();
    }
    
    /*
     * (non-Javadoc)
     * suppress warning because this method is robust to heap pollution, it only depends 
     * on the fact that the array will contain {@code U} elements, not on the fact 
     * that it is an array of {@code U}s; we can add the @SafeVarargs annotation. 
     * See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
     */
    @SafeVarargs
    @Override
    public final void setOrderingAttributes(U... attributes) {
        log.entry((Object[]) attributes);
        
        List<U> newAttributes = new ArrayList<U>();
        for (int i = 0; i < attributes.length; i++) {
            newAttributes.add(attributes[i]);
        }
        this.setOrderingAttributes(newAttributes);
        
        log.traceExit();
    }
    
    @Override
    public void clearOrderingAttributes() {
        log.entry();
        this.orderingAttributes.clear();
        log.traceExit();
    }
    
    @Override
    public LinkedHashMap<U, Direction> getOrderingAttributes() {
        log.entry();
        return log.traceExit(new LinkedHashMap<U, Direction>(orderingAttributes));
    }
}
