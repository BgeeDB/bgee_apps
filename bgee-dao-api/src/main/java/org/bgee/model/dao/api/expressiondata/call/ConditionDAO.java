package org.bgee.model.dao.api.expressiondata.call;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.BaseConditionTO;
import org.bgee.model.dao.api.expressiondata.DAODataType;

/**
 * DAO defining queries using or retrieving {@link ConditionTO}s, used for global expression calls
 * (see {@link org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO RawDataConditionDAO}
 * for conditions used in raw data). 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Mar. 2021
 * @since   Bgee 14, Feb. 2017
 * @see ConditionTO
 */
public interface ConditionDAO extends DAO<ConditionDAO.Attribute> {

    /**
     * A {@code String} that represents the ID the root of all anat. entities
     * used in {@code Condition}s in Bgee.
     */
    public final static String ANAT_ENTITY_ROOT_ID = "BGEE:0000000";
    /**
     * A {@code String} that represents the ID the root of all dev. stages
     * used in {@code Condition}s in Bgee.
     */
    public final static String DEV_STAGE_ROOT_ID = "UBERON:0000104";
    /**
     * A {@code String} that represents the ID the root of all cell types
     * used in {@code Condition}s in Bgee.
     */
    public final static String CELL_TYPE_ROOT_ID = "GO:0005575";
    /**
     * A {@code String} that represents the root of all sexes
     * used in {@code Condition}s in Bgee.
     */
    public final static String SEX_ROOT_ID = "any";
    /**
     * A {@code String} that represents the standardized name of the root of all strains
     * used in {@code Condition}s in Bgee.
     */
    public final static String STRAIN_ROOT_ID = "wild-type";

    public enum ConditionParameter {
        ANAT_ENTITY(),
        CELL_TYPE(),
        STAGE(),
        SEX(),
        STRAIN()
    }
    /**
     * {@code Enum} used to define the attributes to populate in the {@code ConditionTO}s 
     * obtained from this {@code ConditionDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ConditionTO#getId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link ConditionTO#getAnatEntityId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link ConditionTO#getStageId()}.
     * <li>{@code CELL_TYPE_ID}: corresponds to {@link ConditionTO#getCellTypeId()}.
     * <li>{@code SEX_ID}: corresponds to {@link ConditionTO#getSex()}.
     * <li>{@code STRAIN_ID}: corresponds to {@link ConditionTO#getStrain()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link ConditionTO#getSpeciesId()}.
     * </ul>
     */
    //XXX: retrieval of ConditionRankInfoTOs associated to a ConditionTO not yet implemented,
    //to be added when needed.
    public enum Attribute implements DAO.Attribute {
        ID("globalConditionId", null, null, false),
        SPECIES_ID("speciesId", null, null, false),
        //The order of the condition parameters is important and is used to generate field names
        ANAT_ENTITY_ID("anatEntityId", "AnatEntity", ANAT_ENTITY_ROOT_ID, true),
        CELL_TYPE_ID("cellTypeId", "CellType", CELL_TYPE_ROOT_ID, true),
        STAGE_ID("stageId", "Stage", DEV_STAGE_ROOT_ID, true),
        SEX_ID("sex", "Sex", SEX_ROOT_ID, true),
        STRAIN_ID("strain", "Strain", STRAIN_ROOT_ID, true);

        public static final List<EnumSet<Attribute>> ALL_COND_PARAM_COMBINATIONS =
                getAllPossibleCondParamCombinations();

        private static final List<EnumSet<Attribute>> getAllPossibleCondParamCombinations() {
            return DAO.getAllPossibleEnumCombinations(Attribute.class, getCondParams());
        }
        public static final List<EnumSet<Attribute>> getAllPossibleCondParamCombinations(
                Collection<Attribute> attrs) {
            return DAO.getAllPossibleEnumCombinations(Attribute.class, attrs);
        }

        public static EnumSet<Attribute> getCondParams() {
            return Arrays.stream(Attribute.values()).filter(a -> a.isConditionParameter())
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
        }
        /**
         * A {@code String} that is the corresponding field name in {@code ConditionTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        private final String fieldNamePart;
        private final String rootId;
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;
        
        private Attribute(String fieldName, String fieldNamePart, String rootId,
                boolean conditionParameter) {
            this.fieldName = fieldName;
            this.fieldNamePart = fieldNamePart;
            this.rootId = rootId;
            this.conditionParameter = conditionParameter;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
        /**
         * @return  A {@code String} that is the substring used in field names
         *          related to this {@code Attribute}.
         */
        public String getFieldNamePart() {
            return this.fieldNamePart;
        }
        /**
         * @return  A {@code String} that is the ID of the root of the ontology for the related
         *          condition parameter, if {@link #isConditionParameter()} returns {@code true}.
         */
        public String getRootId() {
            return this.rootId;
        }
        /**
         * @return  A {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, cell type, sex, strain), 
         *          allowing to determine which condition and expression tables to target 
         *          for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }

    public static class CondParamEnumSetComparator implements Comparator<EnumSet<Attribute>> {
        @Override
        public int compare(EnumSet<Attribute> e1, EnumSet<Attribute> e2) {
            return DAO.compareEnumSets(e1, e2, Attribute.class);
        }
    }
    
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
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure
     *                              the filtering of conditions. If several {@code ConditionFilter}s
     *                              are provided, they are seen as "OR" conditions.
     *                              Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              attributes to populate in the returned {@code ConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @return                      A {@code ConditionTOResultSet} containing the requested conditions
     *                              retrieved from the data source.
     * @throws DAOException If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code conditionParameters} is {@code null}, empty,
     *                                  or one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link Attribute#isConditionParameter()}). 
     */
    public ConditionTOResultSet getGlobalConditions(Collection<Integer> speciesIds,
            Collection<DAOConditionFilter> conditionFilters, Collection<Attribute> attributes) 
            throws DAOException, IllegalArgumentException;

