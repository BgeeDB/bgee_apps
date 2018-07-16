package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
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
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;

/**
 * DAO defining queries using or retrieving {@link GlobalExpressionCallTO}s, 
 * with all data integrated and pre-computed.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 14 Feb. 2017
 */
public interface GlobalExpressionCallDAO extends DAO<GlobalExpressionCallDAO.Attribute> {
    
    public enum Attribute implements DAO.Attribute {
        ID, BGEE_GENE_ID, CONDITION_ID, MEAN_RANK,
        DATA_TYPE_OBSERVED_DATA,
        DATA_TYPE_EXPERIMENT_TOTAL_COUNTS, DATA_TYPE_EXPERIMENT_SELF_COUNTS,
        DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS, DATA_TYPE_RANK_INFO;
    }
    /**
     * The attributes available to order retrieved {@code GlobalExpressionCallTO}s
     * <ul>
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link GlobalExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code PUBLIC_GENE_ID}: orders by public gene IDs rather than internal gene IDs (slower query).
     * <li>{@code CONDITION_ID}: corresponds to {@link GlobalExpressionCallTO#getConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: order by the anat. entity ID used in the conditions of the calls.
     * <li>{@code STAGE_ID}: order by the dev. stage ID used in the conditions of the calls.
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
    enum OrderingAttribute implements DAO.OrderingAttribute {
        BGEE_GENE_ID, PUBLIC_GENE_ID, CONDITION_ID, ANAT_ENTITY_ID, STAGE_ID, OMA_GROUP_ID, MEAN_RANK;
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
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link ConditionDAO.Attribute#isConditionParameter()}).
     * @param attributes            A {@code Collection} of {@code GlobalExpressionCallDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code GlobalExpressionCallTO}s. If {@code null} or empty, 
     *                              all attributes are populated.
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are
     *                              {@code GlobalExpressionCallDAO.OrderingAttribute}s defining
     *                              the attributes used to order the returned {@code GlobalExpressionCallTO}s,
     *                              the associated value being a {@code DAO.Direction}
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed.
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
            Collection<CallDAOFilter> callFilters, Collection<ConditionDAO.Attribute> conditionParameters,
            Collection<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException;

    /**
     * Retrieve the maximum of global expression IDs.
     *
     * @return                      An {@code int} that is maximum of expression IDs.
     *                              If there is no call, return 0.
     * @throws DAOException             If an error occurred when accessing the data source.
     */
    public int getMaxGlobalExprId() throws DAOException;

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
     * {@code RawExpressionCallTO} representing a global expression call in the Bgee database 
     * (global expression calls are computed by propagating all data, and have additional columns 
     * as compared to {@code RawExpressionCallTO}s).
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public static class GlobalExpressionCallTO extends RawExpressionCallTO {

        private static final long serialVersionUID = -1057540315343857464L;
        
        private final BigDecimal meanRank;
        
        private final Set<GlobalExpressionCallDataTO> callDataTOs;
        
        public GlobalExpressionCallTO(Integer id, Integer bgeeGeneId, Integer conditionId,
                BigDecimal meanRank, Set<GlobalExpressionCallDataTO> callDataTOs) {
            super(id, bgeeGeneId, conditionId);
            
            this.meanRank = meanRank;
            this.callDataTOs = callDataTOs;
            //there should be at most one GlobalExpressionCallDataTO per data type.
            //we simply use Collectors.toMap that throws an exception in case of key collision
            if (this.callDataTOs != null) {
                this.callDataTOs.stream().collect(Collectors.toMap(c -> c.getDataType(), c -> c));
            }
        }

        /**
         * @return  A {@code BigDecimal} that is the weighted mean rank of the gene in the condition, 
         *          based on the normalized mean rank of each data type requested in the query. 
         *          So for instance, if you configured an {@code ExpressionCallDAOFilter} 
         *          to only retrieved Affymetrix data, then this rank will be equal to the rank 
         *          returned by {@link #getAffymetrixMeanRank()}.
         */
        public BigDecimal getMeanRank() {
            return meanRank;
        }
        /**
         * @return  A {@code Set} of {@code GlobalExpressionCallDataTO}s storing the data supporting this call,
         *          one for each of the requested
         *          {@link org.bgee.model.dao.api.expressiondata.DAODataType DAODataType}s.
         */
        public Set<GlobalExpressionCallDataTO> getCallDataTOs() {
            return callDataTOs;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GlobalExpressionCallTO [id=").append(getId())
                   .append(", bgeeGeneId=").append(getBgeeGeneId())
                   .append(", conditionId=").append(getConditionId())
                   .append(", meanRank=").append(meanRank)
                   .append(", callDataTOs=").append(callDataTOs)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * This {@code TransferObject} stores the supporting data of {@link GlobalExpressionCallTO}s
     * from a specific {@link org.bgee.model.dao.api.expressiondata.DAODataType DAODataType}.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Mar. 2017
     * @see GlobalExpressionCallTO
     * @since Bgee 14 Mar. 2017
     */
    public static class GlobalExpressionCallDataTO extends TransferObject {
        private final static Logger log = LogManager.getLogger(GlobalExpressionCallDataTO.class.getName());
        private static final long serialVersionUID = -3316700982321337127L;

        private final DAODataType dataType;

        private final Boolean conditionObservedData;

        private final Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation;

        private final Set<DAOExperimentCount> experimentCounts;

        private final Integer propagatedCount;

        private final BigDecimal rank;
        private final BigDecimal rankNorm;
        private final BigDecimal weightForMeanRank;

        public GlobalExpressionCallDataTO(DAODataType dataType, Boolean conditionObservedData,
                Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation,
                Set<DAOExperimentCount> experimentCounts, Integer propagatedCount,
                BigDecimal rank, BigDecimal rankNorm, BigDecimal weightForMeanRank) {

            if (dataPropagation.keySet().stream().anyMatch(a -> !a.isConditionParameter())) {
                throw log.throwing(new IllegalArgumentException("Invalid condition parameters: "
                        + dataPropagation.keySet()));
            }
            this.dataType = dataType;
            this.conditionObservedData = conditionObservedData;
            this.dataPropagation = dataPropagation == null? null: Collections.unmodifiableMap(new HashMap<>(dataPropagation));

            this.experimentCounts = experimentCounts == null? null: Collections.unmodifiableSet(new HashSet<>(experimentCounts));
            this.propagatedCount = propagatedCount;

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

        public Set<DAOExperimentCount> getExperimentCounts() {
            return experimentCounts;
        }
        public Integer getPropagatedCount() {
            return propagatedCount;
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
                   .append(", experimentCounts=").append(experimentCounts)
                   .append(", propagatedCount=").append(propagatedCount)
                   .append(", rank=").append(rank)
                   .append(", rankNorm=").append(rankNorm)
                   .append(", weightForMeanRank=").append(weightForMeanRank)
                   .append("]");
            return builder.toString();
        }
    }
}
