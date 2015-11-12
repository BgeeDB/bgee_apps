package org.bgee.model.dao.api;

import java.util.Collection;
import java.util.Set;

/**
 * Parent interface of all DAOs.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Jul 2015
 * @since Bgee 13
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 */
//TODO: when all DAOs will be immutable, this class will not need a generic type anymore.
public interface DAO<T extends Enum<T> & DAO.Attribute> {
    /**
     * Interface implemented by {@code Enum} classes allowing to select 
     * what are the attributes to populate in the {@code TransferObject}s obtained 
     * from a {@code DAO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    ////XXX: when all DAOs will be immutable, will we still actually need this interface?
    public static interface Attribute {
        //nothing here, it is only used for typing the Enum classes.
    }
    
    /**
     * {@code Enum} used to specify the direction of the ordering, 
     * for a given {@link OrderingAttribute} a query is sorted by.
     * <ul>
     * <li>{@code ASC}: order by ascending order.
     * <li>{@code DESC}: order by descending order.
     * </ul>
     */
    public static enum Direction {
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
     * and {@code DAO.OrderingAttribute}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Jul 2015
     * @since Bgee 13
     */
    ////XXX: when all DAOs will be immutable, will we still actually need this interface?
    public static interface OrderingAttribute {
        //nothing here, it is only used for typing the Enum classes.
    }

    /**
     * Allows to define what attributes should be populated in the {@code TransferObject}s 
     * obtained from this {@code DAO}, for all the following calls. By default, 
     * all available attributes are retrieved. If {@code attributes} is {@code null} 
     * or empty, it has the same effect as calling {@link #clearAttributes()}, 
     * and all available attributes will be retrieved for the next calls.
     * <p>
     * Defining what attributes should be populated, rather than populating 
     * all of them, allows to reduce the memory consumption 
     * of the {@code TransferObject}s retrieved.
     * </ul>
     * 
     * @param attributes    A {@code Collection} of {@code Attribute}s {@code T} 
     *                      defining the attributes to populate in the 
     *                      {@code TransferObject}s obtained from this {@code DAO}.
     * @see #clearAttributes()
     */
    //deprecated, as it was decided that each DAO method should accept Attributes when needed, 
    //and that DAOs should be immutable.
    @Deprecated
    public void setAttributes(Collection<T> attributes);
    
    /**
     * Convenient method equivalent to calling {@link #setAttributes(Collection)} 
     * with a {@code Collection} containing all {@code attributes} provided to 
     * this method.
     * 
     * @param attributes    A list of {@code Attribute}s {@code T} defining 
     *                      the attributes to populate in the {@code TransferObject}s 
     *                      obtained from this {@code DAO}, see {@link 
     *                      #setAttributes(Collection)}.
     * @see #setAttributes(Collection)
     * @see #clearAttributes()
     */
    //suppress warning because it is the responsibility of the implementation 
    //to make the method robust to heap pollution, and to add the @SafeVarargs 
    //annotation. See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
    @SuppressWarnings("unchecked")
    //deprecated, as it was decided that each DAO method should accept Attributes when needed, 
    //and that DAOs should be immutable.
    @Deprecated
    public void setAttributes(T... attributes);
    
    /**
     * Get {@code Attribute}s to retrieve in order to build {@code TransferObject}s associated 
     * to this {@code DAO}. Returned {@code Set} is a copy.
     * 
     * @return	A {@code Set} of {@code Attribute}s {@code T} defining the attributes
     * 			to populate in the {@code TransferObject}s obtained from this {@code DAO}.
     */
    //deprecated, as it was decided that each DAO method should accept Attributes when needed, 
    //and that DAOs should be immutable.
    @Deprecated
    public Set<T> getAttributes();

    /**
     * Resets what are the attributes that should be populated in 
     * the {@code TransferObject}s obtained from this {@code DAO}, for all 
     * the following calls. All available attributes will then be populated. 
     * This is useful if you previously called one of the {@code setAttributes} methods
     * to populate only a subset of the attributes, and now want to retrieve all of them.
     * 
     * @see #setAttributes(Collection)
     * @see #setAttributes(Enum[])
     */
    //deprecated, as it was decided that each DAO method should accept Attributes when needed, 
    //and that DAOs should be immutable.
    @Deprecated
    public void clearAttributes();
 }
