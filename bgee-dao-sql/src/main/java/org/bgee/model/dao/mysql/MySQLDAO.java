package org.bgee.model.dao.mysql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.dao.api.DAO;

/**
 * Parent class of all MySQL DAOs of this module.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 *
 * @param <T>   The type of {@code DAO.Attribute} that can be used with this {@code DAO}, 
 *              to define what attributes should be populated in the {@code TransferObject}s 
 *              obtained from this {@code DAO}.
 */
public abstract class MySQLDAO<T extends Enum & DAO.Attribute> implements DAO<T> {
    /**
     * A {@code Set} of {@code DAO.Attribute}s specifying the attributes to retrieve 
     * from the data source in order to build {@code TransferObject}s associated to 
     * this {@code DAO}.
     */
    private final Set<T> attributes;
    
    /**
     * Default constructor.
     */
    public MySQLDAO() {
        //attributes could be an EnumSet, but we would need to provide the class 
        //of T to the constructor in order to get the type...
        this.attributes = new HashSet<T>();
    }

    @Override
    public void setAttributesToGet(Collection<T> attributes) {
        this.clearAttributesToGet();
        if (attributes != null) {
            this.attributes.addAll(attributes);
        }
    }
    
    //suppress warning because this method is robust to heap pollution, it only depends 
    //on the fact that the array will contain {@code T} elements, not on the fact 
    //that it is an array of {@code T}; we can add the @SafeVarargs annotation. 
    //See http://docs.oracle.com/javase/7/docs/technotes/guides/language/non-reifiable-varargs.html
    @SafeVarargs
    @Override
    public final void setAttributesToGet(T... attributes) {
        Set<T> newAttributes = new HashSet<T>();
        for (int i = 0; i < attributes.length; i++) {
            newAttributes.add(attributes[i]);
        }
        this.setAttributesToGet(newAttributes);
    }

    @Override
    public void clearAttributesToGet() {
        this.attributes.clear();
    }
}
