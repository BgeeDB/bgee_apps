package org.bgee.model.ontologycommon;

import org.bgee.model.Entity;

/**
 * Represents an Evidence Code from the Evidence Code Ontology. It is used 
 * to provide information about the evidences supporting an assertion.
 * <p>
 * Although evidence codes are part of an ontology, this class does not extend 
 * {@link OntologyElement}, as we never use their ontology capabilities in the application, 
 * only for retrieving them from a {@code DAO} (for instance, "retrieve evidence codes 
 * children of this one").
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EvidenceCode extends Entity {
	/**
     * Constructor providing the {@code id} of this {@code EvidenceCode}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespace only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A {@code String} representing the ID of 
     * 				this {@code Entity}.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespace only. 
     */
    protected EvidenceCode(String id) {
    	super(id);
    }
}
