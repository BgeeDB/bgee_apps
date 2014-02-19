package org.bgee.model.dao.api.ontologycommon;

import org.bgee.model.dao.api.TransferObject;

/**
 * An {@code TransferObject} representing a relation between two members of an ontology, 
 * as stored in the Bgee database. 
 * <p>
 * As relations are oriented, this class defines a parent term (see {@link #gettargetId()} 
 * and a descent term (see {@link #getsourceId()}). The type of the relation 
 * can be specified (see {@link #getRelationType()}). The relation can be direct, 
 * or indirect (see {@link #isDirectRelation()}).
 * <p>
 * Note that this class is one of the few {@code TransferObject}s that are not 
 * an {@link EntityTO}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class RelationTO extends TransferObject {

    /**
     * List the different relation types allowed in the Bgee database. 
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
     * @see RelationTO#getRelationType()
     * @since Bgee 13
     */
    public enum RelationType {
        ISA_PARTOF, DEVELOPSFROM, TRANSFORMATIONOF;
    }
    
    /**
     * A {@code String} that is the OBO-like ID of the parent term of this relation.
     * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     * GO:0051329 "mitotic interphase"}, then this {@code targetId} is 
     * {@code GO:0051329}.
     * @see #sourceId
     */
    private final String targetId;
    /**
     * A {@code String} that is the OBO-like ID of the parent term of this relation.
     * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     * GO:0051329 "mitotic interphase"}, then this {@code sourceId} is 
     * {@code GO:0000080}.
     * @see #targetId
     */
    private final String sourceId;
    /**
     * A {@link RelationType} that is the type of this relation in the Bgee database. 
     * These types might not always correspond to the OBO standard relation name.
     * If this attribute is {@code null}, it means that the relation type 
     * is not specified.
     */
    private final RelationType relationType;
    /**
     * A {@code boolean} defining whether the relation between {@code targetId} 
     * and {@code sourceId} is direct (for instance, A is_a B), or indirect 
     * (for instance, A is_a B is_a C, therefore there is an indirect composed 
     * relation between A and C: A is_a C). Default is {@code true}.
     */
    private final boolean directRelation;
    
    /**
     * Constructor providing the ID of the parent term in the relation (see 
     * {@link #gettargetId()} for more details), and the ID of the descent term 
     * (see {@link #getsourceId()}). The type of the relation (see {@link 
     * #getRelationType()}) is unspecified, and the relation is assumed to be direct 
     * (see {@link #isDirectRelation()}).
     * 
     * @param sourceId         A {@code String} that is the ID of the descent term.
     * @param targetId          A {@code String} that is the ID of the parent term.
     * @see RelationTO#RelationTO(String, String, RelationType, boolean)
     */
    public RelationTO(String sourceId, String targetId) {
        this(sourceId, targetId, null, true);
    }
    /**
     * Constructor providing the ID of the parent term in the relation (see 
     * {@link #gettargetId()} for more details), the ID of the descent term 
     * (see {@link #getsourceId()}), the type of the relation (see {@link 
     * #getRelationType()}), and defining whether this relation is direct or 
     * indirect (see {@link #isDirectRelation()}).
     * 
     * @param sourceId         A {@code String} that is the ID of the descent term.
     * @param targetId          A {@code String} that is the ID of the parent term.
     * @param relType           A {@code RelationType} defining the type of the relation.
     * @param directRelation    A {@code boolean} defining whether the relation is direct 
     *                          or indirect.
     * @see RelationTO#RelationTO(String, String)
     */
    public RelationTO(String sourceId, String targetId, RelationType relType, 
            boolean directRelation) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relationType = relType;
        this.directRelation = directRelation;
    }
    
    /**
     * @return  A {@code String} that is the OBO-like ID of the parent term of this relation.
     *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     *          GO:0051329 "mitotic interphase"}, then this {@code targetId} is 
     *          {@code GO:0051329}.
     * @see #getSourceId()
     */
    public String getTargetId() {
        return targetId;
    }
    /**
     * @return  A {@code String} that is the OBO-like ID of the parent term of this relation.
     *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     *          GO:0051329 "mitotic interphase"}, then this {@code sourceId} is 
     *          {@code GO:0000080}.
     * @see #getTargetId()
     */
    public String getSourceId() {
        return sourceId;
    }
    /**
     * @return  A {@link RelationType} that is the type of this relation in the Bgee database. 
     *          These types might not always correspond to the OBO standard relation name.
     *          If this attribute is {@code null}, it means that the relation type 
     *          is not specified.
     */
    public RelationType getRelationType() {
        return relationType;
    }
    /**
     * @return  A {@code boolean} defining whether the relation between {@code targetId} 
     *          and {@code sourceId} is direct (for instance, A is_a B), or indirect 
     *          (for instance, A is_a B is_a C, therefore there is an indirect composed 
     *          relation between A and C: A is_a C). Default is {@code true}.
     */
    public boolean isDirectRelation() {
        return directRelation;
    }
    
    @Override
    public String toString() {
        return "Source ID: " + this.getSourceId() + " - Target ID: " + this.getTargetId() + 
                " - Relation type: " + this.getRelationType() + 
                " - Is direct: " + this.isDirectRelation();
    }
    /* (non-Javadoc)
     * Note that the superclass does not implement hashCode, so this method does not 
     * take it into account, but it should if that changes in the future.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (directRelation ? 1231 : 1237);
        result = prime * result
                + ((relationType == null) ? 0 : relationType.hashCode());
        result = prime * result
                + ((sourceId == null) ? 0 : sourceId.hashCode());
        result = prime * result
                + ((targetId == null) ? 0 : targetId.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * Note that the superclass does not implement equals, so this method does not 
     * take it into account, but it should if that changes in the future.
     * @see java.lang.Object#equals(java.lang.Object)
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
        RelationTO other = (RelationTO) obj;
        if (directRelation != other.directRelation) {
            return false;
        }
        if (relationType != other.relationType) {
            return false;
        }
        if (sourceId == null) {
            if (other.sourceId != null) {
                return false;
            }
        } else if (!sourceId.equals(other.sourceId)) {
            return false;
        }
        if (targetId == null) {
            if (other.targetId != null) {
                return false;
            }
        } else if (!targetId.equals(other.targetId)) {
            return false;
        }
        return true;
    }
}
