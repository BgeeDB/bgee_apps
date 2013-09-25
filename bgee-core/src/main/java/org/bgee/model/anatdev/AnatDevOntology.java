package org.bgee.model.anatdev;

import org.bgee.model.anatdev.core.AnatDevEntity;
import org.bgee.model.ontologycommon.Ontology;

/**
 * Parent class of anatomical ontologies or developmental stage ontologies, 
 * holding {@link AnatDevEntity}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T> a subclass of {@code AnatDevEntity}. 
 */
public abstract class AnatDevOntology<T extends AnatDevEntity> extends Ontology<T> {
	/**
	 * Default constructor protected, instances should be obtained using a factory. 
	 */
    protected AnatDevOntology() {
    	super();
    }
}
