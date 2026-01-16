package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;


public interface ObservedExpressionDAO extends DAO<ObservedExpressionDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqLibraryTO}s
     * obtained from this {@code RNASeqLibraryDAO}.
     * <ul>
    // columns from Expression table
     * <li>{@code EXPRESSION_ID}: corresponds to {@link ExpressionTO#getExpressionId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link ExpressionTO#getConditionId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link ExpressionTO#getBgeeGeneId()}.
     * <li>{@code BULK_SCORE}: corresponds to {@link ExpressionTO#getBulkScore()}.
     * <li>{@code BULK_PVALUE}: corresponds to {@link ExpressionTO#getBulkPValue()}.
     * <li>{@code BULK_WEIGHT}: corresponds to {@link ExpressionTO#getBulkWeight()}.
     * <li>{@code BULK_NUM_OBS}: corresponds to {@link ExpressionTO#getBulkNumberObs()}.
     * <li>{@code SINGLE_CELL_SCORE}: corresponds to {@link ExpressionTO#getSingleCellScore()}.
     * <li>{@code SINGLE_CELL_PVALUE}: corresponds to {@link ExpressionTO#getSingleCellPValue()}.
     * <li>{@code SINGLE_CELL_WEIGHT}: corresponds to {@link ExpressionTO#getSingleCellWeight()}.
     * <li>{@code SINGLE_CELL_NUM_OBS}: corresponds to {@link ExpressionTO#getSingleCellNumberObs()}.
     * <li>{@code IN_SITU_SCORE}: corresponds to {@link ExpressionTO#getInSituScore()}.
     * <li>{@code IN_SITU_PVALUE}: corresponds to {@link ExpressionTO#getInSituPValue()}.
     * <li>{@code IN_SITU_WEIGHT}: corresponds to {@link ExpressionTO#getInSituWeight()}. 
     * <li>{@code IN_SITU_NUM_OBS}: corresponds to {@link ExpressionTO#getInSituNumberObs()}.
     **/

    public enum Attribute implements DAO.Attribute {
        EXPRESSION_ID("expressionId"), CONDITION_ID("conditionId"), BGEE_GENE_ID("bgeeGeneId"),
        BULK_SCORE("bulkScore"), BULK_PVALUE("bulkPValue"), BULK_WEIGHT("bulkWeight"), BULK_NUM_OBS("bulkNumberObs"),
        SINGLE_CELL_SCORE("singleCellScore"), SINGLE_CELL_PVALUE("singleCellPValue"),
        SINGLE_CELL_WEIGHT("singleCellWeight"), SINGLE_CELL_NUM_OBS("singleCellNumberObs"),
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
        private final BigDecimal singleCellScore;
        private final BigDecimal singleCellPValue;
        private final BigDecimal singleCellWeight;
        private final Integer singleCellNumberObs;
        private final BigDecimal inSituScore;
        private final BigDecimal inSituPValue;
        private final BigDecimal inSituWeight;
        private final Integer inSituNumberObs;

        public ObservedExpressionTO(Integer expressionId, Integer conditionId, Integer bgeeGeneId,
                BigDecimal bulkScore, BigDecimal bulkPValue, BigDecimal bulkWeight, Integer bulkNumberObs,
                BigDecimal singleCellScore, BigDecimal singleCellPValue, BigDecimal singleCellWeight,
                Integer singleCellNumberObs, BigDecimal inSituScore, BigDecimal inSituPValue,
                BigDecimal inSituWeight, Integer inSituNumberObs) {
            super(expressionId);
            this.conditionId = conditionId;
            this.bgeeGeneId = bgeeGeneId;
            this.bulkScore = bulkScore;
            this.bulkPValue = bulkPValue;
            this.bulkWeight = bulkWeight;
            this.bulkNumberObs = bulkNumberObs;
            this.singleCellScore = singleCellScore;
            this.singleCellPValue = singleCellPValue;
            this.singleCellWeight = singleCellWeight;
            this.singleCellNumberObs = singleCellNumberObs;
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

        public BigDecimal getSingleCellPValue() {
            return singleCellPValue;
        }

        public BigDecimal getSingleCellWeight() {
            return singleCellWeight;
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

        public Integer getSingleCellNumberObs() {
            return singleCellNumberObs;
        }

        public Integer getInSituNumberObs() {
            return inSituNumberObs;
        }

        public BigDecimal getBulkScore() {
            return bulkScore;
        }

        public BigDecimal getSingleCellScore() {
            return singleCellScore;
        }

        public BigDecimal getInSituScore() {
            return inSituScore;
        }

        @Override
        public String toString() {
            return "ObservedExpressionTO [conditionId=" + conditionId + ", bgeeGeneId=" + bgeeGeneId + ", bulkScore="
                    + bulkScore + ", bulkPValue=" + bulkPValue + ", bulkWeight=" + bulkWeight + ", bulkNumberObs="
                    + bulkNumberObs + ", singleCellScore=" + singleCellScore + ", singleCellPValue=" + singleCellPValue
                    + ", singleCellWeight=" + singleCellWeight + ", singleCellNumberObs=" + singleCellNumberObs
                    + ", inSituScore=" + inSituScore + ", inSituPValue=" + inSituPValue + ", inSituWeight="
                    + inSituWeight + ", inSituNumberObs=" + inSituNumberObs + "]";
        }


    }
}
