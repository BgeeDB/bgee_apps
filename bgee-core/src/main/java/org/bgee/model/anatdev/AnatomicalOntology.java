package org.bgee.model.anatdev;

import org.bgee.model.anatdev.core.AnatEntity;

/**
 * An anatomical ontology, holding {@link AnatEntity} elements. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatomicalOntology extends AnatDevOntology<AnatEntity> {
	/**
	 * Default constructor protected, instances should be obtained using a factory. 
	 */
    protected AnatomicalOntology() {
    	super();
    }
}
