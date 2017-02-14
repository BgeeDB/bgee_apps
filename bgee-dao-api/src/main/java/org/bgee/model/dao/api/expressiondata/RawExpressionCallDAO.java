package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

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
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(String speciesId)
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
      private Integer geneId;
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

      public RawExpressionCallTO(Integer id, Integer geneId, Integer conditionId,
          BigDecimal rnaSeqMeanRank, BigDecimal affymetrixMeanRank, BigDecimal estRank,
          BigDecimal inSituRank, BigDecimal affymetrixMaxRank, BigDecimal rnaSeqMaxRank,
          BigDecimal estMaxRank, BigDecimal inSituMaxRank, BigDecimal affymetrixMeanRankNorm,
          BigDecimal rnaSeqMeanRankNorm, BigDecimal estRankNorm, BigDecimal inSituRankNorm,
          BigDecimal affymetrixDistinctRankSum, BigDecimal rnaSeqDistinctRankSum) {
          super();
          this.id = id;
          this.geneId = geneId;
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
      public Integer getGeneId() {
          return this.geneId;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((affymetrixDistinctRankSum == null) ? 0 : affymetrixDistinctRankSum.hashCode());
        result = prime * result + ((affymetrixMaxRank == null) ? 0 : affymetrixMaxRank.hashCode());
        result = prime * result + ((affymetrixMeanRank == null) ? 0 : affymetrixMeanRank.hashCode());
        result = prime * result + ((affymetrixMeanRankNorm == null) ? 0 : affymetrixMeanRankNorm.hashCode());
        result = prime * result + ((conditionId == null) ? 0 : conditionId.hashCode());
        result = prime * result + ((estMaxRank == null) ? 0 : estMaxRank.hashCode());
        result = prime * result + ((estRank == null) ? 0 : estRank.hashCode());
        result = prime * result + ((estRankNorm == null) ? 0 : estRankNorm.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
        result = prime * result + ((inSituMaxRank == null) ? 0 : inSituMaxRank.hashCode());
        result = prime * result + ((inSituRank == null) ? 0 : inSituRank.hashCode());
        result = prime * result + ((inSituRankNorm == null) ? 0 : inSituRankNorm.hashCode());
        result = prime * result + ((rnaSeqDistinctRankSum == null) ? 0 : rnaSeqDistinctRankSum.hashCode());
        result = prime * result + ((rnaSeqMaxRank == null) ? 0 : rnaSeqMaxRank.hashCode());
        result = prime * result + ((rnaSeqMeanRank == null) ? 0 : rnaSeqMeanRank.hashCode());
        result = prime * result + ((rnaSeqMeanRankNorm == null) ? 0 : rnaSeqMeanRankNorm.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawExpressionCallTO other = (RawExpressionCallTO) obj;
        if (affymetrixDistinctRankSum == null) {
            if (other.affymetrixDistinctRankSum != null)
                return false;
        } else if (!affymetrixDistinctRankSum.equals(other.affymetrixDistinctRankSum))
            return false;
        if (affymetrixMaxRank == null) {
            if (other.affymetrixMaxRank != null)
                return false;
        } else if (!affymetrixMaxRank.equals(other.affymetrixMaxRank))
            return false;
        if (affymetrixMeanRank == null) {
            if (other.affymetrixMeanRank != null)
                return false;
        } else if (!affymetrixMeanRank.equals(other.affymetrixMeanRank))
            return false;
        if (affymetrixMeanRankNorm == null) {
            if (other.affymetrixMeanRankNorm != null)
                return false;
        } else if (!affymetrixMeanRankNorm.equals(other.affymetrixMeanRankNorm))
            return false;
        if (conditionId == null) {
            if (other.conditionId != null)
                return false;
        } else if (!conditionId.equals(other.conditionId))
            return false;
        if (estMaxRank == null) {
            if (other.estMaxRank != null)
                return false;
        } else if (!estMaxRank.equals(other.estMaxRank))
            return false;
        if (estRank == null) {
            if (other.estRank != null)
                return false;
        } else if (!estRank.equals(other.estRank))
            return false;
        if (estRankNorm == null) {
            if (other.estRankNorm != null)
                return false;
        } else if (!estRankNorm.equals(other.estRankNorm))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (inSituMaxRank == null) {
            if (other.inSituMaxRank != null)
                return false;
        } else if (!inSituMaxRank.equals(other.inSituMaxRank))
            return false;
        if (inSituRank == null) {
            if (other.inSituRank != null)
                return false;
        } else if (!inSituRank.equals(other.inSituRank))
            return false;
        if (inSituRankNorm == null) {
            if (other.inSituRankNorm != null)
                return false;
        } else if (!inSituRankNorm.equals(other.inSituRankNorm))
            return false;
        if (rnaSeqDistinctRankSum == null) {
            if (other.rnaSeqDistinctRankSum != null)
                return false;
        } else if (!rnaSeqDistinctRankSum.equals(other.rnaSeqDistinctRankSum))
            return false;
        if (rnaSeqMaxRank == null) {
            if (other.rnaSeqMaxRank != null)
                return false;
        } else if (!rnaSeqMaxRank.equals(other.rnaSeqMaxRank))
            return false;
        if (rnaSeqMeanRank == null) {
            if (other.rnaSeqMeanRank != null)
                return false;
        } else if (!rnaSeqMeanRank.equals(other.rnaSeqMeanRank))
            return false;
        if (rnaSeqMeanRankNorm == null) {
            if (other.rnaSeqMeanRankNorm != null)
                return false;
        } else if (!rnaSeqMeanRankNorm.equals(other.rnaSeqMeanRankNorm))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RawExpressionCallTO [id=" + id + ", geneId=" + geneId + ", conditionId="
            + conditionId + ", rnaSeqMeanRank=" + rnaSeqMeanRank + ", affymetrixMeanRank=" + affymetrixMeanRank
            + ", estRank=" + estRank + ", inSituRank=" + inSituRank + ", affymetrixMaxRank=" + affymetrixMaxRank
            + ", rnaSeqMaxRank=" + rnaSeqMaxRank + ", estMaxRank=" + estMaxRank + ", inSituMaxRank=" + inSituMaxRank
            + ", affymetrixMeanRankNorm=" + affymetrixMeanRankNorm + ", rnaSeqMeanRankNorm=" + rnaSeqMeanRankNorm
            + ", estRankNorm=" + estRankNorm + ", inSituRankNorm=" + inSituRankNorm + ", affymetrixDistinctRankSum="
            + affymetrixDistinctRankSum + ", rnaSeqDistinctRankSum=" + rnaSeqDistinctRankSum + "]";
    }
  }
}
