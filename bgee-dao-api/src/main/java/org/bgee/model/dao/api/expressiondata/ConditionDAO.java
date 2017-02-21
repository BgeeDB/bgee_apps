package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link ConditionTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Feb. 2017
 * @see ConditionTO
 */
public interface ConditionDAO extends DAO<ConditionDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ConditionTO}s 
     * obtained from this {@code ConditionDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ConditionTO#getId()}.
     * <li>{@code EXPR_MAPPED_CONDITION_ID}: corresponds to {@link ConditionTO#getExprMappedConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link ConditionTO#getAnatEntityId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link ConditionTO#getStageId()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link ConditionTO#getSpeciesId()}.
     * <li>{@code SEX}: corresponds to {@link ConditionTO#getSex()}.
     * <li>{@code SEX_INFERRED}: corresponds to {@link ConditionTO#getSexInferred()}.
     * <li>{@code STRAIN}: corresponds to {@link ConditionTO#getStrain()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), EXPR_MAPPED_CONDITION_ID("exprMappedConditionId"), ANAT_ENTITY_ID("anatEntityId"), 
        STAGE_ID("stageId"), SPECIES_ID("speciesId");

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
     * Retrieves conditions from data source according to a {@code Set} of {@code Integer}s
     * that are the IDs of species allowing to filter the conditions to use.
     * <p>
     * The conditions are retrieved and returned as a {@code ConditionTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      allowing to filter the conditions to use.
     * @param attributes    A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                      attributes to populate in the returned {@code ConditionTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              An {@code ConditionTOResultSet} containing all conditions from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public ConditionTOResultSet getConditionsBySpeciesIds(Collection<Integer> speciesIds,
        Collection<Attribute> attributes) throws DAOException;
        
    /**
     * {@code DAOResultSet} specifics to {@code ConditionTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public interface ConditionTOResultSet extends DAOResultSet<ConditionTO> {
    }
    
    /**
     * {@code EntityTO} representing a condition in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public class ConditionTO extends EntityTO<Integer> {

        private static final long serialVersionUID = -1057540315343857464L;


        private Integer exprMappedConditionId;
        private String anatEntityId;
        private String stageId;
        private Integer speciesId;
        
        public ConditionTO(Integer id, Integer exprMappedConditionId, String anatEntityId,
            String stageId, Integer speciesId) {
            super(id);
            this.exprMappedConditionId = exprMappedConditionId;
            this.anatEntityId = anatEntityId;
            this.stageId = stageId;
            this.speciesId = speciesId;
        }
        
        /**
         * @return  The {@code String} that is the condition ID that should be used for insertion
         *          into the expression table: too-granular conditions (e.g., 43 yo human stage,
         *          or sexInferred=1) are mapped to less granular conditions for summary.
         *          Equal to {@code conditionId} if condition is not too granular.
         */
        public Integer getExprMappedConditionId() {
            return exprMappedConditionId;
        }
        /**
         * @return  The {@code String} that is the Uberon anatomical entity ID.
         */
        public String getAnatEntityId() {
            return anatEntityId;
        }
        /**
         * @return  The {@code String} that is the Uberon stage ID.
         */
        public String getStageId() {
            return stageId;
        }
        /**
         * @return  The {@code String} that is the NCBI species taxon ID.
         */
        public Integer getSpeciesId() {
            return speciesId;
        }
        
        @Override
        public String toString() {
            return "ConditionTO [id=" + getId() + ", exprMappedConditionId=" + exprMappedConditionId
                + ", anatEntityId=" + anatEntityId + ", stageId=" + stageId + ", speciesId=" + speciesId + "]";
        }
    }
}
