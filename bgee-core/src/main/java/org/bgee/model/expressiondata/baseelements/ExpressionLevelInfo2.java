package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class providing information about the expression level of an
 * {@link org.bgee.model.expressiondata.call.Call.ExpressionCallOTF ExpressionCallOTF}.
 *
 * @author Julien Wollbrett
 * @see org.bgee.model.expressiondata.call.Call.ExpressionCall
 * @since Bgee 16 Nov. 2025
 * @version Bgee 16 Nov. 2025
 */
public class ExpressionLevelInfo2 {

    private final static Logger log = LogManager.getLogger(ExpressionLevelInfo2.class.getName());

    /**
     * A {@code NumberFormat} to format rank scores less than 10.
     */
    private static final NumberFormat FORMAT1 = getNumberFormat(1);
    /**
     * A {@code NumberFormat} to format rank scores less than 100.
     */
    private static final NumberFormat FORMAT10 = getNumberFormat(10);
    /**
     * A {@code NumberFormat} to format rank scores less than 1000.
     */
    private static final NumberFormat FORMAT100 = getNumberFormat(100);
    /**
     * A {@code NumberFormat} to format rank scores greater than or equal to 1000.
     */
    private static final NumberFormat FORMAT1000 = getNumberFormat(1000);
    /**
     * @param max   An {@code int} to retrieve a {@code NumberFormat} managing values 
     *              less than 10, or less than 100, or less than 1000, or greater than 
     *              or equal to 10000.
     * @return      A {@code NumberFormat} parameterized for formatting rank scores 
     *              of the appropriate range.
     */
    private static final NumberFormat getNumberFormat(int max) {
        log.traceEntry("{}", max);
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        //IMPORTANT: if you change the rounding mode, or the min/max fraction digits,
        //you have to also update the method getFormattedGlobalMeanRank
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        if (max < 10) {
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
        } else if (max < 100) {
            formatter.setMaximumFractionDigits(1);
            formatter.setMinimumFractionDigits(1);
        } else if (max < 1000) {
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
        //FIXME: the javadoc for DecimalFormat explicitly states that NumberFormat.getInstance
        //may return subclasses of NumberFormat other than DecimalFormat, this means this code
        //is potentially broken on some machines. (Note FB: otherwise, why do you think I bothered
        //using this NumberFormat mechanism, seriously? :p)
        } else if (formatter instanceof DecimalFormat) {
            ((DecimalFormat) formatter).applyPattern("0.00E0");
        } else {
            throw log.throwing(new IllegalStateException("No formatter could be defined"));
        }
        return log.traceExit(formatter);
    }
    /**
     * @param number    The {@code BigDecimal} to format.
     * @return          A {@code String} corresponding to the provided {@code number}, formatted
     *                  with always 3 digits displayed, e.g.: 1.23, 12.3, 123, 1.23e3.
     *                  No value returned will be equal to 0, the smallest value returned will be 0.01
     */
    private static final String formatExpressionNumber(BigDecimal number) {
        log.traceEntry("{}", number);
        if (number == null) {
            return log.traceExit((String) null);
        }
        BigDecimal threshold = new BigDecimal("0.01");
        BigDecimal numberToFormat = number;
        if (number.compareTo(threshold) < 0) {
            numberToFormat = threshold;
        }
        NumberFormat formatter = null;
        //start with values over 1000, more chances to have a match.
        //And since we are going to round half up, 999.5 will be rounded to 1000
        //IMPORTANT: if you want to change the rounding etc, you have to change the method getNumberFormat
        if (numberToFormat.compareTo(new BigDecimal("999.5")) >= 0) {
            formatter = FORMAT1000;
        //2 significant digits kept below 10, so 9.995 will be rounded to 10
        } else if (numberToFormat.compareTo(new BigDecimal("9.995")) < 0) {
            formatter = FORMAT1;
        //1 significant digit kept below 100, so 99.95 will be rounded to 100
        } else if (numberToFormat.compareTo(new BigDecimal("99.95")) < 0) {
            formatter = FORMAT10;
        //0 significant digit kept below 1000, so 999.5 will be rounded to 1000
        } else if (numberToFormat.compareTo(new BigDecimal("999.5")) < 0) {
            formatter = FORMAT100;
        }
        //1E2 to 1e2
        return log.traceExit(formatter.format(numberToFormat).toLowerCase(Locale.ENGLISH));
    }

    private final BigDecimal rank;
    private final BigDecimal expressionScore;
    private final BigDecimal maxRankForExpressionScore;
    private final BigDecimal pValue;
    private final Set<DataType> dataTypes;

