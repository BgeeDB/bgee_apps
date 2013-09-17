package org.bgee.model.anatdev;

import org.bgee.model.ontologycommon.OntologyElement;

/**
 * Interface implemented by elements describing anatomy or development. 
 * Their characteristics are that they can hold genes with expression data, 
 * and that they can be used as part of an {@link AnatDevOntology}. 
 * <p>
 * This interface can be implemented by two types of classes: classes 
 * corresponding to "real" entities (for instance, {@link AnatomicalEntity}), 
 * or classes used to *group* real entities (for instance, 
 * {@link org.bgee.model.anatdev.evomapping.AnatDevMapping}). They implement 
 * this common interface because they performed many operations in the exact same way. 
 * But they cannot simply inherit from a same class, the inheritance graph 
 * is too complex. Also, some operations are implemented slightly differently.
 * <p>
 * For a matter of fact, this class is named an "element", because it does not specify 
 * methods such as <code>getName</code> (because merged elements will not have any), 
 * as opposed to an <code>AnatDevEntity</code>, which does extend the 
 * {@link org.bgee.model.Entity Entity} class, and provides method such as 
 * <code>getName</code>.
 * 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface AnatDevElement extends OntologyElement {
	
}
