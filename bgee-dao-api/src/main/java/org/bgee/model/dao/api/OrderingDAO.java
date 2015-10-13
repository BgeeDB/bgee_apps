package org.bgee.model.dao.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * A {@code DAO} defining methods allowing to specify attributes to use 
 * to order the results of a query. Not all {@code DAO}s provide ordering capabilities, 
 * this is why these features are defined in a separate interface.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since Bgee 13
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO} See {@link DAO}.
 * @param <U>   The type of {@code OrderingDAO.OrderingAttribute} that can be used 
 *              with this {@code OrderingDAO}, to define what attributes to use 
 *              to order the results of a query to this {@code OrderingDAO}.
 */
public interface OrderingDAO<T extends Enum<?> & DAO.Attribute, 
                             U extends Enum<?> & OrderingDAO.OrderingAttribute
                            > extends DAO<T> {
    
    /**
     * {@code Enum} used to specify the direction of the ordering, 
     * for a given {@link OrderingAttribute} a query is sorted by.
     * <ul>
     * <li>{@code ASC}: order by ascending order.
     * <li>{@code DESC}: order by descending order.
     * </ul>
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(Entry[])
     * @see #getOrderingAttributes()
     */
    public enum Direction {
        ASC, DESC;
    }
    /**
     * Interface implemented by {@code Enum} classes allowing to select 
     * the attributes used to order the results of a query to a {@code DAO}. 
     * This is a separate interface from {@link DAO.Attribute}, because 
     * the attributes to retrieve from a query, and the attributes to use to order 
     * the results of a query, are often different, and some attributes used for ordering 
     * can even be absent from the set of attributes retrievable from a query.
     * <p>
     * Note that implementations can still decide that the attributes to retrieve from a query
     * and the attributes used to order results are the same, in which case 
     * a same {@code Enum} class would be defined as implementing both {@code DAO.Attribute} 
     * and {@code OrderingDAO.OrderingAttribute}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Jul 2015
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(List)
     * @see #setOrderingAttributes(Entry[])
     * @see #setOrderingAttributes(Enum[])
     * @see #clearOrderingAttributes()
     * @see #getOrderingAttributes()
     * @since Bgee 13
     */
    public interface OrderingAttribute {
        //nothing here, it is only used for typing the Enum classes.
    }
    
    /**
     * Defines the attributes to use to order results of queries to this {@code DAO}, 
     * for all the following calls, specifying the ordering direction for each attribute. 
     * By default, results are not ordered. 
     * The order of the entries in {@code attributesWithDir} is important, 
     * and will define the priorities for ordering results based on multiple attributes.
     * <p>
     * If {@code attributesWithDir} is {@code null} or empty, 
     * an {@code IllegalArgumentException} is thrown. To reset the attributes used 
     * for ordering (and so, to disable ordering), use {@link #clearOrderingAttributes()}. 
     * 
     * @param attributesWithDir A {@code LinkedHashMap} defining the attributes to use 
     *                          to order results of all the following calls 
     *                          to this {@code DAO}, with {@code OrderingAttribute}s as keys, 
     *                          associated to their ordering {@code Direction} as values.
     * @throws IllegalArgumentException If {@code attributesWithDir} is {@code null} or empty.
     * @see #setOrderingAttributes(List)
     * @see #setOrderingAttributes(Entry[])
     * @see #setOrderingAttributes(Enum[])
     * @see #clearOrderingAttributes()
     */
    public void setOrderingAttributes(LinkedHashMap<U, Direction> attributesWithDir) 
        throws IllegalArgumentException;
    /**
     * Defines the attributes to use to order results of queries to this {@code DAO}, 
     * for all the following calls, with an {@code ASC} ordering direction for all of them 
     * (see {@link Direction}). 
     * The order of elements in {@code attributes} is important, 
     * and will define the priorities for ordering results based on multiple attributes.
     * <p>
     * If {@code attributes} is {@code null} or empty, 
     * an {@code IllegalArgumentException} is thrown. To reset the attributes used 
     * for ordering (and so, to disable ordering), use {@link #clearOrderingAttributes()}. 
     * 
     * @param attributes        A {@code List} defining the attributes to use 
     *                          to order results of all the following calls 
     *                          to this {@code DAO}, with an {@code ASC} ordering direction 
     *                          by default.
     * @throws IllegalArgumentException If {@code attributes} is {@code null} or empty.
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(Entry[])
     * @see #setOrderingAttributes(Enum[])
     * @see #clearOrderingAttributes()
     */
    public void setOrderingAttributes(List<U> attributes) throws IllegalArgumentException;
    
    /**
     * Convenient method equivalent to calling {@link #setOrderingAttributes(LinkedHashMap)} 
     * with a {@code LinkedHashMap} containing all entries provided to 
     * this method. The entries provided to this method could be for instance 
     * of type {@code SimpleEntry} or {@code SimpleImmutableEntry} (see 
     * {@code java.util.AbstractMap}).
     * 
     * @param attributes    A list of {@code Entry}s defining the attributes to use 
     *                      to order results of all the following calls 
     *                      to this {@code DAO}, associating the {@code OrderingAttribute}s 
     *                      to their ordering {@code Direction}.
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(List)
     * @see #setOrderingAttributes(Enum[])
     * @see #clearOrderingAttributes()
     */
    //suppress warning because it is the responsibility of the implementation 
    //to make the method robust to heap pollution, and to add the @SafeVarargs 
    //annotation. See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
    @SuppressWarnings("unchecked")
    public void setOrderingAttributes(Entry<U, Direction>... attributes);
    

    /**
     * Convenient method equivalent to calling {@link #setOrderingAttributes(List)} 
     * with a {@code List} containing all {@code OrderingAttributes} provided to 
     * this method. 
     * 
     * @param attributes    A list of {@code OrderingAttributes}s defining the attributes 
     *                      to use to order results of all the following calls 
     *                      to this {@code DAO}, with a default ordering direction 
     *                      (see {@link #setOrderingAttributes(List)}).
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(List)
     * @see #setOrderingAttributes(Entry[])
     * @see #clearOrderingAttributes()
     */
    //suppress warning because it is the responsibility of the implementation 
    //to make the method robust to heap pollution, and to add the @SafeVarargs 
    //annotation. See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
    @SuppressWarnings("unchecked")
    public void setOrderingAttributes(U... attributes);
    
    /**
     * Get the {@code OrderingAttributes}s used to define the ordering of the results 
     * obtained from calls to this {@code DAO}, associated to their ordering {@code Direction}. 
     * Returned {@code LinkedHashMap} is a copy. If empty, no ordering is requested. 
     * 
     * @return  A {@code LinkedHashMap} defining the attributes to use to order results 
     *          of all the following calls to this {@code DAO}, 
     *          with {@code OrderingAttribute}s as keys, associated to 
     *          their ordering {@code Direction} as values. 
     */
    public LinkedHashMap<U, Direction> getOrderingAttributes();

    /**
     * Resets the attributes used to order results obtained from this {@code DAO}, for all 
     * the following calls (meaning, ordering is disable).  
     * This is useful if you previously called one of the {@code setOrderingAttributes} methods, 
     * and now want to stop ordering results.
     * 
     * @see #setOrderingAttributes(LinkedHashMap)
     * @see #setOrderingAttributes(List)
     * @see #setOrderingAttributes(Entry[])
     * @see #setOrderingAttributes(Enum[])
     */
    public void clearOrderingAttributes();
}
