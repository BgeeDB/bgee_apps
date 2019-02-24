package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.gene.Gene;

/**
 * A class providing information about the expression level of an
 * {@link org.bgee.model.expressiondata.Call.ExpressionCall ExpressionCall}.
 *
 * @author Frederic Bastian
 * @see org.bgee.model.expressiondata.Call.ExpressionCall
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 */
public class ExpressionLevelInfo {
    private final static Logger log = LogManager.getLogger(ExpressionLevelInfo.class.getName());

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
        log.entry(max);
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
        return log.exit(formatter);
    }

    private final BigDecimal rank;
    private final QualitativeExpressionLevel<Gene> qualExprLevelRelativeToGene;
    private final QualitativeExpressionLevel<AnatEntity> qualExprLevelRelativeToAnatEntity;

    /**
     * @param rank  See {@link #getRank()}
     */
    public ExpressionLevelInfo(BigDecimal rank) {
        this(rank, null, null);
    }
    /**
     * @param rank                              See {@link #getRank()}
     * @param qualExprLevelRelativeToGene       See {@link #getQualExprLevelRelativeToGene()}
     * @param qualExprLevelRelativeToAnatEntity See {@link #getQualExprLevelRelativeToAnatEntity()}
     */
    public ExpressionLevelInfo(BigDecimal rank,
            QualitativeExpressionLevel<Gene> qualExprLevelRelativeToGene,
            QualitativeExpressionLevel<AnatEntity> qualExprLevelRelativeToAnatEntity) {
        if (rank == null) {
            throw log.throwing(new IllegalArgumentException("At least a rank must be provided"));
        }
        if (rank.compareTo(new BigDecimal(0)) <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "The rank cannot be less than or equal to 0."));
        }
        this.rank = rank;
        this.qualExprLevelRelativeToGene = qualExprLevelRelativeToGene;
        this.qualExprLevelRelativeToAnatEntity = qualExprLevelRelativeToAnatEntity;
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
     *          with always 3 digits displayed, e.g.: 1.23, 12.3, 123, 1.23e3, ...
     * @see #getRank()
     */
    public String getFormattedRank() {
        log.entry();
        if (this.rank == null) {
            throw log.throwing(new IllegalStateException("No rank was provided for this call."));
        }
        NumberFormat formatter = null;
        //start with values over 1000, more chances to have a match.
        //And since we are going to round half up, 999.5 will be rounded to 1000
        //IMPORTANT: if you want to change the rounding etc, you have to change the method getNumberFormat
        if (this.rank.compareTo(new BigDecimal(999.5)) >= 0) {
            formatter = FORMAT1000;
        //2 significant digits kept below 10, so 9.995 will be rounded to 10
        } else if (this.rank.compareTo(new BigDecimal(9.995)) < 0) {
            formatter = FORMAT1;
        //1 significant digit kept below 100, so 99.95 will be rounded to 100
        } else if (this.rank.compareTo(new BigDecimal(99.95)) < 0) {
            formatter = FORMAT10;
        //0 significant digit kept below 1000, so 999.5 will be rounded to 1000
        } else if (this.rank.compareTo(new BigDecimal(999.5)) < 0) {
            formatter = FORMAT100;
        }
        //1E2 to 1e2
        return log.exit(formatter.format(this.rank).toLowerCase(Locale.ENGLISH));
    }
    /**
     * @return  {@code QualitativeExpressionLevel} for an {@code ExpressionCall}
     *          relative to the expression level of the {@code Gene} (obtained by comparing
     *          the expression rank score of the call to the min. and max rank scores
     *          of the gene in any condition). See {@link QualitativeExpressionLevel}
     *          for more details.
     */
    public QualitativeExpressionLevel<Gene> getQualExprLevelRelativeToGene() {
        return qualExprLevelRelativeToGene;
    }
    /**
     * @return  {@code QualitativeExpressionLevel} for an {@code ExpressionCall}
     *          relative to the expression level of the {@code AnatEntity} (obtained by comparing
     *          the expression rank score of the call to the min. and max rank scores
     *          in the anatomical entity, considering any gene). See {@link QualitativeExpressionLevel}
     *          for more details.
     */
    public QualitativeExpressionLevel<AnatEntity> getQualExprLevelRelativeToAnatEntity() {
        return qualExprLevelRelativeToAnatEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((qualExprLevelRelativeToAnatEntity == null) ? 0 : qualExprLevelRelativeToAnatEntity.hashCode());
        result = prime * result + ((qualExprLevelRelativeToGene == null) ? 0 : qualExprLevelRelativeToGene.hashCode());
        result = prime * result + ((rank == null) ? 0 : rank.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExpressionLevelInfo other = (ExpressionLevelInfo) obj;
        if (qualExprLevelRelativeToAnatEntity == null) {
            if (other.qualExprLevelRelativeToAnatEntity != null) {
                return false;
            }
        } else if (!qualExprLevelRelativeToAnatEntity.equals(other.qualExprLevelRelativeToAnatEntity)) {
            return false;
        }
        if (qualExprLevelRelativeToGene == null) {
            if (other.qualExprLevelRelativeToGene != null) {
                return false;
            }
        } else if (!qualExprLevelRelativeToGene.equals(other.qualExprLevelRelativeToGene)) {
            return false;
        }
        if (rank == null) {
            if (other.rank != null) {
                return false;
            }
        } else if (!rank.equals(other.rank)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExpressionLevelInfo [rank=").append(rank)
               .append(", qualExprLevelRelativeToGene=").append(qualExprLevelRelativeToGene)
               .append(", qualExprLevelRelativeToAnatEntity=")
               .append(qualExprLevelRelativeToAnatEntity).append("]");
        return builder.toString();
    }
}