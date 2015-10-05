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
    
	/** @see #getGenus() */
	private final String genus;
	
	/** @see #getSpeciesName() */
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
     * @param name          A {@code String} representing the (common) name of this {@code Species}.
     * @param description   A {@code String} description of this {@code Species}.
     */
    public Species(String id, String name, String description) throws IllegalArgumentException {
        this(id, name, description, null, null);
    }
    
    /**
     * Constructor of {@code Species}.
     * @param id            A {@code String} representing the ID of this {@code Species}. 
     *                      Cannot be blank.
     * @param name          A {@code String} representing the (common) name of this {@code Species}.
     * @param description   A {@code String} description of this {@code Species}.
     * @param genus			A {@code String} representing the genus of this {@code Species} (e.g., "Homo" for human).
     * @param speciesName   A {@code String} representing the species name of this {@code Species} (e.g., "sapiens" for human)
     */
    public Species(String id, String name, String description, String genus, String speciesName) throws IllegalArgumentException {
        super(id, name, description);
        this.genus = genus;
        this.speciesName = speciesName;
    }


    /**
     * @return A {@code String} representing the genus of the species (e.g., "Homo" for human)
     */
    public String getGenus() {
    	return this.genus;
    }

    /**
     * @return A {@code String} representing the species name of this {@code Species} (e.g., "sapiens" for human)
     */
    public String getSpeciesName() {
    	return this.speciesName;
    }
    
    /**
     * @return  A {@code String} representing the species common name (e.g., "human" for Homo sapiens)
     */
    @Override
    //method overridden to provide a more accurate javadoc
    public String getName() {
        return super.getName();
    }
    
    /**
     * @return  A {@code String} that is the scientific name of this {@code Species}, 
     *          for instance, "Homo sapiens" for human. 
     */
    public String getScientificName() {
        return this.getGenus() + " " + this.getSpeciesName();
    }

    /**
     * @return A {@code String} containing a short representation of the name (e.g., "H. sapiens" for Homo sapiens).
     */
    public String getShortName() {
    	if (genus == null || speciesName == null) return "";
    	return genus.toUpperCase().charAt(0) +". "+speciesName;
    }


}

