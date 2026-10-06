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
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;

public class OTFExpressionCall {

    private final static Logger log = LogManager.getLogger(OTFExpressionCall.class.getName());

    private final Gene gene;
    private final Condition2 condition;
    private final EnumSet<DataType> supportingDataTypes;
    /**
     * The weighted mean of the p-values of the observations of this condition and of all its
     * sub-conditions, <strong>not calibrated</strong>: the mean of p-values is not itself
     * a p-value. {@link #getAllDataTypePValue()} returns the calibrated value, this one is
     * the value to use to keep aggregating over the condition graph, together with
     * {@link #getAllDataTypePValueWeight()}.
     */
    private final BigDecimal allDataTypePValueRawMean;
    /**
     * The total weight over which {@link #allDataTypePValueRawMean} is the weighted mean.
     */
    private final BigDecimal allDataTypePValueWeight;
    /**
     * Same as {@link #allDataTypePValueRawMean}, over the data types trusted for absent calls only.
     */
    private final BigDecimal trustedDataTypePValueRawMean;
    /**
     * The total weight over which {@link #trustedDataTypePValueRawMean} is the weighted mean.
     */
    private final BigDecimal trustedDataTypePValueWeight;
    /**
     * The number of observations aggregated in this call, over this condition and all its
     * sub-conditions. A mean of several p-values must be doubled to be a valid p-value,
     * a single p-value must not: this count is what tells the two apart.
     */
    private final int observationCount;
    private final BigDecimal bestDescendantAllDataTypePValue;
    private final BigDecimal bestDescendantTrustedDataTypePValue;
    private final BigDecimal expressionScoreWeight;
    private final BigDecimal expressionScore;
    private final BigDecimal bestDescendantExpressionScoreWeight;
    private final BigDecimal bestDescendantExpressionScore;
    private final PropagationState dataPropagation;

