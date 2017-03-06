package org.bgee.model.dao.api;


/**
 * Parent class of all {@code TransferObject}s that are "Entities" in Bgee.
 * <p>
 * {@code TransferObject}s should be immutable. 
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @since Bgee 01
 * 
 * @param <T> The type of ID of this {@code EntityTO}
 */
public abstract class EntityTO<T> extends TransferObject {
	private static final long serialVersionUID = 9170289303150839721L;
	private final T id;
    
    /**
     * Constructor providing the ID of this {@code Entity}.
     * 
     * @param id    A {@code T} that is the ID. Can be {@code null}
     *              or empty.
     */
    protected EntityTO(T id) {
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public T getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return " ID: " + this.getId();
    }
}