    /**
     * Retrieves global conditions belonging to the provided {@code speciesIds} with parameters defined
     * as specified by {@code DAOConditionFilter2}.
     * <p>
     * The conditions are retrieved and returned as a {@code ConditionTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param conditionFilters      A {@code Collection} of {@code DAOConditionFilter2}s to configure
     *                              the filtering of conditions. If several {@code DAOConditionFilter2}s
     *                              are provided, they are seen as "OR" conditions.
     *                              Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              attributes to populate in the returned {@code ConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated.
     * @return                      A {@code ConditionTOResultSet} containing the requested conditions
     *                              retrieved from the data source.
     * @throws DAOException If an error occurred while accessing the data source.
     */
    public ConditionTOResultSet getGlobalConditions(Collection<DAOConditionFilter2> conditionFilters,
            Collection<Attribute> attributes)
            throws DAOException;

    /**
     * Retrieves global conditions belonging to the provided {@code Collection} of
     * {@code DAOCallFilter}.
     * <p>
     * The conditions are retrieved and returned as a {@code ConditionTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param callFilters      A {@code Collection} of {@code DAOCallFilter}s to configure
     *                         the filtering of conditions. If several {@code DAOCallFilter}s
     *                         are provided, they are seen as "OR" conditions.
     * @param attributes       A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                         attributes to populate in the returned {@code ConditionTO}s.
     *                         If {@code null} or empty, all attributes are populated.
     * @return                 A {@code ConditionTOResultSet} containing the requested conditions
     *                         retrieved from the data source.
     * @throws DAOException If an error occurred while accessing the data source.
     */
    public ConditionTOResultSet getGlobalConditionsFromCallFilters(Collection<DAOCallFilter> callFilters, 
            Collection<Attribute> attributes) throws DAOException;

    public ConditionTOResultSet getGlobalConditionsFromIds(Collection<Integer> condIds,
            Collection<Attribute> attributes) throws DAOException, IllegalArgumentException;

