package org.bgee.model.ontologycommon;

public interface OntologyElement {

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
}
