package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.gene.Gene;

public class OTFExpressionCall {

    private final Gene gene;
    private final Condition2 condition;
    private final EnumSet<DataType> supportingDataTypes;
    private final BigDecimal allDataTypePValue;
    private final BigDecimal trustedDataTypePValue;
    private final BigDecimal bestDirectDescendantAllDataTypePValue;
    private final BigDecimal bestDirectDescendantTrustedDataTypePValue;
    private final BigDecimal expressionScoreWeight;
    private final BigDecimal expressionScore;
    private final BigDecimal bestDirectDescendantExpressionScoreWeight;
    private final BigDecimal bestDirectDescendantExpressionScore;
    private final PropagationState dataPropagation;

    public OTFExpressionCall(Gene gene, Condition2 condition, EnumSet<DataType> supportingDataTypes,
            BigDecimal allDataTypePValue, BigDecimal trustedDataTypePValue,
            BigDecimal bestDirectDescendantAllDataTypePValue, BigDecimal bestDirectDescendantTrustedDataTypePValue,
            BigDecimal expressionScoreWeight, BigDecimal expressionScore,
            BigDecimal bestDirectDescendantExpressionScoreWeight, BigDecimal bestDirectDescendantExpressionScore,
            PropagationState dataPropagation) {
        this.gene = gene;
        this.condition = condition;
        this.supportingDataTypes = supportingDataTypes;
        this.allDataTypePValue = allDataTypePValue;
        this.trustedDataTypePValue = trustedDataTypePValue;
        this.bestDirectDescendantAllDataTypePValue = bestDirectDescendantAllDataTypePValue;
        this.bestDirectDescendantTrustedDataTypePValue = bestDirectDescendantTrustedDataTypePValue;
        this.expressionScoreWeight = expressionScoreWeight;
        this.expressionScore = expressionScore;
        this.bestDirectDescendantExpressionScoreWeight = bestDirectDescendantExpressionScoreWeight;
        this.bestDirectDescendantExpressionScore = bestDirectDescendantExpressionScore;
        this.dataPropagation = dataPropagation;
    }

    public Gene getGene() {
        return gene;
    }
    public Condition2 getCondition() {
        return condition;
    }
    public EnumSet<DataType> getSupportingDataTypes() {
        return supportingDataTypes;
    }
    public BigDecimal getAllDataTypePValue() {
        return allDataTypePValue;
    }
    public BigDecimal getTrustedDataTypePValue() {
        return trustedDataTypePValue;
    }
    public BigDecimal getBestDirectDescendantAllDataTypePValue() {
        return bestDirectDescendantAllDataTypePValue;
    }
    public BigDecimal getBestDirectDescendantTrustedDataTypePValue() {
        return bestDirectDescendantTrustedDataTypePValue;
    }
    public BigDecimal getExpressionScoreWeight() {
        return expressionScoreWeight;
    }
    public BigDecimal getExpressionScore() {
        return expressionScore;
    }
    public BigDecimal getBestDirectDescendantExpressionScoreWeight() {
        return bestDirectDescendantExpressionScoreWeight;
    }
    public BigDecimal getBestDirectDescendantExpressionScore() {
        return bestDirectDescendantExpressionScore;
    }
    public PropagationState getDataPropagation() {
        return dataPropagation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDataTypePValue, bestDirectDescendantAllDataTypePValue,
                bestDirectDescendantExpressionScore, bestDirectDescendantExpressionScoreWeight,
                bestDirectDescendantTrustedDataTypePValue, condition, dataPropagation, expressionScore,
                expressionScoreWeight, gene, supportingDataTypes, trustedDataTypePValue);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OTFExpressionCall other = (OTFExpressionCall) obj;
        return Objects.equals(allDataTypePValue, other.allDataTypePValue)
                && Objects.equals(bestDirectDescendantAllDataTypePValue, other.bestDirectDescendantAllDataTypePValue)
                && Objects.equals(bestDirectDescendantExpressionScore, other.bestDirectDescendantExpressionScore)
                && Objects.equals(bestDirectDescendantExpressionScoreWeight, other.bestDirectDescendantExpressionScoreWeight)
                && Objects.equals(bestDirectDescendantTrustedDataTypePValue, other.bestDirectDescendantTrustedDataTypePValue)
                && Objects.equals(condition, other.condition) && Objects.equals(dataPropagation, other.dataPropagation)
                && Objects.equals(expressionScore, other.expressionScore)
                && Objects.equals(expressionScoreWeight, other.expressionScoreWeight)
                && Objects.equals(gene, other.gene) && Objects.equals(supportingDataTypes, other.supportingDataTypes)
                && Objects.equals(trustedDataTypePValue, other.trustedDataTypePValue);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OTFExpressionCall [")
               .append("gene=").append(gene)
               .append(", condition=").append(condition)
               .append(", supportingDataTypes=").append(supportingDataTypes)
               .append(", allDataTypePValue=").append(allDataTypePValue)
               .append(", trustedDataTypePValue=").append(trustedDataTypePValue)
               .append(", bestDirectDescendantAllDataTypePValue=").append(bestDirectDescendantAllDataTypePValue)
               .append(", bestDirectDescendantTrustedDataTypePValue=").append(bestDirectDescendantTrustedDataTypePValue)
               .append(", expressionScoreWeight=").append(expressionScoreWeight)
               .append(", expressionScore=").append(expressionScore)
               .append(", bestDirectDescendantExpressionScoreWeight=").append(bestDirectDescendantExpressionScoreWeight)
               .append(", bestDirectDescendantExpressionScore=").append(bestDirectDescendantExpressionScore)
               .append(", dataPropagation=").append(dataPropagation)
               .append("]");
        return builder.toString();
    }
}
