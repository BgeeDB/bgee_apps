package org.bgee.model.dao.api.ontologycommon;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * DAO defining queries using or retrieving {@link RelationTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, May 2021
 * @since   Bgee 13
 * @see RelationTO
 */
public interface RelationDAO extends DAO<RelationDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RelationTO}s 
     * obtained from this {@code RelationDAO}.
     * <ul>
     * <li>{@code RELATIONID}: corresponds to {@link RelationTO#getId()}.
     * <li>{@code SOURCEID}: corresponds to {@link RelationTO#getSourceId()}.
     * <li>{@code TARGETID}: corresponds to {@link RelationTO#getTargetId()}.
     * <li>{@code RELATIONTYPE}: corresponds to {@link RelationTO#getRelationType()}.
     * <li>{@code RELATIONSTATUS}: corresponds to {@link RelationTO#getRelationStatus()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        RELATION_ID("id"), SOURCE_ID("sourceId"), TARGET_ID("targetId"), 
        RELATION_TYPE("relationType"), RELATION_STATUS("relationStatus");

        /**
         * A {@code String} that is the corresponding field name in {@code RelationTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        
        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Retrieve anatomical entity relations from data source. The relations can be filtered  
     * by species IDs, source and/or target anatomical entity IDs, {@code RelationType}s, 
     * and {@code RelationStatus}.
     * <p>
     * The relations are retrieved and returned as a {@code RelationTOResultSet}. It is the 
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                              to retrieve relations for. Can be {@code null} or empty.
     * @param anySpecies            A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                              whether the relations retrieved should be valid in any 
     *                              of the requested species (if {@code true}), or in all 
     *                              of the requested species (if {@code false} or {@code null}).
     * @param sourceAnatEntityIds   A {@code Collection} of {@code String}s that are the IDs of anat. entities 
     *                              that should be the sources of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param targetAnatEntityIds   A {@code Collection} of {@code String}s that are the IDs of anat. entities 
     *                              that should be the targets of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param sourceOrTarget        A {@code Boolean} defining, when both {@code sourceAnatEntityIds} 
     *                              and {@code targetAnatEntityIds} are not empty, 
     *                              whether the relations retrieved should have one of {@code sourceAnatEntityIds} 
     *                              as source <strong>and/or</strong> one of {@code targetAnatEntityIds} as target 
     *                              (if {@code true}), or, one of {@code sourceAnatEntityIds} 
     *                              as source <strong>and</strong> one of {@code targetAnatEntityIds} as target 
     *                              (if {@code false} or {@code null}).
     * @param relationTypes         A {@code Collection} of {@code RelationType}s that are the relation 
     *                              types allowing to filter the relations to retrieve.
     *                              Can be {@code null} or empty.
     * @param relationStatus        A {@code Collection} of {@code RelationStatus} that are the status
     *                              allowing to filter the relations to retrieve.
     *                              Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code RelationDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code RelationTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @return                      A {@code RelationTOResultSet} allowing to retrieve anatomical 
     *                              entity relations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RelationTOResultSet<String> getAnatEntityRelations(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> sourceAnatEntityIds, Collection<String> targetAnatEntityIds, Boolean sourceOrTarget, 
            Collection<RelationType> relationTypes, Collection<RelationStatus> relationStatus, 
            Collection<RelationDAO.Attribute> attributes) throws DAOException;

    /**
     * Retrieve all relations between stages from data source. The relations 
     * can be filtered by species IDs or {@code RelationStatus} (the only parenthood 
     * relations between stages are "is_a" relations).
     * <p>
     * The relations are retrieved and returned as a {@code RelationTOResultSet}. It is the 
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds        A {@code Set} of {@code Integer}s that are the IDs of species 
     *                          to retrieve relations for.
     * @param relationStatus    A {@code Set} of {@code RelationStatus} that are the status
     *                          allowing to filter the relations to retrieve.
     * @return              A {@code RelationTOResultSet} allowing to retrieve stage relations 
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RelationTOResultSet<String> getStageRelationsBySpeciesIds(Set<Integer> speciesIds, 
            Set<RelationStatus> relationStatus) throws DAOException;

    /**
     * Retrieve developmental stage relations from data source. The relations can be filtered  
     * by species IDs, source and/or target dev. stage IDs, and {@code RelationStatus}.
     * <p>
     * The relations are retrieved and returned as a {@code RelationTOResultSet}. It is the 
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                              to retrieve relations for. Can be {@code null} or empty.
     * @param anySpecies            A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                              whether the relations retrieved should be valid in any 
     *                              of the requested species (if {@code true}), or in all 
     *                              of the requested species (if {@code false} or {@code null}).
     * @param sourceDevStageIds     A {@code Collection} of {@code String}s that are the IDs of dev. stages
     *                              that should be the sources of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param targetDevStageIds     A {@code Collection} of {@code String}s that are the IDs of dev. stages
     *                              that should be the targets of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param sourceOrTarget        A {@code Boolean} defining, when both {@code sourceDevStageIds} 
     *                              and {@code targetDevStageIds} are not empty, 
     *                              whether the relations retrieved should have one of {@code sourceDevStageIds} 
     *                              as source <strong>and/or</strong> one of {@code targetDevStageIds} as target 
     *                              (if {@code true}), or, one of {@code sourceDevStageIds} 
     *                              as source <strong>and</strong> one of {@code targetDevStageIds} as target 
     *                              (if {@code false} or {@code null}).
     * @param relationStatus        A {@code Collection} of {@code RelationStatus} that are the status
     *                              allowing to filter the relations to retrieve.
     *                              Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code RelationDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code RelationTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @return                      A {@code RelationTOResultSet} allowing to retrieve 
     *                              stage relations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RelationTOResultSet<String> getStageRelations(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> sourceDevStageIds, Collection<String> targetDevStageIds, Boolean sourceOrTarget, 
            Collection<RelationStatus> relationStatus, 
            Collection<RelationDAO.Attribute> attributes) throws DAOException;
    
    /**
     * Retrieve relations between taxa from data source. The relations can be filtered  
     * by source and/or target taxon IDs, and {@code RelationStatus}.
     * <p>
     * The relations are retrieved and returned as a {@code RelationTOResultSet}. It is the 
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param sourceTaxIds          A {@code Collection} of {@code Integer}s that are the IDs of taxa
     *                              that should be the sources of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param targetTaxIds          A {@code Collection} of {@code Integer}s that are the IDs of taxa
     *                              that should be the targets of the retrieved relations. 
     *                              Can be {@code null} or empty.
     * @param sourceOrTarget        A {@code Boolean} defining, when both {@code sourceTaxIds} 
     *                              and {@code targetTaxIds} are not empty, 
     *                              whether the relations retrieved should have one of {@code sourceTaxIds} 
     *                              as source <strong>and/or</strong> one of {@code targetTaxIds} as target 
     *                              (if {@code true}), or, one of {@code sourceTaxIds} 
     *                              as source <strong>and</strong> one of {@code targetTaxIds} as target 
     *                              (if {@code false} or {@code null}).
     * @param relationStatus        A {@code Collection} of {@code RelationStatus} that are the status
     *                              allowing to filter the relations to retrieve.
     *                              Can be {@code null} or empty.
     * @param lca                   A {@code boolean} specifying if {@code true} to only retrieve
     *                              relations connecting the requested taxa in {@code sourceTaxIds}
     *                              and {@code targetTaxIds} to taxa that are
     *                              least common ancestors of species in Bgee. The relations linking
     *                              some {@code sourceTaxIds} to some {@code targetTaxIds} are always
     *                              retrieved.
     * @param attributes            A {@code Collection} of {@code RelationDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code RelationTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @return                      A {@code RelationTOResultSet} allowing to retrieve 
     *                              taxon relations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RelationTOResultSet<Integer> getTaxonRelations(Collection<Integer> sourceTaxIds, 
        Collection<Integer> targetTaxIds, Boolean sourceOrTarget, Collection<RelationStatus> relationStatus, 
        boolean lca, Collection<RelationDAO.Attribute> attributes);
    

    /**
     * Inserts the provided anatomical entity relations into the Bgee database, represented as a 
     * {@code Collection} of {@code RelationTO}s.
     * 
     * @param relationTOs   A {@code Collection} of {@code RelationTO}s to be inserted as anatomical 
     *                      entity relations into the database.
     * @return              An {@code int} that is the number of inserted anatomical entity 
     *                      relations.
     * @throws IllegalArgumentException If {@code relationTOs} is empty or null.
     * @throws DAOException If an {@code Exception} occurred while trying to insert relations.
     */
    public int insertAnatEntityRelations(Collection<RelationTO<String>> relationTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * Insert the provided relations between Gene Ontology terms into the Bgee database, 
     * represented as a {@code Collection} of {@code RelationTO}s. 
     * 
     * @param relationTOs   A {@code Collection} of {@code RelationTO}s to be inserted 
     *                      into the database.
     * @return              An {@code int} that is the number of inserted relations.
     * @throws IllegalArgumentException If {@code relationTOs} is empty or null.
     * @throws DAOException If an {@code Exception} occurred while trying to insert relations.
     */
    public int insertGeneOntologyRelations(Collection<RelationTO<String>> relationTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code RelationTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     * 
     * @param <T> the type of target and source IDs in related {@code RelationTO}s.
     */
    public interface RelationTOResultSet<T> extends DAOResultSet<RelationTO<T>> {

    }

    /**
     * A {@code TransferObject} representing a relation between two members of an ontology, 
     * as stored in the Bgee database. 
     * <p>
     * As relations are oriented, this class defines a parent term (see {@link #getTargetId()} 
     * and a descent term (see {@link #getSourceId()}). The type of the relation 
     * can be specified (see {@link #getRelationType()}). The relation can be direct, 
     * or indirect (see {@link #getRelationStatus()}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     * 
     * @param <T> the type of target and source IDs
     */
    public class RelationTO<T> extends EntityTO<Integer> {

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
             * Convert the {@code String} representation of a relation type (for instance, 
             * retrieved from a database) into a {@code RelationType}. This method 
             * compares {@code representation} to the value returned by 
             * {@link #getStringRepresentation()}, as well as to the value 
             * returned by {@link Enum#name()}, for each {@code RelationType}.
             * 
             * @param representation    A {@code String} representing a relation type.
             * @return                  A {@code RelationType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code RelationType}.
             */
            public static final RelationType convertToRelationType(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(RelationType.class, representation));
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
             * Convert the {@code String} representation of a relation status (for instance, 
             * retrieved from a database) into a {@code RelationStatus}. This method 
             * compares {@code representation} to the value returned by 
             * {@link #getStringRepresentation()}, as well as to the value 
             * returned by {@link Enum#name()}, for each {@code RelationStatus}.
             * 
             * @param representation    A {@code String} representing a relation status.
             * @return                  A {@code RelationStatus} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code RelationStatus}.
             */
            public static final RelationStatus convertToRelationStatus(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(RelationStatus.class, representation));
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
         * A {@code T} that is the OBO-like ID of the parent term of this relation.
         * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         * GO:0051329 "mitotic interphase"}, then this {@code targetId} is 
         * {@code GO:0051329}.
         * @see #sourceId
         */
        private final T targetId;
        
        /**
         * A {@code T} that is the OBO-like ID of the parent term of this relation.
         * For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         * GO:0051329 "mitotic interphase"}, then this {@code sourceId} is 
         * {@code GO:0000080}.
         * @see #targetId
         */
        private final T sourceId;
        
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
         * (see {@link #getRelationStatus()}).
         * <p>
         * The relation ID, the relation type, and the relation status are set to {@code null}.
         * 
         * @param sourceId         A {@code T} that is the ID of the descent term.
         * @param targetId          A {@code T} that is the ID of the parent term.
         * @see RelationTO#RelationTO(String, String, String, RelationType, RelationStatus)
         */
        public RelationTO(T sourceId, T targetId) {
            this(null, sourceId, targetId, null, null);
        }
        /**
         * Constructor providing the ID of the parent term in the relation (see 
         * {@link #getTargetId()} for more details), the ID of the descent term 
         * (see {@link #getSourceId()}), the type of the relation (see {@link 
         * #getRelationType()}), and defining whether this relation is direct,  
         * indirect, or reflexive (see {@link #getRelationStatus()}).
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param relationId        An {@code Integer} that is the ID of this relation.
         * @param sourceId          A {@code T} that is the ID of the descent term.
         * @param targetId          A {@code T} that is the ID of the parent term.
         * @param relType           A {@code RelationType} defining the type of the relation.
         * @param relationStatus    A {@code RelationStatus} defining whether the relation
         *                          is direct, indirect, or reflexive.
         * @see RelationTO#RelationTO(Object, Object)
         */
        public RelationTO(Integer relationId, T sourceId, T targetId, 
                RelationType relType, RelationStatus relationStatus) {
            super(relationId);
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.relationType = relType;
            this.relationStatus = relationStatus;
        }

        /**
         * @return  A {@code T} that is the OBO-like ID of the parent term of this relation.
         *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         *          GO:0051329 "mitotic interphase"}, then this {@code targetId} is 
         *          {@code GO:0051329}.
         * @see #getSourceId()
         */
        public T getTargetId() {
            return this.targetId;
        }
        /**
         * @return  A {@code T} that is the OBO-like ID of the child term of this relation.
         *          For instance, if {@code GO:0000080 "mitotic G1 phase" part_of 
         *          GO:0051329 "mitotic interphase"}, then this {@code sourceId} is 
         *          {@code GO:0000080}.
         * @see #getTargetId()
         */
        public T getSourceId() {
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
    }
}
