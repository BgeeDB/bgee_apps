package org.bgee.model.ontologycommon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An ontology holding {@link OntologyElement}s. It can for instance be used to hold 
 * the NCBI taxonomy (<code>Taxon</code> elements), the Uberon anatomical ontology 
 * (<code>AnatDevEntity</code> elements), etc.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 * @param <T>	The specific type implementing <code>OntologyElement</code>, held 
 * 				by this <code>Ontology</code>. 
 */
public abstract class Ontology<T extends OntologyElement> {
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
	 * A <code>Map</code> associating <code>OntologyElement</code>s as values 
	 * to the <code>id</code> of the <code>Entity</code> they wrap as keys 
	 * (<code>id</code> returned by {@link org.bgee.model.Entity#getId()}).
	 */
    private final Map<String, T> allElements;
    /**
     * A <code>Set</code> of <code>OntologyElement</code>s that are the roots 
     * of this <code>Ontology</code> (they have no parents 
     * by {@link OntologyElement.RelationType ISA_PARTOF} relations)
     */
    private final Set<T> roots;
    
    /**
     * Default constructor, protected as it is an abstract class. 
     */
    protected Ontology() {
    	this.allElements = new HashMap<String, T>();
    	this.roots       = new HashSet<T>();
    }
    
    /**
     * Return the element having its ID corresponding to the provided parameter, 
     * or <code>null</code> if this <code>Ontology</code> contains no element 
     * with such ID. 
     * 
     * @param id	A <code>String</code> corresponding to the ID of an element
     * 				present in this <code>Ontology</code>. 
     * @return		The <T> corresponding to <code>id</code>, 
     * 				present in this <code>Ontology</code>. <code>null</code> if none 
     * 				could be found. 
     */
    public T getElement(String id) {
    	return this.allElements.get(id);
    }
}