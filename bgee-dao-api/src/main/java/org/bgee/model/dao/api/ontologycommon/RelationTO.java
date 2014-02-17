package org.bgee.model.dao.api.ontologycommon;

public class RelationTO {

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
     * @see RelationTO#relName
     * @since Bgee 13
     */
    public enum RelationType {
        ISA_PARTOF, DEVELOPSFROM, TRANSFORMATIONOF;
    }
    
    /**
     * A {@code String} that is the OBO-like ID of the parent term of this relation.
     * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     * GO:0051329 "mitotic interphase"}, then this {@code parentId} is 
     * {@code GO:0051329}.
     * @see #descentId
     */
    private final String parentId;
    /**
     * A {@code String} that is the OBO-like ID of the parent term of this relation.
     * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
     * GO:0051329 "mitotic interphase"}, then this {@code descentId} is 
     * {@code GO:0000080}.
     * @see #parentId
     */
    private final String descentId;
    /**
     * A {@link RelationType} that is the type of this relation in the Bgee database. 
     * These types might not always correspond to the OBO standard relation name.
     * If this attribute is {@code null}, it means that the relation type 
     * is not specified.
     */
    private final RelationType relType;
    
    public RelationTO(String parentId, String descentId) {
        this(parentId, descentId, null);
    }
    public RelationTO(String parentId, String descentId, RelationType relType) {
        this.parentId = parentId;
        this.descentId = descentId;
        this.relType = relType;
    }

}
