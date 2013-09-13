package org.bgee.model.ontologycommon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bgee.model.Entity;

/**
 * An ontology of <code>Entity</code> elements. It can be used to hold 
 * the NCBI taxonomy (<code>Species</code> elements), the Uberon anatomical ontology 
 * (<code>AnatomicalEntity</code> elements), etc.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 * @param <T>	A subclass of <code>Entity</code>. 
 */
public abstract class Ontology<T extends Entity> {
	/**
	 * List the different relation types used in Bgee. 
	 * Bgee makes no distinction between is_a and part_of relations, so they are merged 
	 * into one single enum type. Enum types available: 
	 * <ul>
	 * <li><code>ISA_PARTOF</code>
	 * <li><code>DEVELOPSFROM</code>
	 * <li><code>TRANSFORMATIONOF</code>
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public enum RelationType {
		ISA_PARTOF, DEVELOPSFROM, TRANSFORMATIONOF;
	}
	
	/**
	 * A <code>Map</code> associating <code>OntologyEntity</code>s as values 
	 * to the <code>id</code> of the <code>Entity</code> they wrap as keys 
	 * (<code>id</code> returned by {@link org.bgee.model.Entity#getId()}).
	 */
    private final Map<String, OntologyEntity<T>> allElements;
    /**
     * A <code>Set</code> of <code>OntologyEntity</code>s that are the roots 
     * of this <code>Ontology</code> (they have no parents 
     * by {@link OntologyEntity.RelationType ISA_PARTOF} relations)
     */
    private final Set<OntologyEntity<T>> roots;
    
    /**
     * Default constructor, protected as it is an abstract class. 
     */
    protected Ontology() {
    	this.allElements = new HashMap<String, OntologyEntity<T>>();
    	this.roots       = new HashSet<OntologyEntity<T>>();
    }
    
    /**
     * Return the <code>Entity</code> having its ID corresponding to the parameter, 
     * or <code>null</code> if this <code>Ontology</code> contains no <code>Entity</code> 
     * with such ID. 
     * 
     * @param id	A <code>String</code> corresponding to the ID of an <code>Entity</code> 
     * 				present in this <code>Ontology</code> 
     * 				(see {@link org.bgee.model.Entity#getId()}). 
     * @return		The <code>Entity</code> corresponding to <code>id</code>, 
     * 				present in this <code>Ontology</code>. <code>null</code> if none 
     * 				could be found. 
     */
    public T getEntity(String id) {
    	OntologyEntity<T> ontEntity = this.allElements.get(id);
    	if (ontEntity == null) {
    		return null;
    	}
    	return ontEntity.getEntity();
    }
}