package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.BaseConditionTO;

/**
 * DAO defining queries using or retrieving {@link RawDataConditionTO}s.
 *
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14, Sep. 2018
 * @see RawDataConditionTO
 */
public interface RawDataConditionDAO extends DAO<RawDataConditionDAO.Attribute> {

    public final static Set<String> NO_INFO_STRAINS = new HashSet<>(Arrays.asList(
            "wild-type", "NA", "not annotated", "confidential_restricted_data",
            // The following were not standardized as of Bgee 15.0, maybe we can remove them later.
            "(Missing)", "mix of breed", "mixed-breed", "multiple breeds"));

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
        ID("conditionId"), EXPR_MAPPED_CONDITION_ID("exprMappedConditionId"),
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
    public RawDataConditionTOResultSet getRawDataConditionsFromSpeciesIds(Collection<Integer> speciesIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieves raw conditions used in data annotations for requested species and condition filters.
     * The condition filters allow to target <strong>raw</strong> conditions used in the
     * <strong>raw</strong> expression table, to return their associated source raw conditions.
     * <p>
     * The conditions are retrieved and returned as a {@code RawDataConditionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results
     * are retrieved.
     *
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
    public RawDataConditionTOResultSet getRawDataConditionsFromRawConditionFilters(
            Collection<DAORawDataConditionFilter> condFilters,
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
    //XXX FB: probably to delete now that speciesIds are integrated in the DAORawDataConditionFilters?
    public RawDataConditionTOResultSet getRawDataConditions(
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
     * @version Bgee 15, Mar. 2021
     * @since   Bgee 14, Sep. 2018
     */
    public class RawDataConditionTO extends BaseConditionTO {
        private final static Logger log = LogManager.getLogger(RawDataConditionTO.class.getName());

        /**
         * {@code EnumDAOField} representing the different sex info that can be used
         * in {@link RawDataConditionTO} in Bgee.
         *
         * @author Frederic Bastian
         * @version Bgee 15 Mar. 2021
         */
        public enum DAORawDataSex implements EnumDAOField {
            NOT_ANNOTATED("not annotated", false), HERMAPHRODITE("hermaphrodite", true),
            FEMALE("female", true), MALE("male", true), MIXED("mixed", false), NA("NA", false);

            private final boolean informative;
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code DAORawDataSex}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code DAORawDataSex}.
             * @param informative           A {@code boolean} defining whether this {@code DAORawDataSex}
             *                              is informative.
             */
            private DAORawDataSex(String stringRepresentation, boolean informative) {
                this.stringRepresentation = stringRepresentation;
                this.informative = informative;
            }

            /**
             * Convert the {@code String} representation of a sex (for instance,
             * retrieved from a database) into a {@code DAORawDataSex}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code DAORawDataSex}.
             *
             * @param representation    A {@code String} representing a sex.
             * @return                  A {@code DAORawDataSex} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code DAORawDataSex}.
             */
            public static final DAORawDataSex convertToDAORawDataSex(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(DAORawDataSex.class, representation));
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            public boolean isInformative() {
                return this.informative;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        private static final long serialVersionUID = -1084999422733426566L;

        private final Integer exprMappedConditionId;
        private final DAORawDataSex sex;
        private final Boolean sexInferred;

        public RawDataConditionTO(Integer id, Integer exprMappedConditionId, String anatEntityId,
                String stageId, String cellTypeId, DAORawDataSex sex, Boolean sexInferred, String strain, 
                Integer speciesId) {
            super(id, anatEntityId, stageId, cellTypeId, strain, speciesId);
            this.exprMappedConditionId = exprMappedConditionId;
            this.sex = sex;
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
         * @return  A {@code DAORawDataSex} representing the sex annotated in this {@code RawDataConditionTO}.
         */
        public DAORawDataSex getSex() {
            return sex;
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