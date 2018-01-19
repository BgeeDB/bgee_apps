package org.bgee.model.ontology;

/**
 * List the relation types considered in Bgee. 
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