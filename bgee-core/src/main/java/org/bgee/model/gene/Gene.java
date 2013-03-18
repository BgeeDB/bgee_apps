package org.bgee.model.gene;

import org.bgee.model.Entity;
import org.bgee.model.species.Species;

/**
 * Class allowing to describe genes. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
public class Gene extends Entity
{
	/**
	 * The <code>Species</code> this <code>Gene</code> belongs to.
	 */
	private Species species;
	/**
	 * A <code>String</code> representing the ID of the <code>Species</code> 
	 * this <code>Gene</code> belongs to. If {@link #species} is not null, 
	 * should correspond to the value returned by the <code>getId()</code> method 
	 * on this <code>Species</code> object.
	 */
	private String speciesId;
	/**
	 * Default constructor. 
	 */
    public Gene()
    {
    	super();
    }
}
