package org.bgee.model.species;

import org.bgee.model.Entity;
import org.bgee.model.ontologycommon.BaseOntologyElement;
import org.bgee.model.ontologycommon.OntologyElement;

/**
 * Represents a taxon in a taxonomy. A {@link Species} is not considered 
 * a {@code Taxon}. As of Bgee 13, taxa are taken from the NCBI taxonomy.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Taxon extends Entity implements OntologyElement {

    /**
     * A {@code BaseOntologyElement} implementing {@code OntologyElement}, 
     * which methods in this class specified by this interface are delegated to. 
     * This follows the principle of "favoring composition over inheritance".
     */
    private final BaseOntologyElement delegateOntElement;
    
	/**
     * Constructor providing the {@code id} of this {@code Taxon}. 
     * This {@code id} cannot be {@code null}, or blank, 
     * otherwise an {@code IllegalArgumentException} will be thrown. 
     * 
     * @param id	A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										or blank. 
     */
	public Taxon(String id) throws IllegalArgumentException {
		super(id);
		this.delegateOntElement = new BaseOntologyElement();
	}
	
	public void registerWithId(String id) {
	    this.delegateOntElement.registerWithId(id);
	}

}
