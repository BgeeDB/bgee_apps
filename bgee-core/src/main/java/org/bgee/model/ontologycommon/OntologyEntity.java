package org.bgee.model.ontologycommon;

import java.util.Map;
import java.util.Set;

import org.bgee.model.Entity;

/**
 * An element part of an {@link Ontology}, holding an {@link org.bgee.model.Entity Entity}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 * @param <T>	a subclass of {@link org.bgee.model.Entity Entity}
 */
public abstract class OntologyEntity<T extends Entity> {

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
	 * The <code>Entity</code> wrapped by this <code>OntologyEntity</code>. 
	 */
	private final T entityWrapped;
	private Map<RelationType, Set<T>> directParents;
	private Map<RelationType, Set<T>> directChildren;

	/**
	 * Default constructor providing the <code>Entity</code> wrapped 
	 * by this <code>OntologyEntity</code>. Throws an <code>IllegalArgumentException</code> 
	 * if <code>Entity</code> is <code>null</code>. 
	 * Package protected as only {@link Ontology} should use this class. 
	 * 
	 * @param entity The <code>Entity</code> wrapped by this <code>OntologyEntity</code>.
	 * @throws IllegalArgumentException		if <code>Entity</code> is <code>null</code>. 
	 */
	protected OntologyEntity(T entity) {
		if (entity == null) {
			throw new IllegalArgumentException(
				"To instantiate an OntologyEntity, a non-null Entity must be provided.");
		}
		this.entityWrapped = entity;
	}
	
	/**
	 * Get the <code>Entity</code> wrapped by this <code>OntologyEntity</code>. 
	 * 
	 * @return the <code>Entity</code> wrapped by this <code>OntologyEntity</code>.
	 */
	protected T getEntity() {
		return this.entityWrapped;
	}
	
	/**
	 * hashcode based solely on the hashcode 
	 * of the {@link org.bgee.model.Entity Entity} wrapped.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityWrapped == null) ? 0 : entityWrapped.hashCode());
		return result;
	}
	/**
	 * <code>equals</code> based on the <code>equals</code> method 
	 * of the {@link org.bgee.model.Entity ENtity} wrapped.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OntologyEntity other = (OntologyEntity) obj;
		if (entityWrapped == null) {
			if (other.entityWrapped != null) {
				return false;
			}
		} else if (!entityWrapped.equals(other.entityWrapped)) {
			return false;
		}
		return true;
	}
}
