package org.bgee.model.species;

import org.bgee.model.ontologycommon.OntologyElement;

/**
 * Represents a taxon in a taxonomy. A {@link Species} is not considered 
 * a <code>Taxon</code>. As of Bgee 13, taxa are taken from the NCBI taxonomy.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Taxon extends OntologyElement<Taxon> {

	/**
     * Constructor providing the <code>id</code> of this <code>Taxon</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespace only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of this object.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespace only. 
     */
	public Taxon(String id) throws IllegalArgumentException {
		super(id);
	}

}
