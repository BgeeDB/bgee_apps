package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link ConditionTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Mar. 2017
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
     */
    //XXX: retrieval of GlobalConditionMaxRanksTOs associated to a ConditionTO not yet implemented,
    //to be added when needed.
    public enum Attribute implements DAO.Attribute {
        ID("id", false), EXPR_MAPPED_CONDITION_ID("exprMappedConditionId", false), 
        SPECIES_ID("speciesId", false), 
        ANAT_ENTITY_ID("anatEntityId", true), STAGE_ID("stageId", true);

        /**
         * A {@code String} that is the corresponding field name in {@code AnatEntityTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;
        
        private Attribute(String fieldName, boolean conditionParameter) {
            this.fieldName = fieldName;
            this.conditionParameter = conditionParameter;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
        /**
         * @return  A {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, sex, strain), allowing to determine 
         *          which condition and expression tables to target for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }
    
    /**
     * Retrieves raw conditions used to annotate data and used in the "raw" expression table,
     * where data are not propagated nor precomputed.
     *
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                              allowing to filter the conditions to retrieve. If {@code null}
     *                              or empty, condition for all species are retrieved.
     * @param attributes            A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              attributes to populate in the returned {@code ConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @return
     * @throws DAOException
     */
    public ConditionTOResultSet getRawConditionsBySpeciesIds(Collection<Integer> speciesIds, 
            Collection<Attribute> attributes) throws DAOException;
    
    /**
     * Retrieves global conditions belonging to the provided {@code speciesIds} with parameters defined
     * as specified by {@code conditionParameters}. These global conditions result from
     * the computation of propagated calls according to different condition parameters combinations.
     * For instance, grouping all data related to a same anatomical entity whatever
     * the developmental stage is, or all data in a same anatomical entity - stage whatever the sex is.
     * {@code conditionParameters} defines the condition parameters considered for aggregating the data.
     * A call to {@link Attribute#isConditionParameter()} must return {@code true} for an {@code Attribute}
     * to be accepted in this {@code Collection}.
     * <p>
     * The conditions are retrieved and returned as a {@code ConditionTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                              allowing to filter the conditions to retrieve. If {@code null}
     *                              or empty, condition for all species are retrieved.
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              condition parameters considered for aggregating the expression data
     *                              (see {@link Attribute#isConditionParameter()}).
     *                              It is different from {@code attributes}, because you might want 
     *                              to retrieve, for instance, only anatomical entity IDs, 
     *                              while your expression query was using a stage ID parameter for filtering, 
     *                              and thus the data must have been aggregated by taking stages
     *                              into account.
     * @param attributes            A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              attributes to populate in the returned {@code ConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @return                      An {@code ConditionTOResultSet} containing all conditions 
     *                              from data source.
     * @throws DAOException If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code conditionParameters} is {@code null}, empty,
     *                                  or one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link Attribute#isConditionParameter()}). 
     */
    public ConditionTOResultSet getGlobalConditionsBySpeciesIds(Collection<Integer> speciesIds,
        Collection<Attribute> conditionParameters, Collection<Attribute> attributes) 
            throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve the maximum of global condition IDs, used in the global expression data,
     * pre-computed and propagated.
     * @return                      An {@code int} that is maximum of global condition IDs.
     *                              If there is no condition, return 0.
     * @throws DAOException             If an error occurred when accessing the data source.
     */
    public int getMaxGlobalConditionId() throws DAOException;

    /**
     * Retrieve the max ranks and global max ranks over all conditions and data types.
     * Only the attributes returned by {@link GlobalConditionMaxRankTO#getMaxRank()} and
     * {@link GlobalConditionMaxRankTO#getGlobalMaxRank()} are populated in the returned
     * {@code GlobalConditionMaxRankTO}s.
     * @return                          A {@code GlobalConditionMaxRankTO} allowing to retrieve
     *                                  the max rank and global max rank.
     * @throws DAOException             If an error occurred when accessing the data source.
     */
    public GlobalConditionMaxRankTO getMaxRank() throws DAOException;

    /**
     * Insert into the datasource the provided global {@code ConditionTO}s. These global conditions
     * result from the computation of propagated calls according to different
     * condition parameters combinations. For instance, grouping all data related to
     * a same anatomical entity whatever the developmental stage is, or all data
     * in a same anatomical entity - stage whatever the sex is. Only the condition attributes
     * that were considered for aggregating the data should be set in the provided {@code ConditionTO}s.
     * 
     * @param conditionTOs          A {@code Collection} of {@code ConditionTO}s to be inserted 
     *                              into the datasource.
     * @return                      An {@code int} that is the number of conditions inserted.
     * @throws DAOException If an error occurred while inserting the conditions.
     */
    public int insertGlobalConditions(Collection<ConditionTO> conditionTOs) throws DAOException;
    
    /**
     * Inserts the provided correspondence between raw condition and global conditions 
     * into the data source, represented as a {@code Collection} of {@code GlobalConditionToRawConditionTO}s. 
     *
     * @param globalCondToRawCondTOs    A {@code Collection} of {@code GlobalConditionToRawConditionTO}s
     *                                  to be inserted into the data source.
     * @return                          An {@code int} that is the number of inserted TOs. 
     * @throws DAOException             If an error occurred while trying to insert data.
     * @throws IllegalArgumentException If {@code globalCondToRawCondTOs} is {@code null} or empty.
     */
    public int insertGlobalConditionToRawCondition(
            Collection<GlobalConditionToRawConditionTO> globalCondToRawCondTOs)
                    throws DAOException, IllegalArgumentException;

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
     * It can represent either a "raw" condition or a "global" condition (see
     * {@link #getRawConditionsBySpeciesIds(Collection, Collection)} and
     * {@link #getGlobalConditionsBySpeciesIds(Collection, Collection, Collection)}.
     * 
     * @author  Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14, Mar. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public class ConditionTO extends EntityTO<Integer> {

        private static final long serialVersionUID = -1057540315343857464L;

        private final Integer exprMappedConditionId;
        private final String anatEntityId;
        private final String stageId;
        private final Integer speciesId;
        
        public ConditionTO(Integer id, Integer exprMappedConditionId, String anatEntityId,
            String stageId, Integer speciesId) {
            super(id);
            this.exprMappedConditionId = exprMappedConditionId;
            this.anatEntityId = anatEntityId;
            this.stageId = stageId;
            this.speciesId = speciesId;
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
            StringBuilder builder = new StringBuilder();
            builder.append("ConditionTO [id=").append(getId())
                   .append(", exprMappedConditionId=").append(exprMappedConditionId)
                   .append(", anatEntityId=").append(anatEntityId)
                   .append(", stageId=").append(stageId)
                   .append(", speciesId=").append(speciesId).append("]");
            return builder.toString();
        }
    }

    /**
     * Allows to store the max gene expression ranks in each global condition,
     * whether by taking into account the conditions itself, or all child conditions.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @since Bgee 14 mar. 2017
     */
    public class GlobalConditionMaxRankTO extends TransferObject {
        private static final long serialVersionUID = 1170648972684653250L;

        /**
         * @see #getConditionId()
         */
        private final Integer conditionId;
        /**
         * @see #getDataType()
         */
        private final DAODataType dataType;
        /**
         * @see #getMaxRank()
         */
        private final BigDecimal maxRank;
        /**
         * @see #getGlobalMaxRank()
         */
        private final BigDecimal globalMaxRank;

        public GlobalConditionMaxRankTO(BigDecimal maxRank, BigDecimal globalMaxRank) {
            this(null, null, maxRank, globalMaxRank);
        }
        public GlobalConditionMaxRankTO(Integer conditionId, DAODataType dataType,
                BigDecimal maxRank, BigDecimal globalMaxRank) {
            this.conditionId = conditionId;
            this.dataType = dataType;
            this.maxRank = maxRank;
            this.globalMaxRank = globalMaxRank;
        }

        /**
         * @return  An {@code Integer} that is the ID of the global condition which the max ranks
         *          are related to.
         */
        public Integer getConditionId() {
            return conditionId;
        }
        /**
         * @return  A {@code DAODataType} that is the data type considered to compute
         *          the max ranks.
         */
        public DAODataType getDataType() {
            return dataType;
        }
        /**
         * @return  A {@code BigDecimal} that is the max rank observed by this data type
         *          in this condition, without considering child conditions.
         */
        public BigDecimal getMaxRank() {
            return maxRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the max rank observed by this data type
         *          in this condition, taking also into account all child conditions.
         */
        public BigDecimal getGlobalMaxRank() {
            return globalMaxRank;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConditionMaxRanksTO [conditionId=").append(conditionId)
                   .append(", dataType=").append(dataType)
                   .append(", maxRank=").append(maxRank)
                   .append(", globalMaxRank=").append(globalMaxRank).append("]");
            return builder.toString();
        }
    }

    /**
     * {@code DAOResultSet} specifics to {@code GlobalConditionToRawConditionTO}s
     *
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    public interface GlobalConditionToRawConditionTOResultSet
                    extends DAOResultSet<GlobalConditionToRawConditionTO> {
    }

    /**
     * A {@code TransferObject} representing a relation between a globalCondition and
     * one of the raw conditions considered when aggregating the data in the related globalCondition.
     * <p>
     * This class defines a raw condition ID (see {@link #getConditionId()}
     * and a global condition ID (see {@link #getGlobalConditionId()}), and also stores
     * the origin of the relations (association from sub-conditions or parent conditions
     * or from the same condition, see {@link #getCondtionRelationOrigin()}).
     *
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @since Bgee 14 Mar. 2017
     */
    //TODO: add related method in TOComparator
    public static class GlobalConditionToRawConditionTO extends TransferObject {
        private final static Logger log = LogManager.getLogger(GlobalConditionToRawConditionTO.class.getName());
        private static final long serialVersionUID = -553628358149907274L;

        public enum ConditionRelationOrigin implements TransferObject.EnumDAOField {
            SELF("self"), DESCENDANT("descendant"), PARENT("parent");

            /**
             * The {@code String} representation of the enum.
             */
            private String stringRepresentation;
            /**
             * Constructor
             * @param stringRepresentation the {@code String} representation of the enum.
             */
            ConditionRelationOrigin(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            @Override
            public String getStringRepresentation() {
                return stringRepresentation;
            }
            /**
             * Return the mapped {@link ConditionRelationOrigin} from a string representation.
             * @param stringRepresentation A string representation
             * @return The corresponding {@code ConditionRelationOrigin}
             * @see org.bgee.model.dao.api.TransferObject.EnumDAOField#convert(Class, String)
             */
            public static ConditionRelationOrigin convertToCondRelOrigin(String stringRepresentation){
                log.entry(stringRepresentation);
                return log.exit(GlobalConditionToRawConditionTO.convert(ConditionRelationOrigin.class, 
                        stringRepresentation));
            }
        }

        /**
         * A {@code Integer} representing the ID of the raw condition.
         */
        private final Integer rawConditionId;
        /**
         * A {@code Integer} representing the ID of the global condition.
         */
        private final Integer globalConditionId;
        /**
         * A {@code ConditionRelationOrigin} representing the origin of the association.
         */
        private final ConditionRelationOrigin conditionRelationOrigin;

        /**
         * Constructor providing the condition ID (see {@link #getRawConditionId()}) and
         * the global condition ID (see {@link #getGlobalConditionId()}).
         *
         * @param rawExpressionId           An {@code Integer} that is the ID of the raw condition.
         * @param globalExpressionId        An {@code Integer} that is the ID of the global condition.
         * @param conditionRelationOrigin   An {@code ConditionRelationOrigin} representing
         *                                  the origin of the association.
         **/
        public GlobalConditionToRawConditionTO(Integer rawConditionId, Integer globalConditionId,
                ConditionRelationOrigin conditionRelationOrigin) {
            super();
            this.rawConditionId = rawConditionId;
            this.globalConditionId = globalConditionId;
            this.conditionRelationOrigin = conditionRelationOrigin;
        }

        /**
         * @return  the {@code Integer} representing the ID of the raw condition.
         */
        public Integer getRawConditionId() {
            return rawConditionId;
        }
        /**
         * @return  the {@code Integer} representing the ID of the global condition.
         */
        public Integer getGlobalConditionId() {
            return globalConditionId;
        }
        /**
         * @return  {@code ConditionRelationOrigin} representing the origin of the association.
         */
        public ConditionRelationOrigin getConditionRelationOrigin() {
            return conditionRelationOrigin;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GlobalConditionToRawConditionTO [rawConditionId=").append(rawConditionId)
                    .append(", globalConditionId=").append(globalConditionId)
                    .append(", conditionRelationOrigin=").append(conditionRelationOrigin).append("]");
            return builder.toString();
        }
    }
}
