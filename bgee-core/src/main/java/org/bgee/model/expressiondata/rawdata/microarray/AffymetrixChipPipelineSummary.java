package org.bgee.model.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.util.Objects;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataPipelineSummary;

/**
 * Class describing pipeline summary for affymetrix chips
 *
 * @author Julien Wollbrett
 * @version Bgee 15, Nov. 2022
 */
public class AffymetrixChipPipelineSummary extends RawDataPipelineSummary{

    private final String scanDate;
    private final String normalizationType;
    private final BigDecimal qualityScore;
    private final BigDecimal percentPresent;

    public AffymetrixChipPipelineSummary(Integer distinctRankCount, BigDecimal maxRank, String scanDate,
            String normalizationType, BigDecimal qualityScore, BigDecimal percentPresent) {
        super(distinctRankCount, maxRank);
        this.scanDate = scanDate;
        this.normalizationType = normalizationType;
        this.qualityScore = qualityScore;
        this.percentPresent = percentPresent;
    }
    public String getScanDate() {
        return scanDate;
    }
    public String getNormalizationType() {
        return normalizationType;
    }
    public BigDecimal getQualityScore() {
        return qualityScore;
    }
    public BigDecimal getPercentPresent() {
        return percentPresent;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(normalizationType, percentPresent, qualityScore, scanDate);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AffymetrixChipPipelineSummary other = (AffymetrixChipPipelineSummary) obj;
        return Objects.equals(normalizationType, other.normalizationType)
                && Objects.equals(percentPresent, other.percentPresent)
                && Objects.equals(qualityScore, other.qualityScore) &&
                Objects.equals(scanDate, other.scanDate);
    }
    @Override
    public String toString() {
        return "AffymetrixChipPipelineSummary [scanDate=" + scanDate + ", normalizationType="
                + normalizationType + ", qualityScore=" + qualityScore + ", percentPresent="
                + percentPresent + ", distinctRankCount=" + getDistinctRankCount() + ", maxRank="
                + getMaxRank() + "]";
    }

    

}
