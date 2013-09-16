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
 * {@link org.bgee.model.anatdev.evogrouping.AnatDevEvoGroup}). The latter 
 * will most likely delegate implementation to the real underlying grouped entities. 
 * These two types of classes implement this common interface because they are used 
 * in many tools in common (for instance, 
 * {@link org.bgee.model.expressiondata.querytools.AnatDevExpressionQuery}).
 * <p>
 * For a matter of fact, this class is named an "element", because it does not specify 
 * methods such as <code>getId</code> (because merged elements will not have any really), 
 * as opposed to an <code>AnatDevEntity</code>, which does extend the 
 * {@link org.bgee.model.Entity Entity} class, and provides method such as 
 * <code>getId</code>.
 * 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface AnatDevElement extends OntologyElement {
	
}
