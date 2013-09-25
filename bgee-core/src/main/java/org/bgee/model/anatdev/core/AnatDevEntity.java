package org.bgee.model.anatdev.core;

import org.bgee.model.Entity;
import org.bgee.model.anatdev.AnatDevElement;
import org.bgee.model.ontologycommon.BaseOntologyElement;

/**
 * Parent class of anatomical or developmental {@link Entity}s. Unlike some other 
 * {@code AnatDevElement}s, they correspond to "real" entities 
 * (for instance, an {@link AnatEntity}), rather than to entities that were merged 
 * (see for instance {@link org.bgee.model.anatdev.evomapping.AnatDevMapping}).
 * <p>
 * For a matter of fact, this is why they are named "entity" (as they extend the class 
 * {@code Entity}), as opposed to {@code AnatDevElement}s, which do not 
 * provide methods such as, for instance, {@code getId}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class AnatDevEntity extends Entity implements AnatDevElement {
	/**
	 * A {@code BaseOntologyElement} implementing {@code OntologyElement}, 
	 * which methods in this class specified by this interface are delegated to. 
	 * This follows the principle of "favoring composition over inheritance".
	 */
    private BaseOntologyElement delegateOntElement;
	/**
     * Constructor providing the {@code id} of this {@code AnatDevEntity}. 
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
    protected AnatDevEntity(String id) {
    	super(id);
    }


	@Override
	public void registerWithId(String id) {
		delegateOntElement.registerWithId(id);
	}
}
