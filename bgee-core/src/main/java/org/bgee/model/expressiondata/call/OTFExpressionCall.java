package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.gene.Gene;

public class OTFExpressionCall {

    private final static Logger log = LogManager.getLogger(OTFExpressionCall.class.getName());

    //TODO: implement method allowing advanced ordering options using ordering attributes
    /**
     * Sorts all calls from the provided map into a single list ordered by decreasing
     * {@code expressionScore}. Calls with a {@code null} expression score are placed last.
     *
     * @param propagatedExpressionCalls a map of genes to their propagated calls
     * @return a flat list of all calls sorted by decreasing expression score
     */
    public static List<OTFExpressionCall> sortByDecreasingExpressionScore(
            Map<Gene, Set<OTFExpressionCall>> propagatedExpressionCalls) {
        return propagatedExpressionCalls.values().stream()
                .flatMap(Set::stream)
                .sorted(Comparator.comparing(
                        OTFExpressionCall::getExpressionScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

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

    public String getFormattedAllDatatypePValue() {
        log.traceEntry();
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        // do not use scientific notation when FDR pValue is bigger than 0.001 or equal
        // to 0
        if(allDataTypePValue.compareTo(new BigDecimal(0.001)) >= 0 || 
                allDataTypePValue.compareTo(new BigDecimal(0)) == 0) {
            formatter.setMaximumFractionDigits(3);
            formatter.setMinimumFractionDigits(0);
        } else if (formatter instanceof DecimalFormat) {
            ((DecimalFormat) formatter).applyPattern("0.00E0");
        } else {
            throw log.throwing(new IllegalStateException("No formatter could be defined "
                    + "for " + allDataTypePValue));
        }
        //In Bgee 16 we limited the precision to 30 digits
        return log.traceExit((allDataTypePValue.compareTo(new BigDecimal("0")) != 0 &&
                allDataTypePValue.compareTo(new BigDecimal("1E-30")) <= 0 ? "<= ": "")
                + formatter.format(allDataTypePValue).toLowerCase(Locale.US));
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
