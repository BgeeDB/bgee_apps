package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;

/**
 * DAO defining queries using or retrieving {@link GlobalExpressionCallTO}s, 
 * with all data integrated and pre-computed.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14 Feb. 2017
 */
public interface GlobalExpressionCallDAO extends DAO<GlobalExpressionCallDAO.Attribute> {

    public interface CanBeDataTypeDependent {
        public boolean isDataTypeDependant();
    }
    public enum Attribute implements DAO.Attribute, CanBeDataTypeDependent {
//        //As of Bgee 15.0, there is no longer a globalExpressionId field.
//        ID(false),
        BGEE_GENE_ID(false, false), GLOBAL_CONDITION_ID(false, false), MEAN_RANK(true, true),
        DATA_TYPE_RANK_INFO(true, true), DATA_TYPE_OBSERVED_DATA(false, true),
        FDR_P_VALUE_COND_INFO(false, true), FDR_P_VALUE_DESCENDANT_COND_INFO(false, true);

        private final boolean requireExtraGlobalCondInfo;
        private final boolean dataTypeDependant;

        private Attribute(boolean requireExtraGlobalCondInfo, boolean dataTypeDependant) {
            this.requireExtraGlobalCondInfo = requireExtraGlobalCondInfo;
            this.dataTypeDependant = dataTypeDependant;
        }
        public boolean isRequireExtraGlobalCondInfo() {
            return requireExtraGlobalCondInfo;
        }
        /**
         * @return  {@code true} if this {@code Attribute} corresponds to different results
         *          depending on a data type selection, {@code false} otherwise.
         *          For instance, computation of the mean expression rank, or of the FDR
         *          from aggregated p-values, is data type dependent.
         * @see AttributeInfo
         */
        @Override
        public boolean isDataTypeDependant() {
            return dataTypeDependant;
        }
    }
    /**
     * The attributes available to order retrieved {@code GlobalExpressionCallTO}s
     * <ul>
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link GlobalExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code PUBLIC_GENE_ID}: orders by public gene IDs rather than internal gene IDs (slower query).
     * <li>{@code CONDITION_ID}: corresponds to {@link GlobalExpressionCallTO#getConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: order by the anat. entity ID used in the conditions of the calls.
     * <li>{@code STAGE_ID}: order by the dev. stage ID used in the conditions of the calls.
     * <li>{@code CELL_TYPE_ID}: order by the cell type ID used in the conditions of the calls.
     * <li>{@code SEX_ID}: order by the sex ID used in the conditions of the calls.
     * <li>{@code STRAIN_ID}: order by the strain ID used in the conditions of the calls.
     * <li>{@code OMA_GROUP_ID}: order results by the OMA group genes belong to. 
     * If this {@code OrderingAttribute} is used in a query not specifying any targeted taxon 
     * for gene orthology, then the {@code OMAParentNodeId} of the gene is used (see 
     * {@link org.bgee.model.dao.api.gene.GeneDAO.GeneTO#getOMAParentNodeId()}); otherwise, 
     * the OMA group the gene belongs to at the level of the targeted taxon is used. 
     * <li>{@code MEAN_RANK}: Corresponds to {@link GlobalExpressionCallTO#getMeanRank()}. 
     * Order results by mean rank of the gene in the corresponding condition. 
     * Only the mean ranks computed from the data types requested in the query are considered. 
     * </ul>
     */
    enum OrderingAttribute implements DAO.OrderingAttribute, CanBeDataTypeDependent {
        BGEE_GENE_ID("bgeeGeneId", false, false, false), PUBLIC_GENE_ID("geneId", false, true, false),
        OMA_GROUP_ID("OMAParentNodeId", false, true, false),
        GLOBAL_CONDITION_ID("globalConditionId", false, false, false),
        ANAT_ENTITY_ID("anatEntityId", true, false, false), STAGE_ID("stageId", true, false, false),
        CELL_TYPE_ID("cellTypeId", true, false, false), SEX_ID("sex", true, false, false),
        STRAIN_ID("strain", true, false, false), MEAN_RANK("meanRank", true, false, true);

        private final String fieldName;
        private final boolean requireExtraGlobalCondInfo;
        private final boolean requireExtraGeneInfo;
        private final boolean dataTypeDependant;

