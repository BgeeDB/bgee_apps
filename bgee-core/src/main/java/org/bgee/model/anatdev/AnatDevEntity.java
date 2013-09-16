package org.bgee.model.anatdev;

import org.bgee.model.Entity;
import org.bgee.model.ontologycommon.BaseOntologyElement;

/**
 * Parent class of anatomical or developmental {@link Entity}s. Unlike some other 
 * <code>AnatDevElement</code>s, they correspond to "real" entities 
 * (for instance, an {@link AnatomicalEntity}), rather than to entities that were merged 
 * (see for instance {@link org.bgee.model.anatdev.evogrouping.AnatDevEvoGroup}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class AnatDevEntity extends Entity implements AnatDevElement {
	/**
	 * A <code>BaseOntologyElement</code> implementing <code>OntologyElement</code>, 
	 * which methods in this class specified by this interface are delegated to. 
	 * This follows the principle of "favoring composition over inheritance".
	 */
    private BaseOntologyElement delegateOntElement;
	/**
     * Constructor providing the <code>id</code> of this <code>AnatDevEntity</code>. 
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
    protected AnatDevEntity(String id) {
    	super(id);
    }


	@Override
	public void registerWithId(String id) {
		delegateOntElement.registerWithId(id);
	}
}
