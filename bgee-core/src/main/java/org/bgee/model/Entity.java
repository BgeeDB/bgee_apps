package org.bgee.model;

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
	 */
    private String id;
	/**
	 * A <code>String</code> representing the name of this <code>Entity</code>.
	 */
    private String name;
    /**
	 * A <code>String</code> that is a description of this <code>Entity</code>.
	 */
    private String description;
    
    
    /**
     * Default constructor.
     */
    public Entity()
    {
    	this.setId(null);
    	this.setName(null);
    	this.setDescription(null);
    }
    
    
	/**
	 * Gets the ID of this <code>Entity</code>.
	 * @return 	A <code>String</code> representing the ID of this <code>Entity</code>
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * Sets the ID of this <code>Entity</code>.
	 * @param id A <code>String</code> that is the ID to set.
	 */
	public void setId(String id) {
		this.id = id;
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
}