    public OTFExpressionCall(Gene gene, Condition2 condition, EnumSet<DataType> supportingDataTypes,
            BigDecimal allDataTypePValueRawMean, BigDecimal allDataTypePValueWeight,
            BigDecimal trustedDataTypePValueRawMean, BigDecimal trustedDataTypePValueWeight,
            int observationCount,
            BigDecimal bestDescendantAllDataTypePValue, BigDecimal bestDescendantTrustedDataTypePValue,
            BigDecimal expressionScoreWeight, BigDecimal expressionScore,
            BigDecimal bestDescendantExpressionScoreWeight, BigDecimal bestDescendantExpressionScore,
            PropagationState dataPropagation) {
        this.gene = gene;
        this.condition = condition;
        this.supportingDataTypes = supportingDataTypes;
        this.allDataTypePValueRawMean = allDataTypePValueRawMean;
        this.allDataTypePValueWeight = allDataTypePValueWeight;
        this.trustedDataTypePValueRawMean = trustedDataTypePValueRawMean;
        this.trustedDataTypePValueWeight = trustedDataTypePValueWeight;
        this.observationCount = observationCount;
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
    public Condition2 getCondition() {
        return condition;
    }
    public EnumSet<DataType> getSupportingDataTypes() {
        return supportingDataTypes;
    }
    /**
     * @return  The p-value of this call over all the requested data types, calibrated:
     *          see {@link #calibrate(BigDecimal)}. This is the value to compare to the p-value
     *          thresholds; use {@link #getAllDataTypePValueRawMean()} to keep aggregating.
     */
    public BigDecimal getAllDataTypePValue() {
        return calibrate(allDataTypePValueRawMean);
    }
    /**
     * @return  The p-value of this call over the data types trusted for absent calls, calibrated.
     */
    public BigDecimal getTrustedDataTypePValue() {
        return calibrate(trustedDataTypePValueRawMean);
    }
    public BigDecimal getAllDataTypePValueRawMean() {
        return allDataTypePValueRawMean;
    }
    public BigDecimal getAllDataTypePValueWeight() {
        return allDataTypePValueWeight;
    }
    public BigDecimal getTrustedDataTypePValueRawMean() {
        return trustedDataTypePValueRawMean;
    }
    public BigDecimal getTrustedDataTypePValueWeight() {
        return trustedDataTypePValueWeight;
    }
    public int getObservationCount() {
        return observationCount;
    }
    /**
     * The mean of several p-values is not a p-value, but twice that mean is one, whatever
     * the dependence between the observations. A call aggregating several observations therefore
     * exposes twice its weighted mean, capped at 1, while a call backed by a single observation
     * exposes its p-value unchanged.
     * <p>
     * Of note, the calibration is applied here, when the p-value is read, and not when it is
     * computed: the aggregation over the condition graph uses the raw weighted mean, and
     * doubling it at every condition would compound the factor at each level.
     *
     * @param rawMean   A {@code BigDecimal} that is the raw weighted mean of the p-values.
     * @return          A {@code BigDecimal} that is the corresponding p-value, or {@code null}
     *                  if {@code rawMean} is {@code null}.
     */
    private BigDecimal calibrate(BigDecimal rawMean) {
        if (rawMean == null) {
            return null;
        }
        if (this.observationCount <= 1) {
            return rawMean;
        }
        BigDecimal doubled = rawMean.multiply(new BigDecimal("2"));
        return doubled.compareTo(BigDecimal.ONE) > 0? BigDecimal.ONE: doubled;
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

    /**
     * The expression score of this call, with two decimals, as it is displayed and returned by
     * the API. Counterpart of {@link #getFormattedAllDatatypePValue()} for the score, and the
     * single place formatting it: the responses of the gene page, of the data page and of the
     * multispecies endpoint must not each round it their own way.
     *
     * @return  A {@code String} that is the formatted expression score, {@code null} if this
     *          call has none.
     */
    public String getFormattedExpressionScore() {
        log.traceEntry();
        if (this.expressionScore == null) {
            return log.traceExit((String) null);
        }
        //setScale rather than String.format: the result must not depend on the default Locale,
        //and an expression score runs from 0 to 100, so no thousands separator can occur.
        return log.traceExit(this.expressionScore.setScale(2, RoundingMode.HALF_UP)
                .toPlainString());
    }

    /**
     * Whether the expression score of a call is of high confidence: its summary quality must be
     * better than {@code BRONZE}, whatever the data types supporting it. Bgee 15 also required
     * a data type producing a quantitative score, or a mean rank better than 20,000, but its
     * in situ ranks all stayed below 20,000: every call of the data types still used was
     * of high confidence when better than {@code BRONZE}, as with this rule. This is the single
     * definition of that rule, shared by every response exposing
     * an {@code expressionScoreConfidence}.
     *
     * @param quality               The {@code SummaryQuality} inferred for the call, as returned
     *                              by {@code OTFExpressionCallFilterEngine
     *                              #inferSummaryCallTypeAndQuality(OTFExpressionCall, BigDecimal,
     *                              BigDecimal, BigDecimal, BigDecimal)}. A {@code null} quality,
     *                              meaning no summary call type applies, is not of high
     *                              confidence.
     * @param supportingDataTypes   The {@code DataType}s supporting the call. For a group of
     *                              calls, such as the calls supporting a multi-species
     *                              similarity, their union. Not used by the current rule.
     * @return                      A {@code boolean} that is {@code true} if the expression score
     *                              is of high confidence.
     */
    public static boolean isHighConfidenceExpressionScore(SummaryQuality quality,
            Set<DataType> supportingDataTypes) {
        log.traceEntry("{}, {}", quality, supportingDataTypes);
        return log.traceExit(quality != null && !SummaryQuality.BRONZE.equals(quality));
    }

    /**
     * @param quality   The {@code SummaryQuality} inferred for this call.
     * @return          A {@code boolean} that is {@code true} if the expression score of this
     *                  call is of high confidence.
     * @see #isHighConfidenceExpressionScore(SummaryQuality, Set)
     */
    public boolean isHighConfidenceExpressionScore(SummaryQuality quality) {
        log.traceEntry("{}", quality);
        return log.traceExit(isHighConfidenceExpressionScore(quality, this.supportingDataTypes));
    }

    public String getFormattedAllDatatypePValue() {
        log.traceEntry();
        //the displayed p-value is the calibrated one, as everywhere else
        BigDecimal pValue = this.getAllDataTypePValue();
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        // do not use scientific notation when FDR pValue is bigger than 0.001 or equal
        // to 0
        if(pValue.compareTo(new BigDecimal(0.001)) >= 0 || 
                pValue.compareTo(new BigDecimal(0)) == 0) {
            formatter.setMaximumFractionDigits(3);
            formatter.setMinimumFractionDigits(0);
        } else if (formatter instanceof DecimalFormat) {
            ((DecimalFormat) formatter).applyPattern("0.00E0");
        } else {
            throw log.throwing(new IllegalStateException("No formatter could be defined "
                    + "for " + pValue));
        }
        //In Bgee 16 we limited the precision to 30 digits
        return log.traceExit((pValue.compareTo(new BigDecimal("0")) != 0 &&
                pValue.compareTo(new BigDecimal("1E-30")) <= 0 ? "<= ": "")
                + formatter.format(pValue).toLowerCase(Locale.US));
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDataTypePValueRawMean, allDataTypePValueWeight, observationCount, bestDescendantAllDataTypePValue,
                bestDescendantExpressionScore, bestDescendantExpressionScoreWeight,
                bestDescendantTrustedDataTypePValue, condition, dataPropagation, expressionScore,
                expressionScoreWeight, gene, supportingDataTypes, trustedDataTypePValueRawMean, trustedDataTypePValueWeight);
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
        return Objects.equals(allDataTypePValueRawMean, other.allDataTypePValueRawMean)
                && Objects.equals(allDataTypePValueWeight, other.allDataTypePValueWeight)
                && observationCount == other.observationCount
                && Objects.equals(bestDescendantAllDataTypePValue, other.bestDescendantAllDataTypePValue)
                && Objects.equals(bestDescendantExpressionScore, other.bestDescendantExpressionScore)
                && Objects.equals(bestDescendantExpressionScoreWeight, other.bestDescendantExpressionScoreWeight)
                && Objects.equals(bestDescendantTrustedDataTypePValue, other.bestDescendantTrustedDataTypePValue)
                && Objects.equals(condition, other.condition) && Objects.equals(dataPropagation, other.dataPropagation)
                && Objects.equals(expressionScore, other.expressionScore)
                && Objects.equals(expressionScoreWeight, other.expressionScoreWeight)
                && Objects.equals(gene, other.gene) && Objects.equals(supportingDataTypes, other.supportingDataTypes)
                && Objects.equals(trustedDataTypePValueRawMean, other.trustedDataTypePValueRawMean)
                && Objects.equals(trustedDataTypePValueWeight, other.trustedDataTypePValueWeight);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OTFExpressionCall [")
               .append("gene=").append(gene)
               .append(", condition=").append(condition)
               .append(", supportingDataTypes=").append(supportingDataTypes)
               .append(", allDataTypePValueRawMean=").append(allDataTypePValueRawMean)
               .append(", allDataTypePValueWeight=").append(allDataTypePValueWeight)
               .append(", observationCount=").append(observationCount)
               .append(", trustedDataTypePValueRawMean=").append(trustedDataTypePValueRawMean)
               .append(", trustedDataTypePValueWeight=").append(trustedDataTypePValueWeight)
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
