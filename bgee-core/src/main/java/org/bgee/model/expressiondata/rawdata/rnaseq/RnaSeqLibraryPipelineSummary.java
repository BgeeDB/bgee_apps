package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Objects;

public class RnaSeqLibraryPipelineSummary {

    private final BigDecimal meanRefIntergenicDistribution;
    private final BigDecimal sdRefIntergenicDistribution;
    private final BigDecimal pValueThreshold;
    private final Integer allReadsCount;
    private final Integer allUMIsCount;
    private final Integer mappedReadsCount;
    private final Integer mappedUMIsCount;
    private final Integer minReadLength;
    private final Integer maxReadLength;
    private final BigDecimal libraryMaxRank;
    private final Integer libraryDistinctRankCount;

    public RnaSeqLibraryPipelineSummary (BigDecimal meanRefIntergenicDistribution,
            BigDecimal sdRefIntergenicDistribution, BigDecimal pValueThreshold,
            Integer allReadsCount, Integer allUMIsCount, Integer mappedReadsCount,
            Integer mappedUMIsCount, Integer minReadLength, Integer maxReadLength,
            BigDecimal libraryMaxRank, Integer libraryDistinctRankCount) {
        this.meanRefIntergenicDistribution = meanRefIntergenicDistribution;
        this.sdRefIntergenicDistribution = sdRefIntergenicDistribution;
        this.pValueThreshold = pValueThreshold;
        this.allReadsCount = allReadsCount;
        this.allUMIsCount = allUMIsCount;
        this.mappedReadsCount = mappedReadsCount;
        this.mappedUMIsCount = mappedUMIsCount;
        this.minReadLength = minReadLength;
        this.maxReadLength = maxReadLength;
        this.libraryMaxRank = libraryMaxRank;
        this.libraryDistinctRankCount = libraryDistinctRankCount;
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
    public BigDecimal getLibraryMaxRank() {
        return libraryMaxRank;
    }
    public Integer getLibraryDistinctRankCount() {
        return libraryDistinctRankCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allReadsCount, allUMIsCount, libraryDistinctRankCount, libraryMaxRank, mappedReadsCount,
                mappedUMIsCount, maxReadLength, meanRefIntergenicDistribution, minReadLength, pValueThreshold,
                sdRefIntergenicDistribution);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RnaSeqLibraryPipelineSummary other = (RnaSeqLibraryPipelineSummary) obj;
        return Objects.equals(allReadsCount, other.allReadsCount) && Objects.equals(allUMIsCount, other.allUMIsCount)
                && Objects.equals(libraryDistinctRankCount, other.libraryDistinctRankCount)
                && Objects.equals(libraryMaxRank, other.libraryMaxRank)
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
                + minReadLength + ", maxReadLength=" + maxReadLength + ", libraryMaxRank=" + libraryMaxRank
                + ", libraryDistinctRankCount=" + libraryDistinctRankCount + "]";
    }

    
}
