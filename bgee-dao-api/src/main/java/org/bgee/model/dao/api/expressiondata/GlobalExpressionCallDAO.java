package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;

/**
 * DAO defining queries using or retrieving {@link GlobalExpressionCallTO}s, 
 * with all data integrated and pre-computed. Also allows to insert and retrieve 
 * {@code GlobalExpressionToRawExpressionTO}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
 * @since   Bgee 14 Feb. 2017
 */
public interface GlobalExpressionCallDAO extends DAO<GlobalExpressionCallDAO.Attribute> {
    
    public enum Attribute implements DAO.Attribute {
        ID(false, false), BGEE_GENE_ID(false, false), CONDITION_ID(false, false), 
        AFFYMETRIX_EXP_PRESENT_HIGH_SELF_COUNT(true, false),
        AFFYMETRIX_EXP_PRESENT_LOW_SELF_COUNT(true, false), 
        AFFYMETRIX_EXP_ABSENT_HIGH_SELF_COUNT(true, false),
        AFFYMETRIX_EXP_ABSENT_LOW_SELF_COUNT(true, false), 
        AFFYMETRIX_EXP_PRESENT_HIGH_DESCENDANT_COUNT(false, false),
        AFFYMETRIX_EXP_PRESENT_LOW_DESCENDANT_COUNT(false, false), 
        AFFYMETRIX_EXP_ABSENT_HIGH_PARENT_COUNT(false, false),
        AFFYMETRIX_EXP_ABSENT_LOW_PARENT_COUNT(false, false), 
        AFFYMETRIX_EXP_PRESENT_HIGH_TOTAL_COUNT(false, true),
        AFFYMETRIX_EXP_PRESENT_LOW_TOTAL_COUNT(false, true), 
        AFFYMETRIX_EXP_ABSENT_HIGH_TOTAL_COUNT(false, true),
        AFFYMETRIX_EXP_ABSENT_LOW_TOTAL_COUNT(false, true), 
        AFFYMETRIX_EXP_PROPAGATED_COUNT(false, false),
        RNA_SEQ_EXP_PRESENT_HIGH_SELF_COUNT(true, false), 
        RNA_SEQ_EXP_PRESENT_LOW_SELF_COUNT(true, false),
        RNA_SEQ_EXP_ABSENT_HIGH_SELF_COUNT(true, false), 
        RNA_SEQ_EXP_ABSENT_LOW_SELF_COUNT(true, false),
        RNA_SEQ_EXP_PRESENT_HIGH_DESCENDANT_COUNT(false, false), 
        RNA_SEQ_EXP_PRESENT_LOW_DESCENDANT_COUNT(false, false),
        RNA_SEQ_EXP_ABSENT_HIGH_PARENT_COUNT(false, false), 
        RNA_SEQ_EXP_ABSENT_LOW_PARENT_COUNT(false, false),
        RNA_SEQ_EXP_PRESENT_HIGH_TOTAL_COUNT(false, true), 
        RNA_SEQ_EXP_PRESENT_LOW_TOTAL_COUNT(false, true),
        RNA_SEQ_EXP_ABSENT_HIGH_TOTAL_COUNT(false, true), 
        RNA_SEQ_EXP_ABSENT_LOW_TOTAL_COUNT(false, true),
        RNA_SEQ_EXP_PROPAGATED_COUNT(false, false), 
        EST_LIB_PRESENT_HIGH_SELF_COUNT(true, false),
        EST_LIB_PRESENT_LOW_SELF_COUNT(true, false), 
        EST_LIB_PRESENT_HIGH_DESCENDANT_COUNT(false, false),
        EST_LIB_PRESENT_LOW_DESCENDANT_COUNT(false, false), 
        EST_LIB_PRESENT_HIGH_TOTAL_COUNT(false, true),
        EST_LIB_PRESENT_LOW_TOTAL_COUNT(false, true),
        EST_LIB_PROPAGATED_COUNT(false, false), 
        IN_SITU_EXP_PRESENT_HIGH_SELF_COUNT(true, false),
        IN_SITU_EXP_PRESENT_LOW_SELF_COUNT(true, false), 
        IN_SITU_EXP_ABSENT_HIGH_SELF_COUNT(true, false),
        IN_SITU_EXP_ABSENT_LOW_SELF_COUNT(true, false), 
        IN_SITU_EXP_PRESENT_HIGH_DESCENDANT_COUNT(false, false),
        IN_SITU_EXP_PRESENT_LOW_DESCENDANT_COUNT(false, false), 
        IN_SITU_EXP_ABSENT_HIGH_PARENT_COUNT(false, false),
        IN_SITU_EXP_ABSENT_LOW_PARENT_COUNT(false, false), 
        IN_SITU_EXP_PRESENT_HIGH_TOTAL_COUNT(false, true),
        IN_SITU_EXP_PRESENT_LOW_TOTAL_COUNT(false, true), 
        IN_SITU_EXP_ABSENT_HIGH_TOTAL_COUNT(false, true),
        IN_SITU_EXP_ABSENT_LOW_TOTAL_COUNT(false, true), 
        IN_SITU_EXP_PROPAGATED_COUNT(false, false),
        GLOBAL_MEAN_RANK(false, false),
        AFFYMETRIX_MEAN_RANK(false, false), RNA_SEQ_MEAN_RANK(false, false), 
        EST_RANK(false, false), IN_SITU_RANK(false, false), 
        AFFYMETRIX_MEAN_RANK_NORM(false, false), RNA_SEQ_MEAN_RANK_NORM(false, false), 
        EST_RANK_NORM(false, false), IN_SITU_RANK_NORM(false, false), 
        AFFYMETRIX_DISTINCT_RANK_SUM(false, false), 
        RNA_SEQ_DISTINCT_RANK_SUM(false, false);
        
