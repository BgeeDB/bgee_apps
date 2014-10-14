package org.bgee.model.dao.api.anatdev;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
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
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, STARTSTAGEID, ENDSTAGEID, NONINFORMATIVE;
    }

    /**
     * Retrieves anatomical entities from data source according to a {@code Set} of 
     * {@code String}s that are the IDs of species allowing to filter the entities to use.
     * <p>
     * The anatomical entities are retrieved and returned as a {@code AnatEntityTOResultSet}. It is 
     * the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the anatomical entities to use
     * @return              An {@code AnatEntityTOResultSet} containing all anatomical entities
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getAnatEntities(Set<String> speciesIds) throws DAOException;
    
    /**
     * Retrieves non-informative anatomical entities without expression call from data source 
     * according to a {@code Set} of {@code String}s that are the IDs of species allowing to filter 
     * the entities to use.
     * <p>
     * The non-informative anatomical entities are retrieved and returned as a 
     * {@code AnatEntityTOResultSet}. It is the responsibility of the caller to close this 
     * {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the non-informative anatomical entities to use
     * @return              An {@code AnatEntityTOResultSet} containing non-informative 
     *                      anatomical entities from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public AnatEntityTOResultSet getNonInformativeAnatEntities(Set<String> speciesIds) 
            throws DAOException;
    
    /**
     * Inserts the provided anatomical entities into the data source, 
     * represented as a {@code Collection} of {@code AnatEntityTO}s. 
     * 
     * @param anatEntityTOs A {@code Collection} of {@code AnatEntityTO}s to be inserted into the 
     *                      data source.
     * @return              An {@code int} that is the number of inserted anatomical entities.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entities. The {@code SQLException} will be wrapped into a 
     *                      {@code DAOException} ({@code DAO}s do not expose these kind of 
     *                      implementation details).
     */
    public int insertAnatEntities(Collection<AnatEntityTO> anatEntityTOs) throws DAOException;


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
    public class AnatEntityTO extends EntityTO {

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
         * A {@code boolean} defining whether this anatomical entity is part of a non-informative 
         * subset in used the ontology. For instance, in Uberon, 'upper_level "abstract upper-level 
         * terms not directly useful for analysis"'.
         */
        private final boolean nonInformative;

        /**
         * Constructor providing the ID, the name, the description, the start stage, and the end 
         * stage of this anatomical entity.
         * 
         * @param id                A {@code String} that is the ID of this anatomical entity. 
         * @param name              A {@code String} that is the name of this anatomical entity.
         * @param description       A {@code String} that is the description of this anatomical 
         *                          entity.
         * @param startStageId      A {@code String} that is the start of developmental stage of 
         *                          this anatomical entity.
         * @param endStageId        A {@code String} that is the end of developmental stage of this 
         *                          anatomical entity.
         * @param nonInformative    A {@code boolean} defining whether this anatomical entity is 
         *                          part of a non-informative subset in the used ontology.
         */
        public AnatEntityTO(String id, String name, String description, 
                String startStageId, String endStageId, boolean nonInformative) {
            super(id, name, description);
            this.startStageId = startStageId;
            this.endStageId = endStageId;
            this.nonInformative = nonInformative;
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
         * @return  the {@code boolean} defining whether this anatomical entity is 
         *          part of a non-informative subset in the used ontology.
         */
        public boolean isNonInformative() {
            return this.nonInformative;
        }

        @Override
        public String toString() {
            return " ID: " + this.getId() + " - Name: " + this.getName() +
            " - Description: " + this.getDescription() + "- Start stage Id: " +  startStageId + 
            " - End stage Id:" + endStageId + " - Non-informative:" + nonInformative;
        }
    }
}
