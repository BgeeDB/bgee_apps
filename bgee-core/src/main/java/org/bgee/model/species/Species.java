package org.bgee.model.species;

import org.bgee.model.NamedEntity;

/**
 * Class allowing to describe species used in Bgee.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
//TODO: equals/hashCode/toString
public class Species extends NamedEntity {
    
	/** A {@String} representing the genus (e.g., "homo" for human) of the species */
	private final String genus;
	
	/** A {@String} representing the "scientific" (e.g., "sapiens" for human) name of the species */
	private final String speciesName;
	
    /**
     * 0-arg constructor private, at least an ID must be provided, see {@link #Species(String)}.
     */
    @SuppressWarnings("unused")
    private Species() {
        this(null);
    }
    
    /**
     * Constructor providing the {@code id} of this {@code Species}.
     * This {@code id} cannot be blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     *
     * @param id    A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException if {@code id} is blank.
     */
    public Species(String id) throws IllegalArgumentException {
        this(id, null, null);
    }
    /**
     * Constructor of {@code Species}.
     * @param id            A {@code String} representing the ID of this {@code Species}. 
     *                      Cannot be blank.
     * @param name          A {@code String} representing the (common) name of this {@Species}.
     * @param description   A {@code String} description of this {@Species}.
     */
    public Species(String id, String name, String description) throws IllegalArgumentException {
        this(id, name, description, null, null);
    }
    
    /**
     * Constructor of {@code Species}.
     * @param id            A {@code String} representing the ID of this {@code Species}. 
     *                      Cannot be blank.
     * @param name          A {@code String} representing the (common) name of this {@Species}.
     * @param description   A {@code String} description of this {@Species}.
     * @param genus			A {@code String} representing the genus of this {@Species} (e.g., "homo" for human).
     * @param speciesName   A {@String} representing the "scientific" name of this {@Species} (e.g., "sapiens" for human)
     */
    public Species(String id, String name, String description, String genus, String speciesName) throws IllegalArgumentException {
        super(id, name, description);
        this.genus = genus;
        this.speciesName = speciesName;
    }
    

    public String getGenus() {
    	return this.genus;
    }
    
    public String getSpeciesName() {
    	return this.speciesName;
    }
    
    public String getShortName() {
    	if (genus == null || speciesName == null) return "";
    	return genus.toUpperCase().charAt(0) +". "+speciesName;
    }
}

