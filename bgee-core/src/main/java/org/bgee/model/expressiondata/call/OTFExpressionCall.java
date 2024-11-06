package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.Gene;

public class OTFExpressionCall {

    private final Gene gene;
    private final Condition condition;
    private final EnumSet<DataType> supportingDataTypes;
    private final BigDecimal trustedDataTypePValue;
    private final BigDecimal allDataTypePValue;
    private final BigDecimal bestDescendantTrustedDataTypePValue;
    private final BigDecimal bestDescendantAllDataTypePValue;
    private final BigDecimal expressionScoreWeight;
    private final BigDecimal expressionScore;
    private final BigDecimal bestDescendantExpressionScoreWeight;
    private final BigDecimal bestDescendantExpressionScore;
    private final DataPropagation dataPropagation;

    public OTFExpressionCall(Gene gene, Condition condition, EnumSet<DataType> supportingDataTypes,
            BigDecimal trustedDataTypePValue, BigDecimal allDataTypePValue,
            BigDecimal bestDescendantTrustedDataTypePValue, BigDecimal bestDescendantAllDataTypePValue,
            BigDecimal expressionScoreWeight, BigDecimal expressionScore,
            BigDecimal bestDescendantExpressionScoreWeight, BigDecimal bestDescendantExpressionScore,
            DataPropagation dataPropagation) {
        this.gene = gene;
        this.condition = condition;
        this.supportingDataTypes = supportingDataTypes;
        this.trustedDataTypePValue = trustedDataTypePValue;
        this.allDataTypePValue = allDataTypePValue;
        this.bestDescendantTrustedDataTypePValue = bestDescendantTrustedDataTypePValue;
        this.bestDescendantAllDataTypePValue = bestDescendantAllDataTypePValue;
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
    public BigDecimal getTrustedDataTypePValue() {
        return trustedDataTypePValue;
    }
    public BigDecimal getAllDataTypePValue() {
        return allDataTypePValue;
    }
    public BigDecimal getBestDescendantTrustedDataTypePValue() {
        return bestDescendantTrustedDataTypePValue;
    }
    public BigDecimal getBestDescendantAllDataTypePValue() {
        return bestDescendantAllDataTypePValue;
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
    public DataPropagation getDataPropagation() {
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
}
