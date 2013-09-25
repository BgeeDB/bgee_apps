package org.bgee.model.species;

import org.bgee.model.ontologycommon.OntologyElement;

/**
 * Represents a taxon in a taxonomy. A {@link Species} is not considered 
 * a {@code Taxon}. As of Bgee 13, taxa are taken from the NCBI taxonomy.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Taxon extends OntologyElement<Taxon> {

	/**
     * Constructor providing the {@code id} of this {@code Taxon}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespace only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespace only. 
     */
	public Taxon(String id) throws IllegalArgumentException {
		super(id);
	}

}
