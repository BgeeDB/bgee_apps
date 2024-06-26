package org.bgee.model.dao.api.anatdev;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link AnatEntityTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see AnatEntityTO
 * @since Bgee 13
 */
public interface AnatEntityDAO extends DAO<AnatEntityDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AnatEntityTO}s 
     * obtained from this {@code AnatEntityDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link AnatEntityTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link AnatEntityTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link AnatEntityTO#getDescription()}.
     * <li>{@code STARTSTAGEID}: corresponds to {@link AnatEntityTO#getStartStageId()}.
     * <li>{@code ENDSTAGEID}: corresponds to {@link AnatEntityTO#getEndStageId()}.
     * <li>{@code NONINFORMATIVE}: corresponds to {@link AnatEntityTO#isNonInformative()}.
     * <li>{@code IS_CELL_TYPE}: corresponds to {@link AnatEntityTO#isCellType()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), NAME("name"), DESCRIPTION("description"), 
        START_STAGE_ID("startStageId"), END_STAGE_ID("endStageId"), 
        NON_INFORMATIVE("nonInformative")/*, CELL_TYPE("cellType")*/;

        /**
         * A {@code String} that is the corresponding field name in {@code AnatEntityTO} class.
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
     * Retrieves anatomical entities from data source existing in any of the requested species.
     * <p>
     * The anatomical entities are retrieved and returned as a {@code AnatEntityTOResultSet}. It is 
     * the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      allowing to filter the anatomical entities to use.
     * @return              An {@code AnatEntityTOResultSet} containing all anatomical entities
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds) 
            throws DAOException;
    
    /**
     * Retrieves anatomical entities from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of anatomical entities to filter the entities to use.
     * <p>
     * The anatomical entities are retrieved and returned as a {@code AnatEntityTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
     * 
     * @param anatEntitiesIds   A {@code Collection} of {@code String}s that are the IDs of anatomical 
     *                          entities allowing to filter the anatomical entities to use.
     * @return                  An {@code AnatEntityTOResultSet} containing all anatomical entities
     *                          from data source.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getAnatEntitiesByIds(Collection<String> anatEntitiesIds) 
            throws DAOException;
    
    /**
     * Retrieves anatomical entities from data source existing in any of the requested species, 
     * and potentially filtered based on the provided anatomical entity IDs. If an entity 
     * in {@code anatEntitiesIds} does not exists in any of the requested species, 
     * it will not be returned.
     * <p>
     * The anatomical entities are retrieved and returned as a {@code AnatEntityTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          allowing to filter the anatomical entities to use.
     * @param anatEntitiesIds   A {@code Collection} of {@code String}s that are the IDs of anatomical 
     *                          entities allowing to filter the anatomical entities to use.
     * @return                  An {@code AnatEntityTOResultSet} containing anatomical entities
     *                          from data source according to {@code speciesIds} and
     *                          {@code anatEntitiesIds}.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getAnatEntities(Collection<Integer> speciesIds, 
            Collection<String> anatEntitiesIds) throws DAOException;

    /**
     * Retrieves anatomical entities from data source according to the IDs of species and 
     * IDs of anatomical entities allowing to filter the entities to use. If an entity 
     * in {@code anatEntitiesIds} does not exists according to the species filtering, 
     * it will not be returned.
     * <p>
     * The anatomical entities are retrieved and returned as a {@code AnatEntityTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          allowing to filter the anatomical entities to use.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                          whether the entities retrieved should be valid in any 
     *                          of the requested species (if {@code true}), or in all 
     *                          of the requested species (if {@code false}).
     * @param anatEntitiesIds   A {@code Collection} of {@code String}s that are the IDs of anatomical 
     *                          entities allowing to filter the anatomical entities to use.
     * @param attributes        A {@code Collection} of {@code AnatEntityDAO.Attribute}s 
     *                          defining the attributes to populate in the returned 
     *                          {@code AnatEntityTO}s. If {@code null} or empty, 
     *                          all attributes are populated. 
     * @return                  An {@code AnatEntityTOResultSet} containing anatomical entities
     *                          from data source according to {@code speciesIds} and
     *                          {@code anatEntitiesIds}.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getAnatEntities(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> anatEntitiesIds, Collection<AnatEntityDAO.Attribute> attributes) 
                    throws DAOException;

    /**
     * Retrieves non-informative anatomical entities. If {@code evenIfUsedInAnnots} is {@code false},
     * only the non-informative anatomical entities not used in raw data annotations
     * (expression data and similarity annotations) will be returned.
     * <p>
     * The non-informative anatomical entities are retrieved and returned as
     * a {@code AnatEntityTOResultSet}. It is the responsibility of the caller to close
     * this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species
     *                              allowing to filter the non-informative anatomical entities to use.
     *                              If {@code null} or empty, non-informative anatomical entities existing
     *                              in any species are retrieved.
     * @param evenIfUsedInAnnots    A {@code boolean} specifying, if {@code true}, that even
     *                              the non-informative anat. entities used in annotations
     *                              should be returned.
     * @param attributes            A {@code Collection} of {@code AnatEntityDAO.Attribute}s
     *                              defining the attributes to populate in the returned
     *                              {@code AnatEntityTO}s. If {@code null} or empty,
     *                              all attributes are populated.
     * @return                      An {@code AnatEntityTOResultSet} containing non-informative
     *                              anatomical entities from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getNonInformativeAnatEntitiesBySpeciesIds(Collection<Integer> speciesIds,
            boolean evenIfUsedInAnnots, Collection<AnatEntityDAO.Attribute> attributes) throws DAOException;
    
    /**
     * Inserts the provided anatomical entities into the data source, 
     * represented as a {@code Collection} of {@code AnatEntityTO}s. 
     * 
     * @param anatEntityTOs A {@code Collection} of {@code AnatEntityTO}s to be inserted into the 
     *                      data source.
     * @return              An {@code int} that is the number of inserted anatomical entities.
     * @throws IllegalArgumentException If {@code anatEntityTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  anatomical entities. The {@code SQLException} will be 
     *                                  wrapped into a {@code DAOException} ({@code DAO}s do not 
     *                                  expose these kind of implementation details).
     */
    public int insertAnatEntities(Collection<AnatEntityTO> anatEntityTOs) 
            throws DAOException, IllegalArgumentException;


    /**
     * {@code DAOResultSet} specifics to {@code AnatEntityTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface AnatEntityTOResultSet extends DAOResultSet<AnatEntityTO> {
        
    }

    /**
     * An {@code EntityTO} representing an anatomical entity, as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class AnatEntityTO extends NamedEntityTO<String> {

        private static final long serialVersionUID = -4321114125536711089L;
        
        /**
         * A {@code String} that is the ID of the start of developmental stage of this 
         * anatomical entity.
         */
        private final String startStageId;
        
        /**
         * A {@code String} that is the ID of the end of developmental stage of this 
         * anatomical entity.
         */
        private final String endStageId;

        /**
         * A {@code Boolean} defining whether this anatomical entity is part of a non-informative 
         * subset in used the ontology. For instance, in Uberon, 'upper_level "abstract upper-level 
         * terms not directly useful for analysis"'.
         */
        private final Boolean nonInformative;
        /**
         * A {@code Boolean} defining whether this anatomical entity is a cell type, or not.
         * It is a cell type if it has the term with ID ConditionDAO.CELL_TYPE_ROOT_ID
         * as an ancestor by is_a or part_of relation.
         */
        private final Boolean cellType;

        /**
         * Constructor providing the ID, the name, the description, the start stage, and the end 
         * stage of this anatomical entity.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                A {@code String} that is the ID of this anatomical entity. 
         * @param name              A {@code String} that is the name of this anatomical entity.
         * @param description       A {@code String} that is the description of this anatomical 
         *                          entity.
         * @param startStageId      A {@code String} that is the start of developmental stage of 
         *                          this anatomical entity.
         * @param endStageId        A {@code String} that is the end of developmental stage of this 
         *                          anatomical entity.
         * @param nonInformative    A {@code Boolean} defining whether this anatomical entity is 
         *                          part of a non-informative subset in the used ontology.
         * @param cellType          A {@code Boolean} defining whether this anatomical entity is
         *                          a cell type.
         */
        public AnatEntityTO(String id, String name, String description, String startStageId,
                String endStageId, Boolean nonInformative, Boolean cellType) {
            super(id, name, description);
            this.startStageId = startStageId;
            this.endStageId = endStageId;
            this.nonInformative = nonInformative;
            this.cellType = cellType;
        }

        /**
         * @return  the {@code String} representing the ID of the start of developmental stage 
         *          of this anatomical entity.
         */
        public String getStartStageId() {
            return this.startStageId;
        }

        /**
         * @return  the {@code String} representing the ID of the end of developmental stage of this 
         *          anatomical entity.
         */
        public String getEndStageId() {
            return this.endStageId;
        }

        /**
         * @return  the {@code Boolean} defining whether this anatomical entity is 
         *          part of a non-informative subset in the used ontology.
         */
        public Boolean isNonInformative() {
            return this.nonInformative;
        }
        /**
         * @return  the {@code Boolean} defining whether this anatomical entity is
         *          a cell type. It is a cell type if it has the term with ID
         *          {@code ConditionDAO.CELL_TYPE_ROOT_ID} as an ancestor by
         *          is_a or part_of relation.
         */
        public Boolean isCellType() {
            return this.cellType;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AnatEntityTO [")
                   .append("getId()=").append(getId())
                   .append(", getName()=").append(getName())
                   .append(", getDescription()=").append(getDescription())
                   .append(", startStageId=").append(startStageId)
                   .append(", endStageId=").append(endStageId)
                   .append(", nonInformative=").append(nonInformative)
                   .append(", cellType=").append(cellType)
                   .append("]");
            return builder.toString();
        }
    }
}
