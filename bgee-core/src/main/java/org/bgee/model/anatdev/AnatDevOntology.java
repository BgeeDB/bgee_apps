package org.bgee.model.anatdev;

import org.bgee.model.ontologycommon.Ontology;

/**
 * Parent class of anatomical ontologies or developmental stage ontologies, 
 * holding {@link AnatDevEntity}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T> a subclass of <code>AnatDevEntity</code>. 
 */
public abstract class AnatDevOntology<T extends AnatDevEntity> extends Ontology<T> {

}
