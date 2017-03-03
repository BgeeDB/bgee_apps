package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link RawExpressionCallTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Feb. 2017
 * @see RawExpressionCallTO
 */
public interface RawExpressionCallDAO extends DAO<RawExpressionCallDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawExpressionCallTO}s 
     * obtained from this {@code RawExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RawExpressionCallTO#getId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link RawExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RawExpressionCallTO#getConditionId()}.
     * <li>{@code AFFYMETRIX_MEAN_RANK}: corresponds to {@link RawExpressionCallTO#getAffymetrixMeanRank()}.
     * <li>{@code RNA_SEQ_MEAN_RANK}: corresponds to {@link RawExpressionCallTO#getRNASeqMeanRank()}.
     * <li>{@code EST_RANK}: corresponds to {@link RawExpressionCallTO#getESTRank()}.
     * <li>{@code IN_SITU_RANK}: corresponds to {@link RawExpressionCallTO#getInSituRank()}.
     * <li>{@code AFFYMETRIX_MEAN_RANK_NORM}: corresponds to {@link RawExpressionCallTO#getAffymetrixMeanRankNorm()}.
     * <li>{@code RNA_SEQ_MEAN_RANK_NORM}: corresponds to {@link RawExpressionCallTO#getRNASeqMeanRankNorm()}.
     * <li>{@code EST_RANK_NORM}: corresponds to {@link RawExpressionCallTO#getESTRankNorm()}.
     * <li>{@code IN_SITU_RANK_NORM}: corresponds to {@link RawExpressionCallTO#getInSituRankNorm()}.
     * <li>{@code AFFYMETRIX_DISTINCT_RANK_SUM}: corresponds to {@link RawExpressionCallTO#getAffymetrixDistinctRankSum()}.
     * <li>{@code RNA_SEQ_DISTINCT_RANK_SUM}: corresponds to {@link RawExpressionCallTO#getRNASeqDistinctRankSum()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), BGEE_GENE_ID("bgeeGeneId"), CONDITION_ID("conditionId"), 
        AFFYMETRIX_MEAN_RANK("affymetrixMeanRank"), RNA_SEQ_MEAN_RANK("rnaSeqMeanRank"), 
        EST_RANK("estRank"), IN_SITU_RANK("inSituRank"), 
        AFFYMETRIX_MEAN_RANK_NORM("affymetrixMeanRankNorm"), RNA_SEQ_MEAN_RANK_NORM("rnaSeqMeanRankNorm"), 
        EST_RANK_NORM("estRankNorm"), IN_SITU_RANK_NORM("inSituRankNorm"), 
        AFFYMETRIX_DISTINCT_RANK_SUM("affymetrixDistinctRankSum"), 
        RNA_SEQ_DISTINCT_RANK_SUM("rnaSeqDistinctRankSum");
        
        /**
         * A {@code String} that is the corresponding field name in {@code RelationTO} class.
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
     * Retrieve raw expression calls for a requested collection of gene IDs, ordered by gene IDs
     * and expression IDs, for the requested combination of condition parameters.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve calls for.
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link ConditionDAO.Attribute#isConditionParameter()}).
     * @return                      A {@code RawExpressionCallTOResultSet} allowing to obtain 
     *                              the requested {@code RawExpressionCallTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds,
            Collection<ConditionDAO.Attribute> condParameters) throws DAOException, IllegalArgumentException;
    
    /**
     * {@code DAOResultSet} specifics to {@code RawExpressionCallTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public interface RawExpressionCallTOResultSet extends DAOResultSet<RawExpressionCallTO> {
    }
    
    /**
     * {@code EntityTO} representing a raw expression call in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public class RawExpressionCallTO extends EntityTO<Integer> {
        
        private static final long serialVersionUID = -1057540315343857464L;
        
        /**
         * An {@code Integer} representing the ID of the gene associated to this call.
         */
        private final Integer bgeeGeneId;
        /**
         * An {@code Integer} representing the ID of the condition associated to this call.
         */
        private final Integer conditionId;
        
        private final BigDecimal rnaSeqMeanRank;
        private final BigDecimal affymetrixMeanRank;
        private final BigDecimal estRank;
        private final BigDecimal inSituRank;
        
        private final BigDecimal affymetrixMeanRankNorm;
        private final BigDecimal rnaSeqMeanRankNorm;
        private final BigDecimal estRankNorm;
        private final BigDecimal inSituRankNorm;
        
        private final BigDecimal affymetrixDistinctRankSum;
        private final BigDecimal rnaSeqDistinctRankSum;
        
        public RawExpressionCallTO(Integer id, Integer bgeeGeneId, Integer conditionId,
                BigDecimal affymetrixMeanRank, BigDecimal rnaSeqMeanRank, BigDecimal estRank,
                BigDecimal inSituRank, BigDecimal affymetrixMeanRankNorm,
                BigDecimal rnaSeqMeanRankNorm, BigDecimal estRankNorm, BigDecimal inSituRankNorm,
                BigDecimal affymetrixDistinctRankSum, BigDecimal rnaSeqDistinctRankSum) {
            super(id);
            this.bgeeGeneId = bgeeGeneId;
            this.conditionId = conditionId;
            this.affymetrixMeanRank = affymetrixMeanRank;
            this.rnaSeqMeanRank = rnaSeqMeanRank;
            this.estRank = estRank;
            this.inSituRank = inSituRank;
            this.affymetrixMeanRankNorm = affymetrixMeanRankNorm;
            this.rnaSeqMeanRankNorm = rnaSeqMeanRankNorm;
            this.estRankNorm = estRankNorm;
            this.inSituRankNorm = inSituRankNorm;
            this.affymetrixDistinctRankSum = affymetrixDistinctRankSum;
            this.rnaSeqDistinctRankSum = rnaSeqDistinctRankSum;
        }
        
        public Integer getBgeeGeneId() {
            return this.bgeeGeneId;
        }
        public Integer getConditionId() {
            return this.conditionId;
        }
        
        public BigDecimal getRNASeqMeanRank() {
            return rnaSeqMeanRank;
        }
        public BigDecimal getAffymetrixMeanRank() {
            return affymetrixMeanRank;
        }
        public BigDecimal getESTRank() {
            return estRank;
        }
        public BigDecimal getInSituRank() {
            return inSituRank;
        }
        
        public BigDecimal getAffymetrixMeanRankNorm() {
            return affymetrixMeanRankNorm;
        }
        public BigDecimal getRNASeqMeanRankNorm() {
            return rnaSeqMeanRankNorm;
        }
        public BigDecimal getESTRankNorm() {
            return estRankNorm;
        }
        public BigDecimal getInSituRankNorm() {
            return inSituRankNorm;
        }
        
        public BigDecimal getAffymetrixDistinctRankSum() {
            return affymetrixDistinctRankSum;
        }
        public BigDecimal getRNASeqDistinctRankSum() {
            return rnaSeqDistinctRankSum;
        }
        
        @Override
        public String toString() {
            return "RawExpressionCallTO [id=" + this.getId() + ", bgeeGeneId=" + bgeeGeneId 
                    + ", conditionId=" + conditionId + ", rnaSeqMeanRank=" + rnaSeqMeanRank 
                    + ", affymetrixMeanRank=" + affymetrixMeanRank + ", estRank=" + estRank 
                    + ", inSituRank=" + inSituRank + ", affymetrixMeanRankNorm=" 
                    + affymetrixMeanRankNorm + ", rnaSeqMeanRankNorm=" + rnaSeqMeanRankNorm
                    + ", estRankNorm=" + estRankNorm + ", inSituRankNorm=" + inSituRankNorm 
                    + ", affymetrixDistinctRankSum=" + affymetrixDistinctRankSum 
                    + ", rnaSeqDistinctRankSum=" + rnaSeqDistinctRankSum + "]";
        }
    }
}
