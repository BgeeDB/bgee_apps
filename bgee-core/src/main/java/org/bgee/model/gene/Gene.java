package org.bgee.model.gene;

import org.bgee.model.species.Species;

/**
 * Class allowing to describe genes. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 01
 */
public class Gene {
    
    /**
     * A {@code String} that is the Ensembl gene ID.
     */
    private final String ensemblGeneId;
    
    /**
     * A {@code String} that is the name of the gene.
     */
    private final String name;

    /**
     * A {@code String} that is the description of the gene.
     */
    private final String description;

	/**
	 * The {@code Species} this {@code Gene} belongs to.
	 */
	private final Species species;
	
	/**
	 * An {@code Integer} representing the ID of the {@code Species} 
	 * this {@code Gene} belongs to. If {@link #species} is not null, 
	 * should correspond to the value returned by the {@code getId()} method 
	 * on this {@code Species} object.
	 */
	private final Integer speciesId;
	
	/**
     * Constructor providing the {@code id} of this {@code Gene}.
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException If {@code id} is {@code null}, or blank. 
     */
    public Gene (String ensemblGeneId) throws IllegalArgumentException {
    	this(ensemblGeneId, null);
    }
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species.
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @param speciesId     An {@code Integer} representing the ID of the species this gene belongs to.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene (String ensemblGeneId, Integer speciesId) throws IllegalArgumentException {
        this(ensemblGeneId, speciesId, null);
    }

    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, and the name. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @param speciesId     An {@code Integer} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String ensemblGeneId, Integer speciesId, String name) throws IllegalArgumentException {
    	this(ensemblGeneId, speciesId, name, null);
    }
    
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, the name, and the description. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @param speciesId     An {@code Integer} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @param description   A {@code String} representing the description of this gene.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String ensemblGeneId, Integer speciesId, String name, String description)
            throws IllegalArgumentException {
    	this(ensemblGeneId, speciesId, name, description, null);
    }
    
    /**
     * Constructor providing the {@code id} of this {@code Gene} and of the species, the name,
     * the description, and the {@code Species}. 
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @param speciesId     An {@code Integer} representing the ID of the species this gene belongs to.
     * @param name          A {@code String} representing the name of this gene.
     * @param description   A {@code String} representing the description of this gene.
     * @param species       A {@code Species} representing the species this gene belongs to.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Gene(String ensemblGeneId, Integer speciesId, String name, String description, Species species)
        throws IllegalArgumentException {
        this.ensemblGeneId = ensemblGeneId;
        this.name = name;
        this.description = description;
        this.speciesId = speciesId;
        this.species = species;
    }
    
	/**
	 * @return The {@code String} that is the Ensembl gene ID.
	 */
	public String getEnsemblGeneId() {
        return ensemblGeneId;
    }
    /**
     * @return  The {@code String} that is the name of the gene.
     */
    public String getName() {
        return name;
    }
    /**
     * @return  The {@code String} that is the description of the gene.
     */
    public String getDescription() {
        return description;
    }
    /**
	 * Returns the {@code Species} this {@code Gene} belongs to.
	 * 
	 * @return The {@code Species} this {@code Gene} belongs to.
	 */
	public Species getSpecies() {
		return this.species;
	}
	
	/**
	 * Return the ID of the species this {@code Gene} belongs to. 
	 * <p>
	 * If the method {@link #getSpecies()} returned an object not {@code null}, 
	 * then this method is equivalent to calling {@code getId()} on this {@code Species} object.
	 * Otherwise, it returns the value sets at the instantiation.
	 * 
	 * @return 	The {@code Integer} corresponding to the ID of the species 
	 * 			this {@code Gene} belongs to. Equivalent to calling {@code getId()} 
	 * 			on the {@code Species} returned by the {@code getSpecies()} method, 
	 * 			if not {@code null}. Otherwise, returns the value sets at the instantiation.
	 */
	public Integer getSpeciesId() {
		if (this.getSpecies() != null) {
			return this.getSpecies().getId();
		}
		return this.speciesId;
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ensemblGeneId == null) ? 0 : ensemblGeneId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((species == null) ? 0 : species.hashCode());
        result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
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
        Gene other = (Gene) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (ensemblGeneId == null) {
            if (other.ensemblGeneId != null) {
                return false;
            }
        } else if (!ensemblGeneId.equals(other.ensemblGeneId)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
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