        private final boolean selfAttribute;
        private final boolean totalAttribute;
        
        private Attribute(boolean selfAttribute, boolean totalAttribute) {
            this.selfAttribute = selfAttribute;
            this.totalAttribute = totalAttribute;
            assert !(selfAttribute && totalAttribute);
        }
        
        public boolean isSelfAttribute() {
            return selfAttribute;
        }
        public boolean isTotalAttribute() {
            return totalAttribute;
        }
    }
    /**
     * The attributes available to order retrieved {@code GlobalExpressionCallTO}s
     * <ul>
     * <li>{@code GENE_ID}: corresponds to {@link GlobalExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link GlobalExpressionCallTO#getConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: order by the anat. entity ID used in the conditions of the calls.
     * <li>{@code STAGE_ID}: order by the dev. stage ID used in the conditions of the calls.
     * <li>{@code OMA_GROUP_ID}: order results by the OMA group genes belong to. 
     * If this {@code OrderingAttribute} is used in a query not specifying any targeted taxon 
     * for gene orthology, then the {@code OMAParentNodeId} of the gene is used (see 
     * {@link org.bgee.model.dao.api.gene.GeneDAO.GeneTO#getOMAParentNodeId()}); otherwise, 
     * the OMA group the gene belongs to at the level of the targeted taxon is used. 
     * <li>{@code MEAN_RANK}: Corresponds to {@link GlobalExpressionCallTO#getGlobalMeanRank()}. 
     * Order results by mean rank of the gene in the corresponding condition. 
     * Only the mean ranks computed from the data types requested in the query are considered. 
     * </ul>
     */
    enum OrderingAttribute implements DAO.OrderingAttribute {
        GENE_ID, CONDITION_ID, ANAT_ENTITY_ID, STAGE_ID, OMA_GROUP_ID, MEAN_RANK;
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
     * @param callDataFilters       A {@code Collection} of {@code CallDataDAOFilter}s to configure
     *                              the filtering based on experiment expression counts. If several 
     *                              {@code CallDAOFilter}s are provided, they are seen 
     *                              as "AND" conditions. Can be {@code null} or empty.
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
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<CallDataDAOFilter> callDataFilters,
            Collection<ConditionDAO.Attribute> conditionParameters,
            Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes)
                throws DAOException, IllegalArgumentException;