    /**
     * Retrieve the correspondence between raw condition and global conditions, represented as
     * {@code GlobalConditionToRawConditionTO}s.
     * <p>
     * The results are retrieved and returned as a {@code GlobalConditionToRawConditionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     *
     * @param speciesIds                    A {@code Collection} of {@code Integer}s that are the IDs
     *                                      of species allowing to filter the results to retrieve.
     *                                      If {@code null} or empty, results for all species are retrieved.
     * @param conditionParameters           A {@code Collection} of {@code ConditionDAO.Attribute}s
     *                                      defining the condition parameters of the global conditions
     *                                      to retrieve mappings for.
     *                                      (see {@link Attribute#isConditionParameter()}).
     * @return                              A {@code GlobalConditionToRawConditionTOResultSet}
     *                                      allowing to retrieve the requested
     *                                      {@code GlobalConditionToRawConditionTO}s.
     * @throws DAOException
     * @throws IllegalArgumentException
     */
    public GlobalConditionToRawConditionTOResultSet getGlobalCondToRawCondBySpeciesIds(
        Collection<Integer> speciesIds, Collection<Attribute> conditionParameters)
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
     * Retrieve the max ranks and global max ranks over all conditions and over the requested data types
     * for the requested species.
     * Only the attributes returned by {@link ConditionRankInfoTO#getMaxRank()} and
     * {@link ConditionRankInfoTO#getGlobalMaxRank()} are populated in the returned
     * {@code ConditionRankInfoTO}s.
     * <p>
     * Note that these max ranks are used to normalize ranks of expression calls, and to compute
     * a weighted mean rank for each call. So, most of the time, the max weighted mean rank
     * of the calls in a given species over all data types is less than the actual max rank
     * for a given data type, as retrieved in these {@code ConditionRankInfoTO}s.
     *
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are the IDs
     *                                  of the species which we want the max ranks for. If {@code null}
     *                                  or empty, max ranks for all species are returned.
     * @param dataTypes                 A {@code Collection} of {@code DAODataType}s that are the data types
     *                                  to consider when retrieving the max ranks. If {@code null}
     *                                  or empty, all data types are considered.
     * @return                          A {@code Map} where keys are {@code Integer}s representing IDs of species,
     *                                  the associated value being a {@code ConditionRankInfoTO} allowing to retrieve
     *                                  the max rank and global max rank over all conditions,
     *                                  and for the requested data types, in this species.
     * @throws DAOException             If an error occurred when accessing the data source.
     */
    public Map<Integer, ConditionRankInfoTO> getMaxRanks(Collection<Integer> speciesIds,
            Collection<DAODataType> dataTypes) throws DAOException;

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
     * {@code BaseConditionTO} representing a global condition in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 15, Mar. 2021
     * @since   Bgee 14, Feb. 2017
     */
    public class ConditionTO extends BaseConditionTO {
        private final static Logger log = LogManager.getLogger(ConditionTO.class.getName());
        /**
         * {@code EnumDAOField} representing the different sex info that can be used
         * in {@link ConditionTO}s in Bgee.
         *
         * @author Frederic Bastian
         * @version Bgee 15, Mar. 2021
         * @since Bgee 14, Sep. 2018
         */
        public enum DAOSex implements EnumDAOField {
            ANY("any"), HERMAPHRODITE("hermaphrodite"), FEMALE("female"), MALE("male");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code DAOSex}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code DAOSex}.
             */
            private DAOSex(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a sex (for instance,
             * retrieved from a database) into a {@code DAOSex}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code DAOSex}.
             *
             * @param representation    A {@code String} representing a sex.
             * @return                  A {@code DAOSex} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code DAOSex}.
             */
            public static final DAOSex convertToDAOSex(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(DAOSex.class, representation));
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        private static final long serialVersionUID = -1057540315343857464L;

        private final DAOSex sex;
        /**
         * @see #getRankInfoTOs()
         */
        private final Set<ConditionRankInfoTO> rankInfoTOs;
        
        public ConditionTO(Integer id, String anatEntityId, String stageId, String cellTypeId,
                DAOSex sex, String strainId, Integer speciesId,
                Collection<ConditionRankInfoTO> rankInfoTOs) {
            super(id, anatEntityId, stageId, cellTypeId, strainId, speciesId);
            this.sex = sex;
            if (rankInfoTOs != null) {
                this.rankInfoTOs = Collections.unmodifiableSet(new HashSet<>(rankInfoTOs));
            } else {
                this.rankInfoTOs = null;
            }
            //there should be at most one GlobalExpressionCallDataTO per data type.
            //we simply use Collectors.toMap that throws an exception in case of key collision
            if (this.rankInfoTOs != null) {
                this.rankInfoTOs.stream().collect(Collectors.toMap(c -> c.getDataType(), c -> c));
            }
        }

        /**
         * @return  A {@code DAOSex} representing the sex annotated in this {@code ConditionTO}.
         */
        public DAOSex getSex() {
            return sex;
        }
        /**
         * @return  A {@code Set} of {@code ConditionRankInfoTO}s providing information
         *          about max expression rank in this {@code ConditionTO},
         *          one for each of the requested {@code DAODataType}.
         */
        public Set<ConditionRankInfoTO> getRankInfoTOs() {
            return rankInfoTOs;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConditionTO [id=").append(getId())
                   .append(", anatEntityId=").append(getAnatEntityId())
                   .append(", stageId=").append(getStageId())
                   .append(", cellTypeId=").append(getCellTypeId())
                   .append(", sex=").append(getSex())
                   .append(", strainId=").append(getStrainId())
                   .append(", speciesId=").append(getSpeciesId()).append("]");
            return builder.toString();
        }
    }

    /**
     * Allows to store the max gene expression ranks in each global condition, for a specific {@code DAODataType}.
     * Max ranks are computed either by taking into account the conditions itself (see {@link #getMaxRank()}),
     * or all child conditions (see {@link #getGlobalMaxRank()}).
     * <p>
     * Note that these max ranks are used to normalize ranks of expression calls, and to compute
     * a weighted mean rank for each call. So, most of the time, the max weighted mean rank
     * of the calls in a given species over all data types is less than the actual max rank
     * for a given data type, as retrieved in these {@code ConditionRankInfoTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Jun. 2019
     * @since Bgee 14 mar. 2017
     */
    public class ConditionRankInfoTO extends TransferObject {
        private static final long serialVersionUID = 1170648972684653250L;

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

        public ConditionRankInfoTO(BigDecimal maxRank, BigDecimal globalMaxRank) {
            this(null, maxRank, globalMaxRank);
        }
        public ConditionRankInfoTO(DAODataType dataType, BigDecimal maxRank, BigDecimal globalMaxRank) {
            this.dataType = dataType;
            this.maxRank = maxRank;
            this.globalMaxRank = globalMaxRank;
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
            builder.append("ConditionMaxRanksTO [dataType=").append(dataType)
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
                log.traceEntry("{}", stringRepresentation);
                return log.traceExit(GlobalConditionToRawConditionTO.convert(ConditionRelationOrigin.class, 
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
