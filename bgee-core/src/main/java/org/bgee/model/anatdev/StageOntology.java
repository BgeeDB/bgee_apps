package org.bgee.model.anatdev;

import org.bgee.model.anatdev.core.DevStage;

/**
 * A developmental stage ontology, holding {@link DevStage} elements. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class StageOntology extends AnatDevOntology<DevStage> {
	/**
	 * Default constructor protected, instances should be obtained using a factory. 
	 */
    protected StageOntology() {
    	super();
    }
}