    /**
     * @param rank  See {@link #getRank()}
     */
    public ExpressionLevelInfo2(BigDecimal rank) {
        this(rank, null, null, null, null);
    }
    /**
     * @param rank                              See {@link #getRank()}
     * @param expressionScore                   See {@link #getExpressionScore()}
     * @param maxRankForExpressionScore         See {@link #getMaxRankForExpressionScore()}
     * @param pValue                            See {@link #getPValue()}
//     * @param qualExprLevelRelativeToGene       See {@link #getQualExprLevelRelativeToGene()}
//     * @param qualExprLevelRelativeToAnatEntity See {@link #getQualExprLevelRelativeToAnatEntity()}
     */
    public ExpressionLevelInfo2(BigDecimal rank, BigDecimal expressionScore,
            BigDecimal maxRankForExpressionScore, BigDecimal pValue, Set<DataType> dataTypes) {
//            QualitativeExpressionLevel<Gene> qualExprLevelRelativeToGene,
//            QualitativeExpressionLevel<Condition> qualExprLevelRelativeToAnatEntity) {
        if (rank != null && rank.compareTo(new BigDecimal("0")) <= 0 ||
                maxRankForExpressionScore != null &&
                maxRankForExpressionScore.compareTo(new BigDecimal("0")) <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "Ranks cannot be less than or equal to 0."));
        }
        if (expressionScore != null &&
                (expressionScore.compareTo(new BigDecimal("0")) <= 0 ||
                        expressionScore.compareTo(new BigDecimal("100")) > 0)) {
            throw log.throwing(new IllegalArgumentException(
                    "The expression score must be greater than 0 and less than or equal to 100"));
        }
        this.rank = rank;
        this.expressionScore = expressionScore;
        this.maxRankForExpressionScore = maxRankForExpressionScore;
        this.pValue = pValue;
        this.dataTypes = dataTypes;
//        this.qualExprLevelRelativeToGene = qualExprLevelRelativeToGene;
//        this.qualExprLevelRelativeToAnatEntity = qualExprLevelRelativeToAnatEntity;
    }

    /**
     * @return  The {@code BigDecimal} corresponding to the expression rank score,
     *          reflecting the expression level of an {@code ExpressionCall}.
     * @see #getFormattedRank()
     */
    public BigDecimal getRank() {
        return rank;
    }
    /**
     * @return  A {@code String} corresponding to the rank score of this call, formatted
     *          with always 3 digits displayed, e.g.: 1.23, 12.3, 123, 1.23e3.
     * @see #getRank()
     */
    public String getFormattedRank() {
        log.traceEntry();
        return log.traceExit(formatExpressionNumber(this.rank));
    }
    /**
     * @return  The {@code BigDecimal} corresponding to the expression score,
     *          reflecting the expression level of an {@code ExpressionCallOTF}.
     *          The expression score is:
     *          <ul>
     *          <li> a normalization of the rank returned by {@link #getRank()}
     *          to a value between 1 and 100
     *          <li>The values are inverted as compared to the rank returned by {@link #getRank()}:
     *          the higher the expression score, the higher the expression level (for ranks, it is the opposite,
     *          the lower the rank, the higher the expression level).
     *          </ul>
     *          So, the max rank in a species corresponds to an expression score of 1,
     *          and a rank of 1 corresponds to an expression score of 100.
     *
     * @see #getFormattedExpressionScore()
     * @see #getRank()
     */
    public BigDecimal getExpressionScore() {
        return expressionScore;
    }
    /**
     * @return  A {@code String} corresponding to the expression score of this call, formatted
     *          with always 3 digits displayed, e.g.: 1.23, 12.3, 100.
     * @see #getExpressionScore()
     */
    public String getFormattedExpressionScore() {
        log.traceEntry();
        return log.traceExit(String.format(Locale.US, "%,.2f", this.expressionScore.setScale(2, RoundingMode.HALF_UP)));
    }
    /**
     * @return  The {@code BigDecimal} corresponding to the max expression rank,
     *          allowing to transform the expression rank into an expression score.
     * @see #getRank()
     * @see #getExpressionScore()
     */
    public BigDecimal getMaxRankForExpressionScore() {
        return maxRankForExpressionScore;
    }
    /**
     * @return  The {@code BigDecimal} corresponding to the pValue of an {@code ExpressionCallOTF}.
     */
    public BigDecimal getpValue() {
        return pValue;
    }
    /**
     * @return  The {@code Set} of {@code DataType} for which self or descendant expression has been used
     *  to generate the {@code ExpressionCallOTF}
     */
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataTypes, expressionScore, maxRankForExpressionScore, pValue, rank);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpressionLevelInfo2 other = (ExpressionLevelInfo2) obj;
        return Objects.equals(dataTypes, other.dataTypes) && Objects.equals(expressionScore, other.expressionScore)
                && Objects.equals(maxRankForExpressionScore, other.maxRankForExpressionScore)
                && Objects.equals(pValue, other.pValue) && Objects.equals(rank, other.rank);
    }
    @Override
    public String toString() {
        return "ExpressionLevelInfo2 [rank=" + rank + ", expressionScore=" + expressionScore
                + ", maxRankForExpressionScore=" + maxRankForExpressionScore + ", pValue=" + pValue + ", dataTypes="
                + dataTypes + "]";
    }

    



}