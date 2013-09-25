package org.bgee.model.ontologycommon;

import org.bgee.model.Entity;

/**
 * Represents a Confidence information from the 
 * <a href='http://wiki.isb-sib.ch/biocuration/Confidence_information_draft'>
 * Confidence Information Ontology</a>. It is used 
 * to provide information about the confidence in an assertion.
 * <p>
 * Although confidence information is part of an ontology, this class does not extend 
 * {@link OntologyElement}, as we never use its ontology capabilities in the application, 
 * only for retrieving it from a {@code DAO} (for instance, "retrieve confidences  
 * children of this one").
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Confidence extends Entity {
	/**
     * Constructor providing the {@code id} of this {@code Confidence}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespace only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A {@code String} representing the ID of 
     * 				this {@code AnatDevEntity}.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespace only. 
     */
    protected Confidence(String id) {
    	super(id);
    }
}
