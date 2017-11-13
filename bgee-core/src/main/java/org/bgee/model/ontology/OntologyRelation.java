package org.bgee.model.ontology;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: remove this class
public class OntologyRelation<T> {
    private final static Logger log = LogManager.getLogger(OntologyRelation.class.getName());

    /**
     * List the relation status considered in Bgee.
     *
     * <ul>
     * <li>{@code DIRECT}
     * <li>{@code INDIRECT}
     * <li>{@code REFLEXIVE}
     * </ul>
     *
     * @author Julien Wollbrett
     * @version Bgee 14
     * @since Bgee 14
     */
    //XXX: I think we decided to not expose this concept, that was kept in RelationTO class only
    public enum RelationStatus {
        DIRECT, INDIRECT, REFLEXIVE;
    }

    private T sourceId;
    private T targetId;
    private RelationType relationType;
    private RelationStatus relationStatus;
    
    public OntologyRelation(T sourceId, T targetId, RelationType relationType, RelationStatus relationStatus) {
        if(targetId == null){
            throw log.throwing(new IllegalArgumentException("targetIds can't be null"));
        }
        if(sourceId == null){
            throw log.throwing(new IllegalArgumentException("sourceId can't be null"));
        }
        if(relationType == null){
            throw log.throwing(new IllegalArgumentException("relation type can't be null"));
        }
        if(relationStatus == null){
            throw log.throwing(new IllegalArgumentException("relation status can't be null"));
        }
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relationStatus = relationStatus;
        this.relationType = relationType;
    }


    public T getSourceId() {
        return sourceId;
    }

    public T getTargetId() {
        return targetId;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public RelationStatus getRelationStatus() {
        return relationStatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
        result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
        result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
        result = prime * result + ((relationStatus == null) ? 0 : relationStatus.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OntologyRelation<?> other = (OntologyRelation<?>) obj;
        if (!sourceId.equals(other.getSourceId())) {
            return false;
        }
        if (targetId != other.getTargetId()) {
            return false;
        }
        if (relationStatus != other.getRelationStatus()) {
            return false;
        }
        if (relationType != other.getRelationType()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("ElementRelation [").append(super.toString())
        .append(", sourceId=").append(sourceId)
        .append(", targetId=").append(targetId)
        .append(", relationStatus=").append(relationStatus)
        .append(", relationType=").append(relationType)
        .append("]");
        return builder.toString();
    }
}