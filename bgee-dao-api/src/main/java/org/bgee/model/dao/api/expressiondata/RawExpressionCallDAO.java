package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link RawExpressionCallTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Feb. 2017
 * @see RawExpressionCallTO
 */
public interface RawExpressionCallDAO extends DAO<RawExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawExpressionCallTO}s 
     * obtained from this {@code RawExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RawExpressionCallDTO#getId()}.
     * <li>{@code GENE_ID}: corresponds to {@link RawExpressionCallDTO#getSourceId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RawExpressionCallDTO#getTargetId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), GENE_ID("geneId"), CONDITION_ID("conditionId");
        
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
     * TODO javadoc
     * @param speciesId
     * @return
     * @throws DAOException
     */
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(int speciesId)
        throws DAOException;

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
   * @version Bgee 14, Feb. 2017
   * @since   Bgee 14, Feb. 2017
   */
  public class RawExpressionCallTO extends TransferObject {

      private static final long serialVersionUID = -1057540315343857464L;
      
      /**
       * A {@code String} representing the ID of this call.
       */
      private Integer id;
      /**
       * A {@code String} representing the ID of the gene associated to this call.
       */
      private Integer bgeeGeneId;
      /**
       * A {@code String} representing the ID of the condition associated this call.
       */
      private Integer conditionId;

      private BigDecimal rnaSeqMeanRank;
      private BigDecimal affymetrixMeanRank;

      private BigDecimal estRank;
      private BigDecimal inSituRank;

      private BigDecimal affymetrixMaxRank;
      private BigDecimal rnaSeqMaxRank;
      private BigDecimal estMaxRank;
      private BigDecimal inSituMaxRank;

      private BigDecimal affymetrixMeanRankNorm;
      private BigDecimal rnaSeqMeanRankNorm;

      private BigDecimal estRankNorm;
      private BigDecimal inSituRankNorm;

      private BigDecimal affymetrixDistinctRankSum;
      private BigDecimal rnaSeqDistinctRankSum;

      public RawExpressionCallTO(Integer id, Integer bgeeGeneId, Integer conditionId,
          BigDecimal rnaSeqMeanRank, BigDecimal affymetrixMeanRank, BigDecimal estRank,
          BigDecimal inSituRank, BigDecimal affymetrixMaxRank, BigDecimal rnaSeqMaxRank,
          BigDecimal estMaxRank, BigDecimal inSituMaxRank, BigDecimal affymetrixMeanRankNorm,
          BigDecimal rnaSeqMeanRankNorm, BigDecimal estRankNorm, BigDecimal inSituRankNorm,
          BigDecimal affymetrixDistinctRankSum, BigDecimal rnaSeqDistinctRankSum) {
          super();
          this.id = id;
          this.bgeeGeneId = bgeeGeneId;
          this.conditionId = conditionId;
          this.rnaSeqMeanRank = rnaSeqMeanRank;
          this.affymetrixMeanRank = affymetrixMeanRank;
          this.estRank = estRank;
          this.inSituRank = inSituRank;
          this.affymetrixMaxRank = affymetrixMaxRank;
          this.rnaSeqMaxRank = rnaSeqMaxRank;
          this.estMaxRank = estMaxRank;
          this.inSituMaxRank = inSituMaxRank;
          this.affymetrixMeanRankNorm = affymetrixMeanRankNorm;
          this.rnaSeqMeanRankNorm = rnaSeqMeanRankNorm;
          this.estRankNorm = estRankNorm;
          this.inSituRankNorm = inSituRankNorm;
          this.affymetrixDistinctRankSum = affymetrixDistinctRankSum;
          this.rnaSeqDistinctRankSum = rnaSeqDistinctRankSum;
      }
      
      public Integer getId() {
          return this.id;
      }
      public Integer getBgeeGeneId() {
          return this.bgeeGeneId;
      }
      public Integer getConditionId() {
          return this.conditionId;
      }

    public BigDecimal getRnaSeqMeanRank() {
        return rnaSeqMeanRank;
    }

    public BigDecimal getAffymetrixMeanRank() {
        return affymetrixMeanRank;
    }

    public BigDecimal getEstRank() {
        return estRank;
    }

    public BigDecimal getInSituRank() {
        return inSituRank;
    }

    public BigDecimal getAffymetrixMaxRank() {
        return affymetrixMaxRank;
    }

    public BigDecimal getRnaSeqMaxRank() {
        return rnaSeqMaxRank;
    }

    public BigDecimal getEstMaxRank() {
        return estMaxRank;
    }

    public BigDecimal getInSituMaxRank() {
        return inSituMaxRank;
    }

    public BigDecimal getAffymetrixMeanRankNorm() {
        return affymetrixMeanRankNorm;
    }

    public BigDecimal getRnaSeqMeanRankNorm() {
        return rnaSeqMeanRankNorm;
    }

    public BigDecimal getEstRankNorm() {
        return estRankNorm;
    }

    public BigDecimal getInSituRankNorm() {
        return inSituRankNorm;
    }

    public BigDecimal getAffymetrixDistinctRankSum() {
        return affymetrixDistinctRankSum;
    }

    public BigDecimal getRnaSeqDistinctRankSum() {
        return rnaSeqDistinctRankSum;
    }

    @Override
    public String toString() {
        return "RawExpressionCallTO [id=" + id + ", bgeeGeneId=" + bgeeGeneId + ", conditionId="
            + conditionId + ", rnaSeqMeanRank=" + rnaSeqMeanRank + ", affymetrixMeanRank=" + affymetrixMeanRank
            + ", estRank=" + estRank + ", inSituRank=" + inSituRank + ", affymetrixMaxRank=" + affymetrixMaxRank
            + ", rnaSeqMaxRank=" + rnaSeqMaxRank + ", estMaxRank=" + estMaxRank + ", inSituMaxRank=" + inSituMaxRank
            + ", affymetrixMeanRankNorm=" + affymetrixMeanRankNorm + ", rnaSeqMeanRankNorm=" + rnaSeqMeanRankNorm
            + ", estRankNorm=" + estRankNorm + ", inSituRankNorm=" + inSituRankNorm + ", affymetrixDistinctRankSum="
            + affymetrixDistinctRankSum + ", rnaSeqDistinctRankSum=" + rnaSeqDistinctRankSum + "]";
    }
  }
}