    /**
     * Retrieve the maximum of global expression IDs in the appropriate table specified by {@code conditionParameters}.
     * 
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link ConditionDAO.Attribute#isConditionParameter()}).
     * @return                      An {@code int} that is maximum of expression IDs in the appropriate table.
     *                              If there is no call, return 0.
     * @throws DAOException             If an error occurred when accessing the data source. 
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public int getMaxGlobalExprId(Collection<ConditionDAO.Attribute> conditionParameters) 
            throws DAOException, IllegalArgumentException;

    /**
     * Insert into the datasource the provided {@code GlobalExpressionCallTO}s. Which expression table 
     * should be targeted will be determined by {@code conditionParameters}. 
     * 
     * @param callTOs               A {@code Collection} of {@code GlobalExpressionCallTO}s to be inserted 
     *                              into the datasource.
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link ConditionDAO.Attribute#isConditionParameter()}).
     * @return                      An {@code int} that is the number of calls inserted.
     * @throws DAOException If an error occurred while inserting the conditions.
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs, 
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException;
    
    /**
     * Inserts the provided correspondence between raw expression and global expression calls 
     * into the data source, represented as a {@code Collection} of {@code GlobalExpressionToRawExpressionTO}s. 
     * 
     * @param globalExprToRawExprTOs    A {@code Collection} of {@code GlobalExpressionToRawExpressionTO}s
     *                                  to be inserted into the data source.
     * @param conditionParameters       A {@code Collection} of {@code ConditionDAO.Attribute}s 
     *                                  defining the combination of condition parameters 
     *                                  that were requested for queries, allowing to determine 
     *                                  which condition and expression tables to target
     *                                  (see {@link ConditionDAO.Attribute#isConditionParameter()}).
     * @return                          An {@code int} that is the number of inserted TOs. 
     * @throws DAOException             If an error occurred while trying to insert data.
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link ConditionDAO.Attribute#isConditionParameter()}).
     */
    public int insertGlobalExpressionToRawExpression(
            Collection<GlobalExpressionToRawExpressionTO> globalExprToRawExprTOs, 
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException;
    
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
    public class GlobalExpressionCallTO extends RawExpressionCallTO {

        private static final long serialVersionUID = -1057540315343857464L;
        
        private final BigDecimal globalMeanRank;
        
        private final Integer affymetrixExpPresentHighSelfCount;
        private final Integer affymetrixExpPresentLowSelfCount;
        private final Integer affymetrixExpAbsentHighSelfCount;
        private final Integer affymetrixExpAbsentLowSelfCount;
        private final Integer affymetrixExpPresentHighDescendantCount;
        private final Integer affymetrixExpPresentLowDescendantCount;
        private final Integer affymetrixExpAbsentHighParentCount;
        private final Integer affymetrixExpAbsentLowParentCount;
        private final Integer affymetrixExpPresentHighTotalCount;
        private final Integer affymetrixExpPresentLowTotalCount;
        private final Integer affymetrixExpAbsentHighTotalCount;
        private final Integer affymetrixExpAbsentLowTotalCount;
        private final Integer affymetrixExpPropagatedCount;
        
        private final Integer rnaSeqExpPresentHighSelfCount;
        private final Integer rnaSeqExpPresentLowSelfCount;
        private final Integer rnaSeqExpAbsentHighSelfCount;
        private final Integer rnaSeqExpAbsentLowSelfCount;
        private final Integer rnaSeqExpPresentHighDescendantCount;
        private final Integer rnaSeqExpPresentLowDescendantCount;
        private final Integer rnaSeqExpAbsentHighParentCount;
        private final Integer rnaSeqExpAbsentLowParentCount;
        private final Integer rnaSeqExpPresentHighTotalCount;
        private final Integer rnaSeqExpPresentLowTotalCount;
        private final Integer rnaSeqExpAbsentHighTotalCount;
        private final Integer rnaSeqExpAbsentLowTotalCount;
        private final Integer rnaSeqExpPropagatedCount;
        
        private final Integer estLibPresentHighSelfCount;
        private final Integer estLibPresentLowSelfCount;
        private final Integer estLibPresentHighDescendantCount;
        private final Integer estLibPresentLowDescendantCount;
        private final Integer estLibPresentHighTotalCount;
        private final Integer estLibPresentLowTotalCount;
        private final Integer estLibPropagatedCount;
        
        private final Integer inSituExpPresentHighSelfCount;
        private final Integer inSituExpPresentLowSelfCount;
        private final Integer inSituExpAbsentHighSelfCount;
        private final Integer inSituExpAbsentLowSelfCount;
        private final Integer inSituExpPresentHighDescendantCount;
        private final Integer inSituExpPresentLowDescendantCount;
        private final Integer inSituExpAbsentHighParentCount;
        private final Integer inSituExpAbsentLowParentCount;
        private final Integer inSituExpPresentHighTotalCount;
        private final Integer inSituExpPresentLowTotalCount;
        private final Integer inSituExpAbsentHighTotalCount;
        private final Integer inSituExpAbsentLowTotalCount;
        private final Integer inSituExpPropagatedCount;
        
        public GlobalExpressionCallTO(Integer id, Integer bgeeGeneId, Integer conditionId,
                BigDecimal globalMeanRank,
                Integer affymetrixExpPresentHighSelfCount, Integer affymetrixExpPresentLowSelfCount, 
                Integer affymetrixExpAbsentHighSelfCount, Integer affymetrixExpAbsentLowSelfCount, 
                Integer affymetrixExpPresentHighDescendantCount, 
                Integer affymetrixExpPresentLowDescendantCount, 
                Integer affymetrixExpAbsentHighParentCount, Integer affymetrixExpAbsentLowParentCount, 
                Integer affymetrixExpPresentHighTotalCount, Integer affymetrixExpPresentLowTotalCount, 
                Integer affymetrixExpAbsentHighTotalCount, Integer affymetrixExpAbsentLowTotalCount, 
                Integer affymetrixExpPropagatedCount, Integer rnaSeqExpPresentHighSelfCount, 
                Integer rnaSeqExpPresentLowSelfCount, Integer rnaSeqExpAbsentHighSelfCount, 
                Integer rnaSeqExpAbsentLowSelfCount, Integer rnaSeqExpPresentHighDescendantCount, 
                Integer rnaSeqExpPresentLowDescendantCount, Integer rnaSeqExpAbsentHighParentCount, 
                Integer rnaSeqExpAbsentLowParentCount, Integer rnaSeqExpPresentHighTotalCount, 
                Integer rnaSeqExpPresentLowTotalCount, Integer rnaSeqExpAbsentHighTotalCount, 
                Integer rnaSeqExpAbsentLowTotalCount, Integer rnaSeqExpPropagatedCount, 
                Integer estLibPresentHighSelfCount, Integer estLibPresentLowSelfCount, 
                Integer estLibPresentHighDescendantCount, Integer estLibPresentLowDescendantCount, 
                Integer estLibPresentHighTotalCount, Integer estLibPresentLowTotalCount, 
                Integer estLibPropagatedCount, Integer inSituExpPresentHighSelfCount, 
                Integer inSituExpPresentLowSelfCount, Integer inSituExpAbsentHighSelfCount, 
                Integer inSituExpAbsentLowSelfCount, Integer inSituExpPresentHighDescendantCount, 
                Integer inSituExpPresentLowDescendantCount, Integer inSituExpAbsentHighParentCount, 
                Integer inSituExpAbsentLowParentCount, Integer inSituExpPresentHighTotalCount, 
                Integer inSituExpPresentLowTotalCount, Integer inSituExpAbsentHighTotalCount, 
                Integer inSituExpAbsentLowTotalCount, Integer inSituExpPropagatedCount, 
                BigDecimal affymetrixMeanRank, BigDecimal rnaSeqMeanRank, BigDecimal estRank,
                BigDecimal inSituRank, BigDecimal affymetrixMeanRankNorm,
                BigDecimal rnaSeqMeanRankNorm, BigDecimal estRankNorm, BigDecimal inSituRankNorm,
                BigDecimal affymetrixDistinctRankSum, BigDecimal rnaSeqDistinctRankSum) {
            
            super(id, bgeeGeneId, conditionId, affymetrixMeanRank, rnaSeqMeanRank, estRank,
                  inSituRank, affymetrixMeanRankNorm, rnaSeqMeanRankNorm, estRankNorm,
                  inSituRankNorm, affymetrixDistinctRankSum, rnaSeqDistinctRankSum);
            
            this.globalMeanRank = globalMeanRank;
            
            this.affymetrixExpPresentHighSelfCount = affymetrixExpPresentHighSelfCount;
            this.affymetrixExpPresentLowSelfCount = affymetrixExpPresentLowSelfCount;
            this.affymetrixExpAbsentHighSelfCount = affymetrixExpAbsentHighSelfCount;
            this.affymetrixExpAbsentLowSelfCount = affymetrixExpAbsentLowSelfCount;
            this.affymetrixExpPresentHighDescendantCount = affymetrixExpPresentHighDescendantCount;
            this.affymetrixExpPresentLowDescendantCount = affymetrixExpPresentLowDescendantCount;
            this.affymetrixExpAbsentHighParentCount = affymetrixExpAbsentHighParentCount;
            this.affymetrixExpAbsentLowParentCount = affymetrixExpAbsentLowParentCount;
            this.affymetrixExpPresentHighTotalCount = affymetrixExpPresentHighTotalCount;
            this.affymetrixExpPresentLowTotalCount = affymetrixExpPresentLowTotalCount;
            this.affymetrixExpAbsentHighTotalCount = affymetrixExpAbsentHighTotalCount;
            this.affymetrixExpAbsentLowTotalCount = affymetrixExpAbsentLowTotalCount;
            this.affymetrixExpPropagatedCount = affymetrixExpPropagatedCount;
            
            this.rnaSeqExpPresentHighSelfCount = rnaSeqExpPresentHighSelfCount;
            this.rnaSeqExpPresentLowSelfCount = rnaSeqExpPresentLowSelfCount;
            this.rnaSeqExpAbsentHighSelfCount = rnaSeqExpAbsentHighSelfCount;
            this.rnaSeqExpAbsentLowSelfCount = rnaSeqExpAbsentLowSelfCount;
            this.rnaSeqExpPresentHighDescendantCount = rnaSeqExpPresentHighDescendantCount;
            this.rnaSeqExpPresentLowDescendantCount = rnaSeqExpPresentLowDescendantCount;
            this.rnaSeqExpAbsentHighParentCount = rnaSeqExpAbsentHighParentCount;
            this.rnaSeqExpAbsentLowParentCount = rnaSeqExpAbsentLowParentCount;
            this.rnaSeqExpPresentHighTotalCount = rnaSeqExpPresentHighTotalCount;
            this.rnaSeqExpPresentLowTotalCount = rnaSeqExpPresentLowTotalCount;
            this.rnaSeqExpAbsentHighTotalCount = rnaSeqExpAbsentHighTotalCount;
            this.rnaSeqExpAbsentLowTotalCount = rnaSeqExpAbsentLowTotalCount;
            this.rnaSeqExpPropagatedCount = rnaSeqExpPropagatedCount;
            
            this.estLibPresentHighSelfCount = estLibPresentHighSelfCount;
            this.estLibPresentLowSelfCount = estLibPresentLowSelfCount;
            this.estLibPresentHighDescendantCount = estLibPresentHighDescendantCount;
            this.estLibPresentLowDescendantCount = estLibPresentLowDescendantCount;
            this.estLibPresentHighTotalCount = estLibPresentHighTotalCount;
            this.estLibPresentLowTotalCount = estLibPresentLowTotalCount;
            this.estLibPropagatedCount = estLibPropagatedCount;
            
            this.inSituExpPresentHighSelfCount = inSituExpPresentHighSelfCount;
            this.inSituExpPresentLowSelfCount = inSituExpPresentLowSelfCount;
            this.inSituExpAbsentHighSelfCount = inSituExpAbsentHighSelfCount;
            this.inSituExpAbsentLowSelfCount = inSituExpAbsentLowSelfCount;
            this.inSituExpPresentHighDescendantCount = inSituExpPresentHighDescendantCount;
            this.inSituExpPresentLowDescendantCount = inSituExpPresentLowDescendantCount;
            this.inSituExpAbsentHighParentCount = inSituExpAbsentHighParentCount;
            this.inSituExpAbsentLowParentCount = inSituExpAbsentLowParentCount;
            this.inSituExpPresentHighTotalCount = inSituExpPresentHighTotalCount;
            this.inSituExpPresentLowTotalCount = inSituExpPresentLowTotalCount;
            this.inSituExpAbsentHighTotalCount = inSituExpAbsentHighTotalCount;
            this.inSituExpAbsentLowTotalCount = inSituExpAbsentLowTotalCount;
            this.inSituExpPropagatedCount = inSituExpPropagatedCount;
        }

        /**
         * @return  A {@code BigDecimal} that is the weighted mean rank of the gene in the condition, 
         *          based on the normalized mean rank of each data type requested in the query. 
         *          So for instance, if you configured an {@code ExpressionCallDAOFilter} 
         *          to only retrieved Affymetrix data, then this rank will be equal to the rank 
         *          returned by {@link #getAffymetrixMeanRank()}.
         */
        public BigDecimal getGlobalMeanRank() {
            return globalMeanRank;
        }

        public Integer getAffymetrixExpPresentHighSelfCount() {
            return affymetrixExpPresentHighSelfCount;
        }
        public Integer getAffymetrixExpPresentLowSelfCount() {
            return affymetrixExpPresentLowSelfCount;
        }
        public Integer getAffymetrixExpAbsentHighSelfCount() {
            return affymetrixExpAbsentHighSelfCount;
        }
        public Integer getAffymetrixExpAbsentLowSelfCount() {
            return affymetrixExpAbsentLowSelfCount;
        }
        public Integer getAffymetrixExpPresentHighDescendantCount() {
            return affymetrixExpPresentHighDescendantCount;
        }
        public Integer getAffymetrixExpPresentLowDescendantCount() {
            return affymetrixExpPresentLowDescendantCount;
        }
        public Integer getAffymetrixExpAbsentHighParentCount() {
            return affymetrixExpAbsentHighParentCount;
        }
        public Integer getAffymetrixExpAbsentLowParentCount() {
            return affymetrixExpAbsentLowParentCount;
        }
        public Integer getAffymetrixExpPresentHighTotalCount() {
            return affymetrixExpPresentHighTotalCount;
        }
        public Integer getAffymetrixExpPresentLowTotalCount() {
            return affymetrixExpPresentLowTotalCount;
        }
        public Integer getAffymetrixExpAbsentHighTotalCount() {
            return affymetrixExpAbsentHighTotalCount;
        }
        public Integer getAffymetrixExpAbsentLowTotalCount() {
            return affymetrixExpAbsentLowTotalCount;
        }
        public Integer getAffymetrixExpPropagatedCount() {
            return affymetrixExpPropagatedCount;
        }
        public Integer getRNASeqExpPresentHighSelfCount() {
            return rnaSeqExpPresentHighSelfCount;
        }
        public Integer getRNASeqExpPresentLowSelfCount() {
            return rnaSeqExpPresentLowSelfCount;
        }
        public Integer getRNASeqExpAbsentHighSelfCount() {
            return rnaSeqExpAbsentHighSelfCount;
        }
        public Integer getRNASeqExpAbsentLowSelfCount() {
            return rnaSeqExpAbsentLowSelfCount;
        }
        public Integer getRNASeqExpPresentHighDescendantCount() {
            return rnaSeqExpPresentHighDescendantCount;
        }
        public Integer getRNASeqExpPresentLowDescendantCount() {
            return rnaSeqExpPresentLowDescendantCount;
        }
        public Integer getRNASeqExpAbsentHighParentCount() {
            return rnaSeqExpAbsentHighParentCount;
        }
        public Integer getRNASeqExpAbsentLowParentCount() {
            return rnaSeqExpAbsentLowParentCount;
        }
        public Integer getRNASeqExpPresentHighTotalCount() {
            return rnaSeqExpPresentHighTotalCount;
        }
        public Integer getRNASeqExpPresentLowTotalCount() {
            return rnaSeqExpPresentLowTotalCount;
        }
        public Integer getRNASeqExpAbsentHighTotalCount() {
            return rnaSeqExpAbsentHighTotalCount;
        }
        public Integer getRNASeqExpAbsentLowTotalCount() {
            return rnaSeqExpAbsentLowTotalCount;
        }
        public Integer getRNASeqExpPropagatedCount() {
            return rnaSeqExpPropagatedCount;
        }
        public Integer getESTLibPresentHighSelfCount() {
            return estLibPresentHighSelfCount;
        }
        public Integer getESTLibPresentLowSelfCount() {
            return estLibPresentLowSelfCount;
        }
        public Integer getESTLibPresentHighDescendantCount() {
            return estLibPresentHighDescendantCount;
        }
        public Integer getESTLibPresentLowDescendantCount() {
            return estLibPresentLowDescendantCount;
        }
        public Integer getESTLibPresentHighTotalCount() {
            return estLibPresentHighTotalCount;
        }
        public Integer getESTLibPresentLowTotalCount() {
            return estLibPresentLowTotalCount;
        }
        public Integer getESTLibPropagatedCount() {
            return estLibPropagatedCount;
        }
        public Integer getInSituExpPresentHighSelfCount() {
            return inSituExpPresentHighSelfCount;
        }
        public Integer getInSituExpPresentLowSelfCount() {
            return inSituExpPresentLowSelfCount;
        }
        public Integer getInSituExpAbsentHighSelfCount() {
            return inSituExpAbsentHighSelfCount;
        }
        public Integer getInSituExpAbsentLowSelfCount() {
            return inSituExpAbsentLowSelfCount;
        }
        public Integer getInSituExpPresentHighDescendantCount() {
            return inSituExpPresentHighDescendantCount;
        }
        public Integer getInSituExpPresentLowDescendantCount() {
            return inSituExpPresentLowDescendantCount;
        }
        public Integer getInSituExpAbsentHighParentCount() {
            return inSituExpAbsentHighParentCount;
        }
        public Integer getInSituExpAbsentLowParentCount() {
            return inSituExpAbsentLowParentCount;
        }
        public Integer getInSituExpPresentHighTotalCount() {
            return inSituExpPresentHighTotalCount;
        }
        public Integer getInSituExpPresentLowTotalCount() {
            return inSituExpPresentLowTotalCount;
        }
        public Integer getInSituExpAbsentHighTotalCount() {
            return inSituExpAbsentHighTotalCount;
        }
        public Integer getInSituExpAbsentLowTotalCount() {
            return inSituExpAbsentLowTotalCount;
        }
        public Integer getInSituExpPropagatedCount() {
            return inSituExpPropagatedCount;
        }


        @Override
        public String toString() {
            return "GlobalExpressionCallTO [id=" + this.getId() + ", bgeeGeneId=" + this.getBgeeGeneId() 
                    + ", conditionId=" + this.getConditionId() 
                    
                    + ", globalMeanRank=" + globalMeanRank
                    
                    + ", affymetrixExpPresentHighSelfCount=" + affymetrixExpPresentHighSelfCount
                    + ", affymetrixExpPresentLowSelfCount=" + affymetrixExpPresentLowSelfCount
                    + ", affymetrixExpAbsentHighSelfCount=" + affymetrixExpAbsentHighSelfCount
                    + ", affymetrixExpAbsentLowSelfCount=" + affymetrixExpAbsentLowSelfCount
                    + ", affymetrixExpPresentHighDescendantCount=" + affymetrixExpPresentHighDescendantCount
                    + ", affymetrixExpPresentLowDescendantCount=" + affymetrixExpPresentLowDescendantCount
                    + ", affymetrixExpAbsentHighParentCount=" + affymetrixExpAbsentHighParentCount
                    + ", affymetrixExpAbsentLowParentCount=" + affymetrixExpAbsentLowParentCount
                    + ", affymetrixExpPresentHighTotalCount=" + affymetrixExpPresentHighTotalCount
                    + ", affymetrixExpPresentLowTotalCount=" + affymetrixExpPresentLowTotalCount
                    + ", affymetrixExpAbsentHighTotalCount=" + affymetrixExpAbsentHighTotalCount
                    + ", affymetrixExpAbsentLowTotalCount=" + affymetrixExpAbsentLowTotalCount
                    + ", affymetrixExpPropagatedCount=" + affymetrixExpPropagatedCount
                    
                    + ", rnaSeqExpPresentHighSelfCount=" + rnaSeqExpPresentHighSelfCount
                    + ", rnaSeqExpPresentLowSelfCount=" + rnaSeqExpPresentLowSelfCount
                    + ", rnaSeqExpAbsentHighSelfCount=" + rnaSeqExpAbsentHighSelfCount
                    + ", rnaSeqExpAbsentLowSelfCount=" + rnaSeqExpAbsentLowSelfCount
                    + ", rnaSeqExpPresentHighDescendantCount=" + rnaSeqExpPresentHighDescendantCount
                    + ", rnaSeqExpPresentLowDescendantCount=" + rnaSeqExpPresentLowDescendantCount
                    + ", rnaSeqExpAbsentHighParentCount=" + rnaSeqExpAbsentHighParentCount
                    + ", rnaSeqExpAbsentLowParentCount=" + rnaSeqExpAbsentLowParentCount
                    + ", rnaSeqExpPresentHighTotalCount=" + rnaSeqExpPresentHighTotalCount
                    + ", rnaSeqExpPresentLowTotalCount=" + rnaSeqExpPresentLowTotalCount
                    + ", rnaSeqExpAbsentHighTotalCount=" + rnaSeqExpAbsentHighTotalCount
                    + ", rnaSeqExpAbsentLowTotalCount=" + rnaSeqExpAbsentLowTotalCount
                    + ", rnaSeqExpPropagatedCount=" + rnaSeqExpPropagatedCount
                    
                    + ", estLibPresentHighSelfCount=" + estLibPresentHighSelfCount
                    + ", estLibPresentLowSelfCount=" + estLibPresentLowSelfCount
                    + ", estLibPresentHighDescendantCount=" + estLibPresentHighDescendantCount
                    + ", estLibPresentLowDescendantCount=" + estLibPresentLowDescendantCount
                    + ", estLibPresentHighTotalCount=" + estLibPresentHighTotalCount
                    + ", estLibPresentLowTotalCount=" + estLibPresentLowTotalCount
                    + ", estLibPropagatedCount=" + estLibPropagatedCount
                    
                    + ", inSituExpPresentHighSelfCount=" + inSituExpPresentHighSelfCount
                    + ", inSituExpPresentLowSelfCount=" + inSituExpPresentLowSelfCount
                    + ", inSituExpAbsentHighSelfCount=" + inSituExpAbsentHighSelfCount
                    + ", inSituExpAbsentLowSelfCount=" + inSituExpAbsentLowSelfCount
                    + ", inSituExpPresentHighDescendantCount=" + inSituExpPresentHighDescendantCount
                    + ", inSituExpPresentLowDescendantCount=" + inSituExpPresentLowDescendantCount
                    + ", inSituExpAbsentHighParentCount=" + inSituExpAbsentHighParentCount
                    + ", inSituExpAbsentLowParentCount=" + inSituExpAbsentLowParentCount
                    + ", inSituExpPresentHighTotalCount=" + inSituExpPresentHighTotalCount
                    + ", inSituExpPresentLowTotalCount=" + inSituExpPresentLowTotalCount
                    + ", inSituExpAbsentHighTotalCount=" + inSituExpAbsentHighTotalCount
                    + ", inSituExpAbsentLowTotalCount=" + inSituExpAbsentLowTotalCount
                    + ", inSituExpPropagatedCount=" + inSituExpPropagatedCount
                    
                    + ", rnaSeqMeanRank=" + this.getRNASeqMeanRank() + ", affymetrixMeanRank=" 
                    + this.getAffymetrixMeanRank() + ", estRank=" + this.getESTRank() 
                    + ", inSituRank=" + this.getInSituRank() + ", affymetrixMeanRankNorm=" 
                    + this.getAffymetrixMeanRankNorm() + ", rnaSeqMeanRankNorm=" 
                    + this.getRNASeqMeanRankNorm() + ", estRankNorm=" + this.getESTRankNorm() 
                    + ", inSituRankNorm=" + this.getInSituRankNorm() + ", affymetrixDistinctRankSum=" 
                    + this.getAffymetrixDistinctRankSum() + ", rnaSeqDistinctRankSum=" 
                    + this.getRNASeqDistinctRankSum() + "]";
        }
    }

    /**
     * {@code DAOResultSet} specifics to {@code GlobalExpressionToRawExpressionTO}s
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    public interface GlobalExpressionToRawExpressionTOResultSet 
                    extends DAOResultSet<GlobalExpressionToRawExpressionTO> {
    }

    /**
     * A {@code TransferObject} representing relation between a raw expression call and a global
     * expression call in the data source.
     * <p>
     * This class defines a raw expression call ID (see {@link #getRawExpressionId()} 
     * and a global expression call ID (see {@link #getGlobalExpressionId()}), and also store 
     * the origin of the relations (association from sub-conditions or parent conditions 
     * or from the same condition, see {@link #getCallOrigin()}).
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    public final class GlobalExpressionToRawExpressionTO extends TransferObject {
        private final static Logger log = LogManager.getLogger(GlobalExpressionToRawExpressionTO.class.getName());
        private static final long serialVersionUID = -553628358149907274L;
        
        public enum CallOrigin implements TransferObject.EnumDAOField {
            SELF("self"), DESCENDANT("descendant"), PARENT("parent");

            /**
             * The {@code String} representation of the enum.
             */
            private String stringRepresentation;
            /**
             * Constructor
             * @param stringRepresentation the {@code String} representation of the enum.
             */
            CallOrigin(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            @Override
            public String getStringRepresentation() {
                return stringRepresentation;
            }
            /**
             * Return the mapped {@link CallOrigin} from a string representation.
             * @param stringRepresentation A string representation
             * @return The corresponding {@code CallOrigin}
             * @see org.bgee.model.dao.api.TransferObject.EnumDAOField#convert(Class, String)
             */
            public static CallOrigin convertToCallOrigin(String stringRepresentation){
                log.entry(stringRepresentation);
                return log.exit(GlobalExpressionToRawExpressionTO.convert(CallOrigin.class, 
                        stringRepresentation));
            }
        }

        /**
         * A {@code Integer} representing the ID of the raw expression call.
         */
        private final Integer rawExpressionId;
        /**
         * A {@code Integer} representing the ID of the global expression call.
         */
        private final Integer globalExpressionId;
        /**
         * A {@code CallOrigin} representing the origin of the association.
         */
        private final CallOrigin callOrigin;

        /**
         * Constructor providing the expression call ID (see {@link #getExpressionId()}) and 
         * the global expression call ID (see {@link #getGlobalExpressionId()}).
         * 
         * @param rawExpressionId       An {@code Integer} that is the ID of the raw expression call.
         * @param globalExpressionId    An {@code Integer} that is the ID of the global expression 
         *                              call.
         * @param callOrigin            An {@code CallOrigin} representing the origin of the association.
         **/
        public GlobalExpressionToRawExpressionTO(Integer rawExpressionId, Integer globalExpressionId, 
                CallOrigin callOrigin) {
            super();
            this.rawExpressionId = rawExpressionId;
            this.globalExpressionId = globalExpressionId;
            this.callOrigin = callOrigin;
        }

        /**
         * @return  the {@code Integer} representing the ID of the expression call.
         */
        public Integer getRawExpressionId() {
            return rawExpressionId;
        }
        /**
         * @return  the {@code Integer} representing the ID of the global expression call.
         */
        public Integer getGlobalExpressionId() {
            return globalExpressionId;
        }
        /**
         * @return  {@code CallOrigin} representing the origin of the association.
         */
        public CallOrigin getCallOrigin() {
            return callOrigin;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GlobalExpressionToRawExpressionTO [rawExpressionId=").append(rawExpressionId)
                    .append(", globalExpressionId=").append(globalExpressionId).append(", callOrigin=")
                    .append(callOrigin).append("]");
            return builder.toString();
        }
    }
}
