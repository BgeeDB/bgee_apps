package org.bgee.model.anatdev;

/**
 * An anatomical ontology, holding {@link AnatomicalEntity} elements. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatomicalOntology extends AnatDevOntology<AnatomicalEntity> {
	/**
	 * Default constructor protected, instances should be obtained using a factory. 
	 */
    protected AnatomicalOntology() {
    	super();
    }
}
