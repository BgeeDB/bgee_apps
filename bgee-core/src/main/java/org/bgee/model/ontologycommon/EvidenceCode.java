package org.bgee.model.ontologycommon;

import org.bgee.model.Entity;

/**
 * Represents an Evidence Code from the Evidence Code Ontology. It is used 
 * to provide information about the evidences supporting an assertion.
 * <p>
 * Although evidence codes are part of an ontology, this class does not extend 
 * {@link OntologyEntity}, as we never use their ontology capabilities in the application, 
 * only for retrieving them from a <code>DAO</code> (for instance, "retrieve evidence codes 
 * children of this one").
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class EvidenceCode extends Entity {
	/**
     * Constructor providing the <code>id</code> of this <code>EvidenceCode</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespace only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A <code>String</code> representing the ID of 
     * 				this <code>AnatDevEntity</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespace only. 
     */
    protected EvidenceCode(String id) {
    	super(id);
    }
}
