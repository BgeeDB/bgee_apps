package org.bgee.model.ontologycommon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An ontology holding {@link OntologyElement}s. It can for instance be used to hold 
 * the NCBI taxonomy ({@code Taxon} elements), the Uberon anatomical ontology 
 * ({@code AnatDevEntity} elements), etc.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 * @param <T>	The specific type implementing {@code OntologyElement}, held 
 * 				by this {@code Ontology}. 
 */
public abstract class Ontology<T extends OntologyElement> {
	/**
	 * List the different relation types used in Bgee. 
	 * Bgee makes no distinction between is_a and part_of relations, so they are merged 
	 * into one single enum type. Enum types available: 
	 * <ul>
	 * <li>{@code ISA_PARTOF}
	 * <li>{@code DEVELOPSFROM}
	 * <li>{@code TRANSFORMATIONOF}
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
	 * A {@code Map} associating {@code OntologyElement}s as values 
	 * to the {@code id} of the {@code Entity} they wrap as keys 
	 * ({@code id} returned by {@link org.bgee.model.Entity#getId()}).
	 */
    private final Map<String, T> allElements;
    /**
     * A {@code Set} of {@code OntologyElement}s that are the roots 
     * of this {@code Ontology} (they have no parents 
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
     * or {@code null} if this {@code Ontology} contains no element 
     * with such ID. 
     * 
     * @param id	A {@code String} corresponding to the ID of an element
     * 				present in this {@code Ontology}. 
     * @return		The <T> corresponding to {@code id}, 
     * 				present in this {@code Ontology}. {@code null} if none 
     * 				could be found. 
     */
    public T getElement(String id) {
    	return this.allElements.get(id);
    }
}