package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Objects;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataPipelineSummary;

public class RnaSeqLibraryPipelineSummary extends RawDataPipelineSummary{

    private final BigDecimal meanRefIntergenicDistribution;
    private final BigDecimal sdRefIntergenicDistribution;
    private final BigDecimal pValueThreshold;
    private final Integer allReadsCount;
    private final Integer allUMIsCount;
    private final Integer mappedReadsCount;
    private final Integer mappedUMIsCount;
    private final Integer minReadLength;
    private final Integer maxReadLength;

    public RnaSeqLibraryPipelineSummary (BigDecimal meanRefIntergenicDistribution,
            BigDecimal sdRefIntergenicDistribution, BigDecimal pValueThreshold,
            Integer allReadsCount, Integer allUMIsCount, Integer mappedReadsCount,
            Integer mappedUMIsCount, Integer minReadLength, Integer maxReadLength,
            BigDecimal libraryMaxRank, Integer libraryDistinctRankCount) {
        super(libraryDistinctRankCount, libraryMaxRank);
        this.meanRefIntergenicDistribution = meanRefIntergenicDistribution;
        this.sdRefIntergenicDistribution = sdRefIntergenicDistribution;
        this.pValueThreshold = pValueThreshold;
        this.allReadsCount = allReadsCount;
        this.allUMIsCount = allUMIsCount;
        this.mappedReadsCount = mappedReadsCount;
        this.mappedUMIsCount = mappedUMIsCount;
        this.minReadLength = minReadLength;
        this.maxReadLength = maxReadLength;
    }

    public BigDecimal getMeanRefIntergenicDistribution() {
        return meanRefIntergenicDistribution;
    }
    public BigDecimal getSdRefIntergenicDistribution() {
        return sdRefIntergenicDistribution;
    }
    public BigDecimal getpValueThreshold() {
        return pValueThreshold;
    }
    public Integer getAllReadsCount() {
        return allReadsCount;
    }
    public Integer getAllUMIsCount() {
        return allUMIsCount;
    }
    public Integer getMappedReadsCount() {
        return mappedReadsCount;
    }
    public Integer getMappedUMIsCount() {
        return mappedUMIsCount;
    }
    public Integer getMinReadLength() {
        return minReadLength;
    }
    public Integer getMaxReadLength() {
        return maxReadLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + Objects.hash(allReadsCount, allUMIsCount, mappedReadsCount, mappedUMIsCount,
                        maxReadLength, meanRefIntergenicDistribution, minReadLength,
                        pValueThreshold, sdRefIntergenicDistribution);
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
        RnaSeqLibraryPipelineSummary other = (RnaSeqLibraryPipelineSummary) obj;
        return Objects.equals(allReadsCount, other.allReadsCount) &&
                Objects.equals(allUMIsCount, other.allUMIsCount)
                && Objects.equals(mappedReadsCount, other.mappedReadsCount)
                && Objects.equals(mappedUMIsCount, other.mappedUMIsCount)
                && Objects.equals(maxReadLength, other.maxReadLength)
                && Objects.equals(meanRefIntergenicDistribution, other.meanRefIntergenicDistribution)
                && Objects.equals(minReadLength, other.minReadLength)
                && Objects.equals(pValueThreshold, other.pValueThreshold)
                && Objects.equals(sdRefIntergenicDistribution, other.sdRefIntergenicDistribution);
    }

    @Override
    public String toString() {
        return "RnaSeqLibraryPipelineSummary [meanRefIntergenicDistribution=" + meanRefIntergenicDistribution
                + ", sdRefIntergenicDistribution=" + sdRefIntergenicDistribution + ", pValueThreshold="
                + pValueThreshold + ", allReadsCount=" + allReadsCount + ", allUMIsCount=" + allUMIsCount
                + ", mappedReadsCount=" + mappedReadsCount + ", mappedUMIsCount=" + mappedUMIsCount + ", minReadLength="
                + minReadLength + ", maxReadLength=" + maxReadLength + ", distinctRankCount="
                + getDistinctRankCount() + ", maxRank=" + getMaxRank() + "]";
    }

    

    
}
