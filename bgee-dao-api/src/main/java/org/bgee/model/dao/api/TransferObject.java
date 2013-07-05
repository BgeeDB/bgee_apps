package org.bgee.model.dao.api;

/**
 * Parent class of all <code>TransferObject</code>s, with attributes common 
 * to almost all entities returned. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class TransferObject 
{
    public String id;
    public String name;
    public String description;
	
	public TransferObject()
    {
    	this.id = null;
    	this.name = null;
    	this.description = null;
    }
}
