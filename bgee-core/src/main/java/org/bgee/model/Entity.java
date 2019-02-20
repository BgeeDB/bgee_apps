package org.bgee.model;

/**
 * Parent class of all classes corresponding to real entities in the Bgee database. 
 * For instance, a {@code Gene}, a {@code Species}, 
 * a {@code RNASeqExperiment}, ... Basically, anything that can have an ID.
 * <p>
 * Note that {@code equals} and {@code hashCode} methods of {@code Entity}s 
 * should be solely based on their ID provided at instantiation.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 01
 * 
 * @param <T> The type of ID of this {@code Entity}
 */
//TODO: use Parametric class to specify whether ID is a String or a Integer. Same in NamedEntity.
public abstract class Entity<T extends Comparable<T>> {
	/**
	 * @see #getId()
	 */
    private final T id;
    
    /**
     * Default constructor not public, at least an ID must always be provided, 
     * see {@link #Entity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
	private Entity() {
    	this(null);
    }
    /**
     * Constructor providing the ID of this {@code Entity}. 
     * {@code id} cannot be {@code null}, otherwise an {@code IllegalArgumentException} is thrown. 
     * 
     * @param id	A {@code T} representing the ID of this {@code Entity}.
     * @throws IllegalArgumentException 	if {@code id} is blank. 
     */
    public Entity(T id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("the ID provided cannot be blank.");
        }
        this.id = id;
    }
    
    
	/**
	 * @return 	A {@code T} representing the ID of this {@code Entity}
	 */
	public T getId() {
		return this.id;
	}
	
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Entity<?> other = (Entity<?>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Entity [id=").append(id).append("]");
        return builder.toString();
    }
}
