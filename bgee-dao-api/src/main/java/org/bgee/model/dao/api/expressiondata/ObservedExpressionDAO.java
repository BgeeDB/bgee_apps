package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;


public interface ObservedExpressionDAO extends DAO<ObservedExpressionDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ObservedExpressionTO}s
     * obtained from this {@code ObservedExpressionDAO}.
     * <ul>
    // columns from Expression table
     * <li>{@code EXPRESSION_ID}: corresponds to {@link ObservedExpressionTO#getId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link ObservedExpressionTO#getConditionId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link ObservedExpressionTO#getBgeeGeneId()}.
     * <li>{@code BULK_SCORE}: corresponds to {@link ObservedExpressionTO#getBulkScore()}.
     * <li>{@code BULK_PVALUE}: corresponds to {@link ObservedExpressionTO#getBulkPValue()}.
     * <li>{@code BULK_WEIGHT}: corresponds to {@link ObservedExpressionTO#getBulkWeight()}.
     * <li>{@code BULK_NUM_OBS}: corresponds to {@link ObservedExpressionTO#getBulkNumberObs()}.
     * <li>{@code FULL_LENGTH_SCORE}: corresponds to {@link ObservedExpressionTO#getFullLengthScore()}.
     * <li>{@code FULL_LENGTH_PVALUE}: corresponds to {@link ObservedExpressionTO#getFullLengthPValue()}.
     * <li>{@code FULL_LENGTH_WEIGHT}: corresponds to {@link ObservedExpressionTO#getFullLengthWeight()}.
     * <li>{@code FULL_LENGTH_NUM_OBS}: corresponds to {@link ObservedExpressionTO#getFullLengthNumberObs()}.
     * <li>{@code DROPLET_SCORE}: corresponds to {@link ObservedExpressionTO#getDropletScore()}.
     * <li>{@code DROPLET_PVALUE}: corresponds to {@link ObservedExpressionTO#getDropletPValue()}.
     * <li>{@code DROPLET_WEIGHT}: corresponds to {@link ObservedExpressionTO#getDropletWeight()}.
     * <li>{@code DROPLET_NUM_OBS}: corresponds to {@link ObservedExpressionTO#getDropletNumberObs()}.
     * <li>{@code IN_SITU_SCORE}: corresponds to {@link ObservedExpressionTO#getInSituScore()}.
     * <li>{@code IN_SITU_PVALUE}: corresponds to {@link ObservedExpressionTO#getInSituPValue()}.
     * <li>{@code IN_SITU_WEIGHT}: corresponds to {@link ObservedExpressionTO#getInSituWeight()}. 
     * <li>{@code IN_SITU_NUM_OBS}: corresponds to {@link ObservedExpressionTO#getInSituNumberObs()}.
     **/

    public enum Attribute implements DAO.Attribute {
        EXPRESSION_ID("expressionId"), CONDITION_ID("conditionId"), BGEE_GENE_ID("bgeeGeneId"),
        BULK_SCORE("bulkScore"), BULK_PVALUE("bulkPValue"), BULK_WEIGHT("bulkWeight"), BULK_NUM_OBS("bulkNumberObs"),
        FULL_LENGTH_SCORE("fullLengthScore"), FULL_LENGTH_PVALUE("fullLengthPValue"),
        FULL_LENGTH_WEIGHT("fullLengthWeight"), FULL_LENGTH_NUM_OBS("fullLengthNumberObs"),
        DROPLET_SCORE("dropletScore"), DROPLET_PVALUE("dropletPValue"),
        DROPLET_WEIGHT("dropletWeight"), DROPLET_NUM_OBS("dropletNumberObs"),
        IN_SITU_SCORE("inSituScore"), IN_SITU_PVALUE("inSituPValue"), IN_SITU_WEIGHT("inSituWeight"),
        IN_SITU_NUM_OBS("inSituNumberObs");

        /**
         * A {@code String} that is the corresponding field name in {@code ExpressionTO} class.
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

    //XXX: Should maybe add orderingAttributes to sort by gene, conditionId, etc.
    public ObservedExpressionTOResultSet getObservedExpression(DAOObservedExpressionFilter dataFilter,
            Collection<Attribute> attributes);

    /**
     * {@code DAOResultSet} for {@code ObservedExpressionTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 16, Oct. 2025
     * @since   Bgee 16, Oct. 2025
     */
    public interface ObservedExpressionTOResultSet extends DAOResultSet<ObservedExpressionTO> {
    }

    public final class ObservedExpressionTO extends EntityTO<Integer> {

        private static final long serialVersionUID = -7154753240891071477L;

        private final Integer conditionId;
        private final Integer bgeeGeneId;
        private final BigDecimal bulkScore;
        private final BigDecimal bulkPValue;
        private final BigDecimal bulkWeight;
        private final Integer bulkNumberObs;
        private final BigDecimal fullLengthScore;
        private final BigDecimal fullLengthPValue;
        private final BigDecimal fullLengthWeight;
        private final Integer fullLengthNumberObs;
        private final BigDecimal dropletScore;
        private final BigDecimal dropletPValue;
        private final BigDecimal dropletWeight;
        private final Integer dropletNumberObs;
        private final BigDecimal inSituScore;
        private final BigDecimal inSituPValue;
        private final BigDecimal inSituWeight;
        private final Integer inSituNumberObs;

        public ObservedExpressionTO(Integer expressionId, Integer conditionId, Integer bgeeGeneId,
                BigDecimal bulkScore, BigDecimal bulkPValue, BigDecimal bulkWeight, Integer bulkNumberObs,
                BigDecimal fullLengthScore, BigDecimal fullLengthPValue, BigDecimal fullLengthWeight,
                Integer fullLengthNumberObs, BigDecimal dropletScore, BigDecimal dropletPValue,
                BigDecimal dropletWeight, Integer dropletNumberObs, BigDecimal inSituScore,
                BigDecimal inSituPValue, BigDecimal inSituWeight, Integer inSituNumberObs) {
            super(expressionId);
            this.conditionId = conditionId;
            this.bgeeGeneId = bgeeGeneId;
            this.bulkScore = bulkScore;
            this.bulkPValue = bulkPValue;
            this.bulkWeight = bulkWeight;
            this.bulkNumberObs = bulkNumberObs;
            this.fullLengthScore = fullLengthScore;
            this.fullLengthPValue = fullLengthPValue;
            this.fullLengthWeight = fullLengthWeight;
            this.fullLengthNumberObs = fullLengthNumberObs;
            this.dropletScore = dropletScore;
            this.dropletPValue = dropletPValue;
            this.dropletWeight = dropletWeight;
            this.dropletNumberObs = dropletNumberObs;
            this.inSituScore = inSituScore;
            this.inSituPValue = inSituPValue;
            this.inSituWeight = inSituWeight;
            this.inSituNumberObs = inSituNumberObs;
        }

        public Integer getConditionId() {
            return conditionId;
        }

        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }

        public BigDecimal getBulkPValue() {
            return bulkPValue;
        }

        public BigDecimal getBulkWeight() {
            return bulkWeight;
        }

        public BigDecimal getFullLengthPValue() {
            return fullLengthPValue;
        }

        public BigDecimal getFullLengthWeight() {
            return fullLengthWeight;
        }

        public BigDecimal getDropletPValue() {
            return dropletPValue;
        }

        public BigDecimal getDropletWeight() {
            return dropletWeight;
        }

        public BigDecimal getInSituPValue() {
            return inSituPValue;
        }

        public BigDecimal getInSituWeight() {
            return inSituWeight;
        }

        public Integer getBulkNumberObs() {
            return bulkNumberObs;
        }

        public Integer getFullLengthNumberObs() {
            return fullLengthNumberObs;
        }

        public Integer getDropletNumberObs() {
            return dropletNumberObs;
        }

        public Integer getInSituNumberObs() {
            return inSituNumberObs;
        }

        public BigDecimal getBulkScore() {
            return bulkScore;
        }

        public BigDecimal getFullLengthScore() {
            return fullLengthScore;
        }

        public BigDecimal getDropletScore() {
            return dropletScore;
        }

        public BigDecimal getInSituScore() {
            return inSituScore;
        }

        @Override
        public String toString() {
            return "ObservedExpressionTO [conditionId=" + conditionId + ", bgeeGeneId=" + bgeeGeneId + ", bulkScore="
                    + bulkScore + ", bulkPValue=" + bulkPValue + ", bulkWeight=" + bulkWeight + ", bulkNumberObs="
                    + bulkNumberObs + ", fullLengthScore=" + fullLengthScore + ", fullLengthPValue=" + fullLengthPValue
                    + ", fullLengthWeight=" + fullLengthWeight + ", fullLengthNumberObs=" + fullLengthNumberObs
                    + ", dropletScore=" + dropletScore + ", dropletPValue=" + dropletPValue + ", dropletWeight="
                    + dropletWeight + ", dropletNumberObs=" + dropletNumberObs + ", inSituScore=" + inSituScore
                    + ", inSituPValue=" + inSituPValue + ", inSituWeight=" + inSituWeight + ", inSituNumberObs="
                    + inSituNumberObs + "]";
        }


    }
}
