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
    	this.setSpecies(null);
    	this.setSpeciesId(null);
    }
    
    
	/**
	 * Returns the <code>Species</code> this <code>Gene</code> belongs to.
	 * @return The <code>Species</code> this <code>Gene</code> belongs to.
	 */
	public Species getSpecies() 
	{
		return this.species;
	}
	/**
	 * Sets the <code>Species</code> this <code>Gene</code> belongs to. 
	 * <p>
	 * Following calls to {@link #getSpeciesId()} will return the value returned 
	 * by a call to <code>getId()</code> on <code>species</code>, 
	 * even if {@link #setSpeciesId(String)} is used previously or afterwards. 
	 * 
	 * @param species 	the <code>Species</code> to set, that this <code>Gene</code> belongs to.
	 */
	public void setSpecies(Species species) 
	{
		this.species = species;
	}
	
	/**
	 * Return the ID of the species this <code>Gene</code> belongs to. 
	 * <p>
	 * If the method {@link #getSpecies()} returned an object not <code>null</code>, 
	 * then this method is equivalent to calling <code>getId()</code> 
	 * on this <code>Species</code> object. Otherwise, it returns the value previously set 
	 * by using {@link #setSpeciesId(String)}.
	 * 
	 * @return 	A <code>String</code> corresponding to the ID of the species 
	 * 			this <code>Gene</code> belongs to. Equivalent to calling <code>getId()</code> 
	 * 			on the <code>Species</code> returned by the <code>getSpecies()</code> method, 
	 * 			if not null. Otherwise, returns the value previously set 
	 * 			by calling <code>setSpeciesId(String)</code>.
	 */
	public String getSpeciesId() 
	{
		if (this.getSpecies() != null) {
			return this.getSpecies().getId();
		}
		return this.speciesId;
	}
	/**
	 * Sets the ID of the species this <code>Gene</code> belongs to. 
	 * <p>
	 * Note that the value returned by {@link #getSpeciesId()} might not correspond 
	 * to the value set here: if {@link #getSpecies()} returns 
	 * a <code>Species</code> object not null, then the value set here is not used, 
	 * and only the value returned by a call to <code>getId()</code> 
	 * on the <code>Species</code> object is used. 
	 * 
	 * @param speciesId 	A <code>String</code> corresponding to the ID of the species 
	 * 						this <code>Gene</code> belongs to. 
	 */
	public void setSpeciesId(String speciesId) 
	{
		this.speciesId = speciesId;
	}
}
