package org.bgee.model.gene;

import org.bgee.model.NamedEntity;
import org.bgee.model.species.Species;

/**
 * Class allowing to describe genes. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov. 2013
 * @since Bgee 01
 */
public class Gene extends NamedEntity {
	/**
	 * The {@code Species} this {@code Gene} belongs to.
	 */
	private final Species species;
	
	/**
	 * A {@code String} representing the ID of the {@code Species} 
	 * this {@code Gene} belongs to. If {@link #species} is not null, 
	 * should correspond to the value returned by the {@code getId()} method 
	 * on this {@code Species} object.
	 */
	private final String speciesId;
	
	/**
     * Constructor providing the {@code id} of this {@code Gene}.
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A  {@code String} representing the ID of this object.
     * @throws IllegalArgumentException If {@code id} is {@code null}, or blank. 
     */
    public Gene (String id) throws IllegalArgumentException {
    	this(id, null);
    }
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species.
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A  {@code String} representing the ID of this object.
     * @param speciesId     A {@code String} representing the ID of the species this gene belongs to.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene (String id, String speciesId) throws IllegalArgumentException {
        this(id, speciesId, null);
    }

    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, and the name. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A  {@code String} representing the ID of this object.
     * @param speciesId     A {@code String} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String id, String speciesId, String name) throws IllegalArgumentException {
    	this(id, speciesId, name, null);
    }
    
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, the name, and the description. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A  {@code String} representing the ID of this object.
     * @param speciesId     A {@code String} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @param description   A {@code String} representing the description of this gene.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String id, String speciesId, String name, String description) throws IllegalArgumentException {
    	this(id, speciesId, name, description, null);
    }
    
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, the name,
     * the description, and the {@code Species}. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A  {@code String} representing the ID of this object.
     * @param speciesId     A {@code String} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @param description   A {@code String} representing the description of this gene.
     * @param species       A {@code Species} representing the species this gene belongs to.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String id, String speciesId, String name, String description, Species species) throws IllegalArgumentException {
        super(id, name, description);
        this.speciesId = speciesId;
        this.species = species;
    }
    
	/**
	 * Returns the {@code Species} this {@code Gene} belongs to.
	 * @return The {@code Species} this {@code Gene} belongs to.
	 */
	public Species getSpecies() {
		return this.species;
	}
	
	/**
	 * Return the ID of the species this {@code Gene} belongs to. 
	 * <p>
	 * If the method {@link #getSpecies()} returned an object not {@code null}, 
	 * then this method is equivalent to calling {@code getId()} 
	 * on this {@code Species} object. Otherwise, it returns the value previously set 
	 * by using {@link #setSpeciesId(String)}.
	 * 
	 * @return 	A {@code String} corresponding to the ID of the species 
	 * 			this {@code Gene} belongs to. Equivalent to calling {@code getId()} 
	 * 			on the {@code Species} returned by the {@code getSpecies()} method, 
	 * 			if not null. Otherwise, returns the value previously set 
	 * 			by calling {@code setSpeciesId(String)}.
	 */
	public String getSpeciesId() {
		if (this.getSpecies() != null) {
			return this.getSpecies().getId();
		}
		return this.speciesId;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((species == null) ? 0 : species.hashCode());
	    result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true;
	    }
	    if (!super.equals(obj)) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    Gene other = (Gene) obj;
	    if (species == null) {
	        if (other.species != null) {
	            return false;
	        }
	    } else if (!species.equals(other.species)) {
	        return false;
	    }
	    if (speciesId == null) {
	        if (other.speciesId != null) {
	            return false;
	        }
	    } else if (!speciesId.equals(other.speciesId)) {
	        return false;
	    }
	    return true;
	}

	@Override
    public String toString() {
            return super.toString() + 
                    " - Species: " + getSpecies() + " - Species ID: " + getSpeciesId();
    }
}