        private OrderingAttribute(String fieldName, boolean requireExtraGlobalCondInfo,
                boolean requireExtraGeneInfo, boolean dataTypeDependant) {
            this.fieldName = fieldName;
            this.requireExtraGlobalCondInfo = requireExtraGlobalCondInfo;
            this.requireExtraGeneInfo = requireExtraGeneInfo;
            this.dataTypeDependant = dataTypeDependant;
        }
        public String getFieldName() {
            return fieldName;
        }
        public boolean isRequireExtraGlobalCondInfo() {
            return requireExtraGlobalCondInfo;
        }
        public boolean isRequireExtraGeneInfo() {
            return requireExtraGeneInfo;
        }
        /**
         * @return  {@code true} if this {@code OrderingAttribute} corresponds to different results
         *          depending on a data type selection, {@code false} otherwise.
         *          For instance, ordering based of the mean expression rank, or of the FDR
         *          from aggregated p-values, is data type dependent.
         * @see OrderingAttributeInfo
         */
        @Override
        public boolean isDataTypeDependant() {
            return dataTypeDependant;
        }
    }

    /**
     * A class allowing to associate an {@code Attribute}, specifying the information to retrieve
     * in an expression query, to the {@code DAODataType}s this {@code Attribute} is requested for.
     * Specifying {@code DAODataType}s is only needed if the {@code Attribute} is dependent on
     * a data type selection (see {@link Attribute#isDataTypeDependent()}).
     *
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 15.0, Apr. 2021
     */
    public static class AttributeInfo extends GenericAttributeInfo<Attribute> {
        /**
         * To instantiate an {@code AttributeInfo} independent from a data type selection.
         *
         * @param attribute An {@code Attribute} for which {@link Attribute#isDataTypeDependent()}
         *                  returns {@code false}.
         * @throws IllegalArgumentException If calling {@link Attribute#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code true}.
         * @see Attribute#isDataTypeDependent()
         */
        public AttributeInfo(Attribute attribute) {
            super(attribute);
        }
        /**
         * To instantiate an {@code AttributeInfo} dependent on a data type selection.
         *
         * @param attribute         An {@code Attribute} for which {@link Attribute#isDataTypeDependent()}
         *                          returns {@code true}.
         * @param targetedDataTypes A {@code Collection} of {@code DAODataType}s specifying
         *                          the data types for which {@code attribute} is requested.
         *                          If {@code null} or empty, all {@code DAODataType}s
         *                          are considered.
         * @throws IllegalArgumentException If calling {@link Attribute#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code false}.
         * @see Attribute#isDataTypeDependent()
         */
        public AttributeInfo(Attribute attribute, Collection<DAODataType> targetedDataTypes) {
            super(attribute, targetedDataTypes);
        }
    }
    /**
     * A class allowing to associate an {@code OrderingAttribute}, specifying how to sort
     * the results from an expression query, to the {@code DAODataType}s this {@code OrderingAttribute}
     * is requested for. Specifying {@code DAODataType}s is only needed if the {@code OrderingAttribute}
     * is dependent on a data type selection (see {@link OrderingAttribute#isDataTypeDependent()}).
     *
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 15.0, Apr. 2021
     */
    public static class OrderingAttributeInfo extends GenericAttributeInfo<OrderingAttribute> {
        /**
         * To instantiate an {@code OrderingAttributeInfo} independent from a data type selection.
         *
         * @param attribute An {@code OrderingAttribute} for which
         *                  {@link OrderingAttribute#isDataTypeDependent()} returns {@code false}.
         * @throws IllegalArgumentException If calling {@link OrderingAttribute#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code true}.
         * @see OrderingAttribute#isDataTypeDependent()
         */
        public OrderingAttributeInfo(OrderingAttribute attribute) {
            super(attribute);
        }
        /**
         * To instantiate an {@code OrderingAttributeInfo} dependent on a data type selection.
         *
         * @param attribute         An {@code OrderingAttribute} for which
         *                          {@link OrderingAttribute#isDataTypeDependent()} returns {@code true}.
         * @param targetedDataTypes A {@code Collection} of {@code DAODataType}s specifying
         *                          the data types for which {@code attribute} is requested.
         *                          If {@code null} or empty, all {@code DAODataType}s
         *                          are considered.
         * @throws IllegalArgumentException If calling {@link OrderingAttribute#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code false}.
         * @see OrderingAttribute#isDataTypeDependent()
         */
        public OrderingAttributeInfo(OrderingAttribute attribute,
                Collection<DAODataType> targetedDataTypes) {
            super(attribute, targetedDataTypes);
        }
    }
    /**
     * A class allowing to associate a {@code CanBeDataTypeDependent}, specifying the information
     * to retrieve in an expression query, to the {@code DAODataType}s this {@code CanBeDataTypeDependent}
     * is requested for. Specifying {@code DAODataType}s is only needed if
     * the {@code CanBeDataTypeDependent} is dependent on a data type selection
     * (see {@link CanBeDataTypeDependent#isDataTypeDependent()}).
     *
     * @param <T>   A class that is of types {@code Enum<T>} and {@code CanBeDataTypeDependent}
     *              ({@code Attribute} or {@code OrderingAttribute}).
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 15.0, Apr. 2021
     */
    public static class GenericAttributeInfo<T extends Enum<T> & CanBeDataTypeDependent> {
        private final T attribute;
        private final EnumSet<DAODataType> targetedDataTypes;

