package org.bgee.model;

import org.apache.commons.lang.StringUtils;

/**
 * Parent class of all classes corresponding to real entities in the Bgee database. 
 * For instance, a <code>Gene</code>, a <code>Species</code>, 
 * a <code>RNASeqExperiment</code>, ...
 * They almost all have an ID, a name, and a description.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
public abstract class Entity 
{
	/**
	 * A <code>String</code> representing the ID of this <code>Entity</code>. 
	 * All <code>Entity</code>s have an <code>id</code> which is immutable 
	 * and provided at instantiation. 
	 */
    private final String id;
	/**
	 * A <code>String</code> representing the name of this <code>Entity</code>.
	 */
    private String name;
    /**
	 * A <code>String</code> that is a description of this <code>Entity</code>.
	 */
    private String description;
    
    
    /**
     * Default constructor not public, an {@link #id} must always be provided, 
     * see {@link #Entity(String)}.
     */
    //Constructor not public on purpose, suppress warnings
    @SuppressWarnings("unused")
	private Entity()
    {
    	this(null);
    }
    /**
     * Constructor providing the <code>id</code> of this <code>Entity</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespaces only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of this <code>Entity</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespaces only. 
     */
    public Entity(String id) throws IllegalArgumentException {
    	if (StringUtils.isBlank(id)) {
    		throw new IllegalArgumentException("id cannot be null, or empty (\"\"), " +
    				"or whitespaces only");
    	}
    	this.id = id;
    	this.setName(null);
    	this.setDescription(null);
    }
    
    
	/**
	 * Gets the ID of this <code>Entity</code>.
	 * All <code>Entity</code>s have an <code>id</code> which is immutable 
	 * and provided at instantiation.
	 * @return 	A <code>String</code> representing the ID of this <code>Entity</code>
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Gets the name of this <code>Entity</code>.
	 * @return 	A <code>String</code> representing the name of this <code>Entity</code>
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the name of this <code>Entity</code>.
	 * @param name A <code>String</code> that is the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description for this <code>Entity</code>.
	 * @return 	A <code>String</code> that is a description of this <code>Entity</code>
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the description of this <code>Entity</code>.
	 * @param description A <code>String</code> that is the description to set. 
	 */
	public void setDescription(String description) {
		this.description = description;
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
		Entity other = (Entity) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
