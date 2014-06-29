package org.bgee.model.dao.api;

import java.util.Collection;
import java.util.List;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * Parent interface of all DAOs.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 */
public interface DAO<T extends Enum<?> & DAO.Attribute> {
    /**
     * Interface implemented by {@code Enum} classes allowing to select 
     * what are the attributes to populate in the {@code TransferObject}s obtained 
     * from a {@code DAO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @see #setAttributes(Collection)
     * @see #setAttributes(Enum[])
     * @since Bgee 13
     */
    public interface Attribute {
        //nothing here, it is only used for typing the Enum classes.
    }
    
    /**
     * Allows to define what attributes should be populated in the {@code TransferObject}s 
     * obtained from this {@code DAO}, for all the following calls. By default, 
     * all available attributes are retrieved. If {@code attributes} is {@code null} 
     * or empty, it has the same effect than calling {@link #clearAttributes()}, 
     * and all available attributes will be retrieved for the next calls.
     * <p>
     * Defining what attributes should be populated, rather than populating 
     * all of them, has two advantages: 
     * <ul>
     * <li>reducing the memory consumption of the {@code TransferObject}s retrieved.
     * <li>potentially, reducing the number of {@code TransferObject}s returned. 
     * Indeed, if {@code attributes} cover some non-unique fields in the data source, 
     * the {@code DAO} should returns distinct results only. Retrieving 
     * {@code TransferObject}s will all available attributes would make all results 
     * to be considered distinct, while the client would have needed only a subset 
     * of these results, interested in only a subset of the attributes available. 
     * This can have huge effect on memory consumption, as {@code DAO}s in Bgee 
     * can return thousands of results.
     * </ul>
     * 
     * @param attributes    A {@code Collection} of {@code Attribute}s {@code T} 
     *                      defining the attributes to populate in the 
     *                      {@code TransferObject}s obtained from this {@code DAO}.
     * @see #clearAttributes()
     */
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
    public void setAttributes(T... attributes);
    
    /**
     * Get {@code Attribute}s to retrieve in order to build {@code TransferObject}s associated 
     * to this {@code DAO}. Returned {@code Collection} is a copy.
     * 
     * @return	A {@code Collection} of {@code Attribute}s {@code T} defining the attributes
     * 			to populate in the {@code TransferObject}s obtained from this {@code DAO}.
     */
    public Collection<T> getAttributes();

    /**
     * Resets what are the attributes that should be populated in 
     * the {@code TransferObject}s obtained from this {@code DAO}, for all 
     * the following calls. All available attributes will then be populated. 
     * This is useful if you previously called {@code #setAttributes(Collection)}
     * or {@link #setAttributes(Enum[])} to populate only a subset of the attributes, 
     * and now want to retrieve all of them.
     * 
     * @see #setAttributes(Collection)
     * @see #setAttributes(Enum[])
     */
    public void clearAttributes();
    
    /**
     * Convenient method to retrieve all {@code TransferObject}s returned by {@code resultSet}. 
     * This method is useful because, usually, DAO methods returned a {@code DAOResultSet}, 
     * allowing to iterate the results without putting all of them in memory; this method 
     * can thus be used to retrieve all results at once, putting all of them in memory. 
     * <p>
     * This method will call {@link DAOResultSet#next()} as long as it returns {@code true}, 
     * and will store, in a {@code List}, the {@code TransferObject}s returned by 
     * {@link DAOResultSet#getTO()}. It will then call {@link DAOResultSet#close()}, before 
     * returning the {@code List} of {@code TransferObject}s.
     * 
     * @param resultSet The {@code DAOResultSet} to retrieve {@code TransferObject}s from.
     * @return          A {@code List} of {@code TransferObject}s, retrieved by iterating 
     *                  the results of {@code resultSet}, and stored in the order 
     *                  they were retrieved. 
     * @throws DAOException if an error occurred while calling {@code next}, or {@code getTO}, 
     *                      or {@code close}, on {@code resultSet}. 
     */
    public <O extends TransferObject> List<O> getAllTOs(DAOResultSet<O> resultSet) 
            throws DAOException;
}