        /**
         * To instantiate a {@code GenericAttributeInfo} independent from a data type selection.
         *
         * @param attribute A {@code CanBeDataTypeDependent} for which
         *                  {@link CanBeDataTypeDependent#isDataTypeDependent()} returns {@code false}.
         * @throws IllegalArgumentException If calling {@link CanBeDataTypeDependent#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code true}.
         * @see CanBeDataTypeDependent#isDataTypeDependent()
         */
        public GenericAttributeInfo(T attribute) {
            this(attribute, false, null);
        }
        /**
         * To instantiate an {@code GenericAttributeInfo} dependent on a data type selection.
         *
         * @param attribute         An {@code CanBeDataTypeDependent} for which
         *                          {@link CanBeDataTypeDependent#isDataTypeDependent()}
         *                          returns {@code true}.
         * @param targetedDataTypes A {@code Collection} of {@code DAODataType}s specifying
         *                          the data types for which {@code attribute} is requested.
         *                          If {@code null} or empty, all {@code DAODataType}s
         *                          are considered.
         * @throws IllegalArgumentException If calling {@link CanBeDataTypeDependent#isDataTypeDependent()}
         *                                  on {@code attribute} returns {@code false}.
         * @see CanBeDataTypeDependent#isDataTypeDependent()
         */
        public GenericAttributeInfo(T attribute, Collection<DAODataType> targetedDataTypes) {
            this(attribute, true, targetedDataTypes);
        }
        private GenericAttributeInfo(T attribute, boolean shouldBeDataTypeDependent,
                Collection<DAODataType> targetedDataTypes) {
            if (attribute == null) {
                throw new IllegalArgumentException("Attribute cannot be null.");
            }
            if (shouldBeDataTypeDependent != attribute.isDataTypeDependant()) {
                throw new IllegalArgumentException(
                        "Incorrect definition of data type selection for Attribute: " + attribute);
            }
            this.attribute = attribute;
            this.targetedDataTypes = targetedDataTypes == null || targetedDataTypes.isEmpty()?
                    EnumSet.allOf(DAODataType.class): EnumSet.copyOf(targetedDataTypes);
        }
        public T getAttribute() {
            return attribute;
        }
        public EnumSet<DAODataType> getTargetedDataTypes() {
            //Defensive copying, no Collections.unmodifiableEnumSet
            return EnumSet.copyOf(targetedDataTypes);
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
            result = prime * result + ((targetedDataTypes == null) ? 0 : targetedDataTypes.hashCode());
            return result;
        }
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
            GenericAttributeInfo<?> other = (GenericAttributeInfo<?>) obj;
            if (attribute != other.attribute) {
                return false;
            }
            if (targetedDataTypes == null) {
                if (other.targetedDataTypes != null) {
                    return false;
                }
            } else if (!targetedDataTypes.equals(other.targetedDataTypes)) {
                return false;
            }
            return true;
        }
    }

    /** 
     * Retrieves global calls from data source in the appropriate table specified by 
     * {@code conditionParameters}.
     * <p>
     * The global calls are retrieved and returned as a {@code GlobalExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
     * 
     * @param callFilters           A {@code Collection} of {@code CallDAOFilter}s, 
     *                              allowing to configure this query. If several 
     *                              {@code CallDAOFilter}s are provided, they are seen 
     *                              as "OR" conditions. Can be {@code null} or empty.
     * @param attributes            A {@code Collection} of {@code GlobalExpressionCallDAO.AttributeInfo}s
     *                              defining the attributes to populate in the returned 
     *                              {@code GlobalExpressionCallTO}s, associated to the requested
     *                              {@code DAODataType}s if necessary. If {@code null} or empty,
     *                              all attributes are populated, with all {@code DAODataType}s
     *                              when applicable.
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are
     *                              {@code GlobalExpressionCallDAO.OrderingAttributeInfo}s defining
     *                              the attributes used to order the returned {@code GlobalExpressionCallTO}s,
     *                              the associated value being a {@code DAO.Direction}
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, no ordering is performed.
     * @return                      A {@code GlobalExpressionCallTOResultSet} containing global
     *                              calls from data source according to {@code attributes} and
     *                              {@code conditionParameters}.
     * @throws DAOException         If an error occurred when accessing the data source. 
     * @throws IllegalArgumentException If {@code callFilters} is {@code null} or empty,
     *                                  or if one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<AttributeInfo> attributes,
            LinkedHashMap<OrderingAttributeInfo, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException;

    /**
     * Obtains the min. and max ranks of genes. For now, to retrieve ranks it should be queried only
     * EXPRESSED calls with min quality BRONZE, in all dev. stages and all anat. entities,
     * only from observed calls, with anat. entity and dev. stage condition parameters. But we don't do any check
     * on this here, to let the possibility to the user to query with different parameters.
     * <p>
     * In the {@code EntityMinMaxRanksTO}s obtained from the returned {@code EntityMinMaxRanksTOResultSet}:
     * <ul>
     * <li>{@link EntityMinMaxRanksTO#getId()} will return the Bgee gene ID as {@code Integer}
     * <li>the species ID will not be provided ({@link EntityMinMaxRanksTO#getSpeciesId()} returns {@code null}),
     * since the Bgee gene ID are unique over all species (we do not need the species ID to distinguish
     * different Bgee gene IDs).
     * </ul>
     *
     * @param dataTypes             A {@code Collection} of {@code DAODataType}s that are
     *                              the data types to consider to compute ranks. If {@code null}
     *                              or empty, all data types are considered.
     * @param callFilters           A {@code Collection} of {@code CallDAOFilter}s,
     *                              allowing to configure this query. If several
     *                              {@code CallDAOFilter}s are provided, they are seen
     *                              as "OR" conditions. Can be {@code null} or empty.
     * @return                      A {@code EntityMinMaxRanksTOResultSet} allowing to retrieve
     *                              the requested {@code EntityMinMaxRanksTO}s.
     * @throws DAOException             If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code callFilters} is {@code null} or empty,
     *                                  or if one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public EntityMinMaxRanksTOResultSet<Integer> getMinMaxRanksPerGene(
            Collection<DAODataType> dataTypes, Collection<CallDAOFilter> callFilters)
                    throws DAOException, IllegalArgumentException;
    /**
     * Obtains the min. and max ranks of anatomical entities. For now, to retrieve ranks it should be queried only
     * EXPRESSED calls with min quality BRONZE, in all dev. stages and for all genes,
     * only from observed calls, with anat. entity and dev. stage condition parameters. But we don't do any check
     * on this here, to let the possibility to the user to query with different parameters.
     * <p>
     * In the {@code EntityMinMaxRanksTO}s obtained from the returned {@code EntityMinMaxRanksTOResultSet}:
     * <ul>
     * <li>{@link EntityMinMaxRanksTO#getId()} will return the anatomical entity ID as {@code String}
     * <li>the species ID is provided ({@link EntityMinMaxRanksTO#getSpeciesId()} returns a non-{@code null} value),
     * since a same anatomical entity ID can be used in several species.
     *
     * @param dataTypes             A {@code Collection} of {@code DAODataType}s that are
     *                              the data types to consider to compute ranks. If {@code null}
     *                              or empty, all data types are considered.
     * @param callFilters           A {@code Collection} of {@code CallDAOFilter}s,
     *                              allowing to configure this query. If several
     *                              {@code CallDAOFilter}s are provided, they are seen
     *                              as "OR" conditions. Can be {@code null} or empty.
     * @return                      A {@code EntityMinMaxRanksTOResultSet} allowing to retrieve
     *                              the requested {@code EntityMinMaxRanksTO}s.
     * @throws DAOException             If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code callFilters} is {@code null} or empty,
     *                                  or if one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public EntityMinMaxRanksTOResultSet<String> getMinMaxRanksPerAnatEntity(
            Collection<DAODataType> dataTypes, Collection<CallDAOFilter> callFilters)
                    throws DAOException, IllegalArgumentException;
    /**
     * Retrieve the maximum of global expression IDs.
     *
     * @return                      An {@code int} that is maximum of expression IDs.
     *                              If there is no call, return 0.
     * @throws DAOException             If an error occurred when accessing the data source.
     */
    public long getMaxGlobalExprId() throws DAOException;

    /**
     * Insert into the datasource the provided {@code GlobalExpressionCallTO}s.
     * 
     * @param callTOs               A {@code Collection} of {@code GlobalExpressionCallTO}s to be inserted 
     *                              into the datasource.
     * @throws DAOException If an error occurred while inserting the conditions.
     * @throws IllegalArgumentException If {@code callTOs} is {@code null} or empty.
     */
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs)
            throws DAOException, IllegalArgumentException;
    
    /**
     * {@code DAOResultSet} specifics to {@code GlobalExpressionCallTO}s
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public interface GlobalExpressionCallTOResultSet extends DAOResultSet<GlobalExpressionCallTO> {
    }

    /**
     * {@code DAOResultSet} specifics to {@code EntityMinMaxRanksTO}s
     *
     * @author  Frederic Bastian
     * @version Bgee 14, Feb. 2019
     * @since   Bgee 14, Feb. 2019
     *
     * @param <T> The type of ID of the returned {@code EntityMinMaxRanksTO}s
     */
    public interface EntityMinMaxRanksTOResultSet<T extends Comparable<T>> extends DAOResultSet<EntityMinMaxRanksTO<T>> {
    }
    
    /**
     * {@code RawExpressionCallTO} representing a global expression call in the Bgee database 
     * (global expression calls are computed by propagating all data, and have additional columns 
     * as compared to {@code RawExpressionCallTO}s).
     * 
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 14, Feb. 2017
     */
    public static class GlobalExpressionCallTO extends RawExpressionCallTO {

        private static final long serialVersionUID = -1057540315343857464L;
        
        private final Set<DAOMeanRank> meanRanks;
        
        private final Set<GlobalExpressionCallDataTO> callDataTOs;
        
        private final Set<DAOFDRPValue> pValues;
        
        private final Set<DAOFDRPValue> bestDescendantPValues;
        
        public GlobalExpressionCallTO(Long id, Integer bgeeGeneId, Integer conditionId,
                Collection<DAOMeanRank> meanRanks, Collection<GlobalExpressionCallDataTO> callDataTOs,
                Collection<DAOFDRPValue> pValues, Collection<DAOFDRPValue> bestDescendantPValues) {
            super(id, bgeeGeneId, conditionId);
            
            this.meanRanks = meanRanks == null? null:
                Collections.unmodifiableSet(new HashSet<>(meanRanks));
            if (callDataTOs != null) {
                this.callDataTOs = Collections.unmodifiableSet(new HashSet<>(callDataTOs));
            } else {
                this.callDataTOs = null;
            }
            if (pValues != null) {
                assert pValues.stream().noneMatch(p -> p.getConditionId() != null);
                this.pValues = Collections.unmodifiableSet(new HashSet<>(pValues));
            } else {
                this.pValues = null;
            }
            if (bestDescendantPValues != null) {
                this.bestDescendantPValues = Collections.unmodifiableSet(new HashSet<>(bestDescendantPValues));
            } else {
                this.bestDescendantPValues = null;
            }
            //there should be at most one GlobalExpressionCallDataTO per data type.
            //we simply use Collectors.toMap that throws an exception in case of key collision
            if (this.callDataTOs != null) {
                this.callDataTOs.stream().collect(Collectors.toMap(c -> c.getDataType(), c -> c));
            }
        }

        /**
         * @return  A {@code Set} of {@code DAOMeanRank}s storing the weighted mean rank
         *          of a gene in a condition, associated to the {@code DAODataType}s used
         *          to compute it.
         */
        public Set<DAOMeanRank> getMeanRanks() {
            return meanRanks;
        }
        /**
         * @return  An unmodifiable {@code Set} of {@code GlobalExpressionCallDataTO}s
         *          storing the data supporting this call, one for each of the requested
         *          {@link org.bgee.model.dao.api.expressiondata.DAODataType DAODataType}s.
         */
        public Set<GlobalExpressionCallDataTO> getCallDataTOs() {
            return callDataTOs;
        }
        /**
         * @return  An unmodifiable {@code Set} of {@code DAOFDRPValue}s
         *          storing the pvalues for all possible combination of datatypes
         */
        public Set<DAOFDRPValue> getPValues() {
            return pValues;
        }
        /**
         * @return  An unmodifiable {@code Set} of {@code DAOFDRPValue}s storing the 
         *          pvalues of best descendant calls for all possible combination of datatypes
         */
        public Set<DAOFDRPValue> getBestDescendantPValues() {
            return bestDescendantPValues;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GlobalExpressionCallTO [id=").append(getId())
                   .append(", bgeeGeneId=").append(getBgeeGeneId())
                   .append(", conditionId=").append(getConditionId())
                   .append(", meanRanks=").append(meanRanks)
                   .append(", callDataTOs=").append(callDataTOs)
                   .append(", pValues=").append(pValues)
                   .append(", bestDescendantPValues=").append(bestDescendantPValues)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * This {@code TransferObject} stores the supporting data of {@link GlobalExpressionCallTO}s
     * from a specific {@link org.bgee.model.dao.api.expressiondata.DAODataType DAODataType}.
     * 
     * @author Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @see GlobalExpressionCallTO
     * @since Bgee 14 Mar. 2017
     */
    public static class GlobalExpressionCallDataTO extends TransferObject {
        private final static Logger log = LogManager.getLogger(GlobalExpressionCallDataTO.class.getName());
        private static final long serialVersionUID = -3316700982321337127L;

        private final DAODataType dataType;

        private final Boolean conditionObservedData;
        private final Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation;

        private final Integer selfObservationCount;
        private final Integer descendantObservationCount;
        private final BigDecimal fdrPValue;
        private final BigDecimal bestDescendantFDRPValue;

        private final BigDecimal rank;
        private final BigDecimal rankNorm;
        private final BigDecimal weightForMeanRank;

        public GlobalExpressionCallDataTO(DAODataType dataType, Boolean conditionObservedData,
                Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation,
                Integer selfObservationCount, Integer descendantObservationCount,
                BigDecimal fdrPValue, BigDecimal bestDescendantFDRPValue,
                BigDecimal rank, BigDecimal rankNorm, BigDecimal weightForMeanRank) {

            if (dataPropagation != null && dataPropagation.keySet().stream().anyMatch(a -> !a.isConditionParameter())) {
                throw log.throwing(new IllegalArgumentException("Invalid condition parameters: "
                        + dataPropagation.keySet()));
            }
            this.dataType = dataType;
            this.conditionObservedData = conditionObservedData;
            this.dataPropagation = dataPropagation == null? null: Collections.unmodifiableMap(new HashMap<>(dataPropagation));

            this.selfObservationCount = selfObservationCount;
            this.descendantObservationCount = descendantObservationCount;
            this.fdrPValue = fdrPValue;
            this.bestDescendantFDRPValue = bestDescendantFDRPValue;

            this.rank = rank;
            this.rankNorm = rankNorm;
            this.weightForMeanRank = weightForMeanRank;
        }

        public DAODataType getDataType() {
            return dataType;
        }
        /**
         * @return  A {@code Boolean} defining whether the call was observed in the condition.
         *          This is independent from {@link #getDataPropagation()},
         *          because even if a data aggregation have produced only SELF propagation states,
         *          we cannot have the guarantee that data were actually observed in the condition
         *          by looking at these independent propagation states.
         */
        public Boolean isConditionObservedData() {
            return conditionObservedData;
        }
        /**
         * @return  A {@code Map} where keys are {@code ConditionDAO.Attribute}s that are
         *          condition parameters (see {@link ConditionDAO.Attribute#isConditionParameter()}),
         *          the associated value being a {@code DAOPropagationState} indicating where the call
         *          originated from in that condition parameter
         *          (for instance, data observed in a given anatomical entity).
         */
        public Map<ConditionDAO.Attribute, DAOPropagationState> getDataPropagation() {
            return dataPropagation;
        }

        /**
         * @return  An {@code Integer} that is the number of observations producing a p-value
         *          in the condition itself. ({@link #getSelfObservationCount()} +
         *          {@link #getDescendantObservationCount()}) allows to retrieve the total number
         *          of p-values used to compute the FDR-corrected p-value returned by
         *          {@link #getFDRPValue()}.
         */
        public Integer getSelfObservationCount() {
            return selfObservationCount;
        }
        /**
         * @return  An {@code Integer} that is the number of observations producing a p-value
         *          in the descendant conditions of the requested condition.
         *          ({@link #getSelfObservationCount()} + {@link #getDescendantObservationCount()})
         *          allows to retrieve the total number of p-values used to compute
         *          the FDR-corrected p-value returned by {@link #getFDRPValue()}.
         */
        public Integer getDescendantObservationCount() {
            return descendantObservationCount;
        }
        /**
         * @return  A {@code BigDecimal} that is the FDR corrected p-value computed from
         *          all the p-values obtained by this data type in a condition
         *          and all its sub-conditions for a gene.
         */
        public BigDecimal getFDRPValue() {
            return fdrPValue;
        }
        /**
         * @return  A {@code BigDecimal} that is the best FDR corrected p-value obtained by
         *          this data type among the sub-conditions of the condition of a call for a gene.
         */
        public BigDecimal getBestDescendantFDRPValue() {
            return bestDescendantFDRPValue;
        }

        public BigDecimal getRank() {
            return rank;
        }
        public BigDecimal getRankNorm() {
            return rankNorm;
        }
        public BigDecimal getWeightForMeanRank() {
            return weightForMeanRank;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GlobalExpressionCallDataTO [dataType=").append(dataType)
                   .append(", dataPropagation=").append(dataPropagation)
                   .append(", selfObservationCount=").append(selfObservationCount)
                   .append(", descendantObservationCount=").append(descendantObservationCount)
                   .append(", fdrPValue=").append(fdrPValue)
                   .append(", bestDescendantFDRPValue=").append(descendantObservationCount)
                   .append(", rank=").append(rank)
                   .append(", rankNorm=").append(rankNorm)
                   .append(", weightForMeanRank=").append(weightForMeanRank)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * An {@code EntityTO} to store min. and max ranks associated to an entity (such as a gene, or an anat. entity).
     *
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2019
     * @since Bgee 14 Feb. 2019
     *
     * @param <T> The type of ID of this {@code EntityTO}
     */
    public static class EntityMinMaxRanksTO<T extends Comparable<T>> extends EntityTO<T> {
        private static final long serialVersionUID = -4260272894290918736L;

        private final BigDecimal minRank;
        private final BigDecimal maxRank;
        private final Integer speciesId;

        public EntityMinMaxRanksTO(T entityId, BigDecimal minRank, BigDecimal maxRank, Integer speciesId) {
            super(entityId);
            this.minRank = minRank;
            this.maxRank = maxRank;
            this.speciesId = speciesId;
        }
        public BigDecimal getMinRank() {
            return minRank;
        }
        public BigDecimal getMaxRank() {
            return maxRank;
        }
        public Integer speciesId() {
            return speciesId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("EntityMinMaxRanksTO [entityId=").append(this.getId())
                   .append(", minRank=").append(minRank)
                   .append(", maxRank=").append(maxRank)
                   .append(", speciesId=").append(speciesId)
                   .append("]");
            return builder.toString();
        }
    }
}