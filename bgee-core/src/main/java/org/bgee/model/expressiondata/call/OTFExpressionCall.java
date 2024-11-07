package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.gene.Gene;

public class OTFExpressionCall {

    private final Gene gene;
    private final Condition condition;
    private final EnumSet<DataType> supportingDataTypes;
    private final BigDecimal allDataTypePValue;
    private final BigDecimal trustedDataTypePValue;
    private final BigDecimal bestDescendantAllDataTypePValue;
    private final BigDecimal bestDescendantTrustedDataTypePValue;
    private final BigDecimal expressionScoreWeight;
    private final BigDecimal expressionScore;
    private final BigDecimal bestDescendantExpressionScoreWeight;
    private final BigDecimal bestDescendantExpressionScore;
    private final PropagationState dataPropagation;

    public OTFExpressionCall(Gene gene, Condition condition, EnumSet<DataType> supportingDataTypes,
            BigDecimal allDataTypePValue, BigDecimal trustedDataTypePValue,
            BigDecimal bestDescendantAllDataTypePValue, BigDecimal bestDescendantTrustedDataTypePValue,
            BigDecimal expressionScoreWeight, BigDecimal expressionScore,
            BigDecimal bestDescendantExpressionScoreWeight, BigDecimal bestDescendantExpressionScore,
            PropagationState dataPropagation) {
        this.gene = gene;
        this.condition = condition;
        this.supportingDataTypes = supportingDataTypes;
        this.allDataTypePValue = allDataTypePValue;
        this.trustedDataTypePValue = trustedDataTypePValue;
        this.bestDescendantAllDataTypePValue = bestDescendantAllDataTypePValue;
        this.bestDescendantTrustedDataTypePValue = bestDescendantTrustedDataTypePValue;
        this.expressionScoreWeight = expressionScoreWeight;
        this.expressionScore = expressionScore;
        this.bestDescendantExpressionScoreWeight = bestDescendantExpressionScoreWeight;
        this.bestDescendantExpressionScore = bestDescendantExpressionScore;
        this.dataPropagation = dataPropagation;
    }

    public Gene getGene() {
        return gene;
    }
    public Condition getCondition() {
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
    public BigDecimal getBestDescendantAllDataTypePValue() {
        return bestDescendantAllDataTypePValue;
    }
    public BigDecimal getBestDescendantTrustedDataTypePValue() {
        return bestDescendantTrustedDataTypePValue;
    }
    public BigDecimal getExpressionScoreWeight() {
        return expressionScoreWeight;
    }
    public BigDecimal getExpressionScore() {
        return expressionScore;
    }
    public BigDecimal getBestDescendantExpressionScoreWeight() {
        return bestDescendantExpressionScoreWeight;
    }
    public BigDecimal getBestDescendantExpressionScore() {
        return bestDescendantExpressionScore;
    }
    public PropagationState getDataPropagation() {
        return dataPropagation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDataTypePValue, bestDescendantAllDataTypePValue, bestDescendantExpressionScore,
                bestDescendantExpressionScoreWeight, bestDescendantTrustedDataTypePValue, condition, dataPropagation,
                expressionScore, expressionScoreWeight, gene, supportingDataTypes, trustedDataTypePValue);
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
                && Objects.equals(bestDescendantAllDataTypePValue, other.bestDescendantAllDataTypePValue)
                && Objects.equals(bestDescendantExpressionScore, other.bestDescendantExpressionScore)
                && Objects.equals(bestDescendantExpressionScoreWeight, other.bestDescendantExpressionScoreWeight)
                && Objects.equals(bestDescendantTrustedDataTypePValue, other.bestDescendantTrustedDataTypePValue)
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
               .append(", bestDescendantAllDataTypePValue=").append(bestDescendantAllDataTypePValue)
               .append(", bestDescendantTrustedDataTypePValue=").append(bestDescendantTrustedDataTypePValue)
               .append(", expressionScoreWeight=").append(expressionScoreWeight)
               .append(", expressionScore=").append(expressionScore)
               .append(", bestDescendantExpressionScoreWeight=").append(bestDescendantExpressionScoreWeight)
               .append(", bestDescendantExpressionScore=").append(bestDescendantExpressionScore)
               .append(", dataPropagation=").append(dataPropagation)
               .append("]");
        return builder.toString();
    }
}
