package org.bgee.model.dao.api.ontologycommon;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * DAO defining queries using or retrieving {@link RelationTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see RelationTO
 * @since Bgee 13
 */
public interface RelationDAO  extends DAO<RelationDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RelationTO}s 
     * obtained from this {@code RelationDAO}.
     * <ul>
     * <li>{@code RELATIONID: corresponds to {@link RelationTO#getRelationId()()}.
     * <li>{@code SOURCEID: corresponds to {@link RelationTO#getSourceId()}.
     * <li>{@code TARGETID: corresponds to {@link RelationTO#getTargetId()()}.
     * <li>{@code RELATIONTYPE: corresponds to {@link RelationTO#getRelationType()}.
     * <li>{@code RELATIONSTATUS: corresponds to {@link RelationTO#getRelationStatus()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        RELATIONID, SOURCEID, TARGETID, RELATIONTYPE, RELATIONSTATUS;
    }

    /**
     * Retrieve all anatomical entity relations from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of species, and a {@code Set} of {@code RelationType}s 
     * that are the relation types, both allowing to filter the relations to use.
     * <p>
     * The relations are ordered by source ID and target ID, the only common columns to 
     * all relation tables. 
     * <p>
     * The relations are retrieved and returned as a {@code RelationTOResultSet}. It is the 
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the calls to use
     * @param relationTypes A {@code Set} of {@code RelationType}s that are the relation types
     *                      allowing to filter the calls to use
     * @return              A {@code RelationTOResultSet} containing all anatomical entity relations
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RelationTOResultSet getAllAnatEntityRelations(Set<String> speciesIds,
            Set<RelationType> relationTypes, Set<RelationStatus> relationStatus) throws DAOException;


    /**
     * Inserts the provided anatomical entity relations into the Bgee database, represented as a 
     * {@code Collection} of {@code RelationTO}s.
     * 
     * @param relationTOs   A {@code Collection} of {@code RelationTO}s to be inserted as anatomical 
     *                      entity relations into the database.
     * @return              An {@code int} that is the number of inserted anatomical entity 
     *                      relations.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entity relations. The {@code SQLException} will be wrapped into a 
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of 
     *                      implementation details).
     */
    public int insertAnatEntityRelations(Collection<RelationTO> relationTOs) throws DAOException;

    /**
     * Insert the provided relations between Gene Ontology terms into the Bgee database, 
     * represented as a {@code Collection} of {@code RelationTO}s. 
     * 
     * @param relationTOs   A {@code Collection} of {@code RelationTO}s to be inserted 
     *                      into the database.
     * @return              An {@code int} that is the number of inserted relations.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert relations 
     *                      between Gene Ontology terms. The {@code SQLException} will be wrapped 
     *                      into a {@code DAOException} ({@code DAOs} do not expose these kind of 
     *                      implementation details).
     */
    public int insertGeneOntologyRelations(Collection<RelationTO> relationTOs) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code RelationTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface RelationTOResultSet extends DAOResultSet<RelationTO> {

    }

    /**
     * A {@code TransferObject} representing a relation between two members of an ontology, 
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
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class RelationTO extends TransferObject {

        private static final long serialVersionUID = 6320202680108735124L;

        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(RelationTO.class.getName());

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
        public enum RelationType implements EnumDAOField {
            ISA_PARTOF("is_a part_of"), DEVELOPSFROM("develops_from"), 
            TRANSFORMATIONOF("transformation_of");
            
            /**
             * Convert the {@code String} representation of a data state (for instance, 
             * retrieved from a database) into a {@code RelationType}. This method 
             * compares {@code representation} to the value returned by 
             * {@link #getStringRepresentation()}, as well as to the value 
             * returned by {@link Enum#name()}, for each {@code RelationType}, 
             * .
             * 
             * @param representation    A {@code String} representing a data state.
             * @return  A {@code RelationType} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code RelationType}.
             */
            public static final RelationType convertToRelationType(String representation) {
                log.entry(representation);
                
                for (RelationType type: RelationType.values()) {
                    if (type.getStringRepresentation().equals(representation) || 
                            type.name().equals(representation)) {
                        return log.exit(type);
                    }
                }
                throw log.throwing(new IllegalArgumentException("\"" + representation + 
                        "\" does not correspond to any RelationType"));
            }
            
            /**
             * Convert a {@code Set} of {@code RelationType}s into a {@code Set} of 
             * {@code String}s, using the method 
             * {@link TransferObject#convertEnumSetToStringSet(Set)}.
             * 
             * @param enums A {@code Set} of {@code RelationType}s to be converted.
             * @return      A {@code Set} of {@code String}s that are the representation of 
             *              the {@code RelationType}s contained in {@code enums}.
             */
            public static final Set<String> convertToStringSet(Set<RelationType> relationType) {
                log.entry(relationType);
                return log.exit(TransferObject.convertEnumSetToStringSet(relationType));
            }

            
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code RelationType}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code RelationType}.
             */
            private RelationType(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            
            /**
             * @return  A {@code String} that is the representation 
             *          for this {@code RelationType}, for instance to be used in a database.
             */
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * List the different relation status allowed in the Bgee database. Enum types available: 
         * <ul>
         * <li>{@code DIRECT}
         * <li>{@code INDIRECT}
         * <li>{@code REFLEXIVE}
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @author Frederic Bastian
         * @version Bgee 13
         * @see RelationTO#getRelationStatus()
         * @since Bgee 13
         */
        public enum RelationStatus implements EnumDAOField {
            DIRECT("direct"), INDIRECT("indirect"), REFLEXIVE("reflexive");
            
            /**
             * Convert the {@code String} representation of a data state (for instance, 
             * retrieved from a database) into a {@code RelationStatus}. This method 
             * compares {@code representation} to the value returned by 
             * {@link #getStringRepresentation()}, as well as to the value 
             * returned by {@link Enum#name()}, for each {@code RelationStatus}.
             * 
             * @param representation    A {@code String} representing a data state.
             * @return  A {@code RelationStatus} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code RelationStatus}.
             */
            public static final RelationStatus convertToRelationStatus(String representation) {
                log.entry(representation);
                
                for (RelationStatus status: RelationStatus.values()) {
                    if (status.getStringRepresentation().equals(representation) || 
                            status.name().equals(representation)) {
                        return log.exit(status);
                    }
                }
                throw log.throwing(new IllegalArgumentException("\"" + representation + 
                        "\" does not correspond to any RelationStatus"));
            }
            
            /**
             * Convert a {@code Set} of {@code RelationStatus}s into a {@code Set} of 
             * {@code String}s, using the method 
             * {@link TransferObject#convertEnumSetToStringSet(Set)}.
             * 
             * @param enums A {@code Set} of {@code RelationStatus}s to be converted.
             * @return      A {@code Set} of {@code String}s that are the representation of 
             *              the {@code RelationStatus}s contained in {@code enums}.
             */
            public static final Set<String> convertToStringSet(Set<RelationStatus> relationStatus) {
                log.entry(relationStatus);
                return log.exit(TransferObject.convertEnumSetToStringSet(relationStatus));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code RelationStatus}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code RelationStatus}.
             */
            private RelationStatus(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            
            /**
             * @return  A {@code String} that is the representation 
             *          for this {@code RelationStatus}, for instance to be used in a database.
             */
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * @return the {@code String} representing the ID of this relation.
         */
        private final String id;
        
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
         * A {@code RelationStatus} defining whether the relation between {@code targetId} 
         * and {@code sourceId} is direct (for instance, A is_a B), or indirect 
         * (for instance, A is_a B is_a C, therefore there is an indirect composed 
         * relation between A and C: A is_a C), or reflexive.
         */
        private final RelationStatus relationStatus;

        /**
         * Constructor providing the ID of the parent term in the relation (see 
         * {@link #getTargetId()} for more details), and the ID of the descent term 
         * (see {@link #getSourceId()}). The type of the relation (see {@link 
         * #getRelationType()}) is unspecified, and the relation is assumed to be direct 
         * (see {@link #isDirectRelation()}).
         * <p>
         * The relation ID and the relation type are set to {@code null}.
         * 
         * @param sourceId         A {@code String} that is the ID of the descent term.
         * @param targetId          A {@code String} that is the ID of the parent term.
         * @see RelationTO#RelationTO(String, String, RelationType, boolean)
         */
        public RelationTO(String sourceId, String targetId) {
            this(null, sourceId, targetId, null, null);
        }
        /**
         * Constructor providing the ID of the parent term in the relation (see 
         * {@link #gettargetId()} for more details), the ID of the descent term 
         * (see {@link #getsourceId()}), the type of the relation (see {@link 
         * #getRelationType()}), and defining whether this relation is direct,  
         * indirect, or reflexive (see {@link #getRelationStatus()}).
         * 
         * @param relationId        A {@code String} that is the ID of this relation.
         * @param sourceId          A {@code String} that is the ID of the descent term.
         * @param targetId          A {@code String} that is the ID of the parent term.
         * @param relType           A {@code RelationType} defining the type of the relation.
         * @param directRelation    A {@code RelationStatus} defining whether the relation
         *                          is direct, indirect, or reflexive.
         * @see RelationTO#RelationTO(String, String)
         */
        public RelationTO(String relationId, String sourceId, String targetId, 
                RelationType relType, RelationStatus relationStatus) {
            this.id = relationId;
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.relationType = relType;
            this.relationStatus = relationStatus;
        }

        /**
         * @return the {@code String} representing the ID of this relation.
         */
        public String getId() {
            return this.id;
        }
        /**
         * @return  A {@code String} that is the OBO-like ID of the parent term of this relation.
         *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         *          GO:0051329 "mitotic interphase"}, then this {@code targetId} is 
         *          {@code GO:0051329}.
         * @see #getSourceId()
         */
        public String getTargetId() {
            return this.targetId;
        }
        /**
         * @return  A {@code String} that is the OBO-like ID of the parent term of this relation.
         *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         *          GO:0051329 "mitotic interphase"}, then this {@code sourceId} is 
         *          {@code GO:0000080}.
         * @see #getTargetId()
         */
        public String getSourceId() {
            return this.sourceId;
        }
        /**
         * @return  A {@link RelationType} that is the type of this relation. 
         *          These types might not always correspond to the OBO standard relation name.
         */
        public RelationType getRelationType() {
            return this.relationType;
        }
        /**
         * @return  A {@code RelationStatus} defining whether the relation between {@code targetId} 
         *          and {@code sourceId} is direct (for instance, A is_a B), indirect 
         *          (for instance, A is_a B is_a C, therefore there is an indirect composed 
         *          relation between A and C: A is_a C), or reflexive (special for each anatomical 
         *          entity, where anatEntityTargetId is equal to anatEntitySourceId).
         */
        public RelationStatus getRelationStatus() {
            return this.relationStatus;
        }

        @Override
        public String toString() {
            return "Relation ID: " + this.getId() + " - Source ID: " + this.getSourceId() + 
                    " - Target ID: " + this.getTargetId() + 
                    " - Relation type: " + this.getRelationType() + 
                    " - Relation status: " + this.getRelationStatus();
        }
        
        /**
         * Implementation of hashCode specific to {@code RelationTO}s: 
         * <ul>
         * <li>if {@link #getId()} returned a non-null value, the hashCode 
         * will be based solely on it. 
         * <li>Otherwise, hashCode will be based on all attributes of this class.
         * </ul>
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            if (id == null) {
                result = prime * result + ((relationStatus == null) ? 0 : 
                    relationStatus.hashCode());
                result = prime * result + ((relationType == null) ? 0 : 
                    relationType.hashCode());
                result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
                result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
            }
            return result;
        }
        
        /**
         * Implementation of equals specific to {@code ResultTO}s and consistent with 
         * the {@link #hashCode()} implementation. See {@link #hashCode()} for more details.
         * @see #hashCode()
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
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else {
                //if id is not null, we will base the equals solely on it
                return id.equals(other.id);
            }
            
            if (relationStatus != other.relationStatus) {
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
}
