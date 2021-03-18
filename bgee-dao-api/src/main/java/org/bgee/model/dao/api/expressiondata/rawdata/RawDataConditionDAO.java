package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.BaseConditionTO;

/**
 * DAO defining queries using or retrieving {@link ConditionTO}s.
 *
 * @author  Frederic Bastian
 * @version Bgee 14, Sep. 2018
 * @since   Bgee 14, Sep. 2018
 * @see RawDataConditionTO
 */
public interface RawDataConditionDAO extends DAO<RawDataConditionDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawDataConditionTO}s
     * obtained from this {@code RawDataConditionDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RawDataConditionTO#getId()}.
     * <li>{@code EXPR_MAPPED_CONDITION_ID}: corresponds to {@link RawDataConditionTO#getExprMappedConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link RawDataConditionTO#getAnatEntityId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link RawDataConditionTO#getStageId()}.
     * <li>{@code CELL_TYPE_ID}: corresponds to {@link RawDataConditionTO#getCellTypeId()}.
     * <li>{@code SEX}: corresponds to {@link RawDataConditionTO#getSex()}.
     * <li>{@code SEX_INFERRED}: corresponds to {@link RawDataConditionTO#getSexInferred()}.
     * <li>{@code STRAIN}: corresponds to {@link RawDataConditionTO#getStrain()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link RawDataConditionTO#getSpeciesId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), EXPR_MAPPED_CONDITION_ID("exprMappedConditionId"),
        ANAT_ENTITY_ID("anatEntityId"), STAGE_ID("stageId"), CELL_TYPE_ID("cellTypeId"), SEX("sex"), 
        SEX_INFERRED("sexInferred"), STRAIN("strain"), SPECIES_ID("speciesId");

        /**
         * A {@code String} that is the corresponding field name in {@code RawDataConditionTO} class.
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
     * Retrieves raw conditions used in data annotations for requested species.
     * <p>
     * The conditions are retrieved and returned as a {@code RawDataConditionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species
     *                              allowing to filter the conditions to retrieve. If {@code null}
     *                              or empty, condition for all species are retrieved.
     * @param attributes            A {@code Collection} of {@code RawDataConditionDAO.Attribute}s defining
     *                              the attributes to populate in the returned {@code RawDataConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated.
     * @return                      A {@code RawDataConditionTOResultSet} containing the requested
     *                              raw data conditions retrieved from the data source.
     * @throws DAOException         If an error occurred while accessing the data source.
     */
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIds(Collection<Integer> speciesIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieves raw conditions used in data annotations for requested species and condition filters.
     * The condition filters allow to target <strong>global</strong> conditions used in the
     * <strong>global</strong> expression table, to return their associated source raw conditions.
     * <p>
     * The conditions are retrieved and returned as a {@code RawDataConditionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species
     *                          allowing to filter the conditions to retrieve. If {@code null}
     *                          or empty, condition for all species are retrieved.
     * @param condFilters       A {@code Collection} of {@code DAORawDataConditionFilter}s
     *                          allowing to specify the <strong>global</strong> conditions
     *                          that should be considered to retrieve the associated <strong>raw</strong>
     *                          conditions.
     * @param attributes        A {@code Collection} of {@code RawDataConditionDAO.Attribute}s defining
     *                          the attributes to populate in the returned {@code RawDataConditionTO}s.
     *                          If {@code null} or empty, all attributes are populated.
     * @return                  A {@code RawDataConditionTOResultSet} containing the requested
     *                          raw data conditions retrieved from the data source.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RawDataConditionTOResultSet getRawDataConditionsBySpeciesIdsAndConditionFilters(
            Collection<Integer> speciesIds, Collection<DAORawDataConditionFilter> condFilters,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code RawDataConditionTO}s
     *
     * @author  Frederic Bastian
     * @version Bgee 14, Sep. 2018
     * @since   Bgee 14, Sep. 2018
     */
    public interface RawDataConditionTOResultSet extends DAOResultSet<RawDataConditionTO> {
    }

    /**
     * {@code BaseConditionTO} representing a raw data condition in the Bgee database.
     *
     * @author Frederic Bastian
     * @version Bgee 14, Sep. 2018
     * @since   Bgee 14, Sep. 2018
     */
    public class RawDataConditionTO extends BaseConditionTO {
        private static final long serialVersionUID = -1084999422733426566L;

        private final Integer exprMappedConditionId;
        private final Boolean sexInferred;

        public RawDataConditionTO(Integer id, Integer exprMappedConditionId, String anatEntityId,
                String stageId, String cellTypeId, DAOSex sex, Boolean sexInferred, String strain, 
                Integer speciesId) {
            super(id, anatEntityId, stageId, cellTypeId, sex, strain, speciesId);
            this.exprMappedConditionId = exprMappedConditionId;
            this.sexInferred = sexInferred;
        }

        /**
         * @return  The {@code Integer} that is the condition ID that should be used for insertion
         *          into the expression table: too-granular conditions (e.g., 43 yo human stage,
         *          or sexInferred=1) are mapped to less granular conditions for summary.
         *          Equal to {@code #getId()} if condition is not too granular.
         */
        public Integer getExprMappedConditionId() {
            return exprMappedConditionId;
        }
        /**
         * @return  A {@code Boolean} specifying whether sex information was retrieved from annotation (false),
         *          or inferred from information in Uberon (true). Note that all conditions
         *          used in the expression tables use a "0" value.
         */
        public Boolean getSexInferred() {
            return sexInferred;
        }
    }
}