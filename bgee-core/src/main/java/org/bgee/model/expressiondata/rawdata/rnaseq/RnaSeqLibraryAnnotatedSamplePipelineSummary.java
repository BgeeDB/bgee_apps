package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Objects;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataPipelineSummary;

public class RnaSeqLibraryAnnotatedSamplePipelineSummary extends RawDataPipelineSummary{

    private final BigDecimal meanRefIntergenicDistribution;
    private final BigDecimal sdRefIntergenicDistribution;
    private final BigDecimal pValueThreshold;
    private final Integer allUMIsCount;
    private final Integer mappedUMIsCount;

    public RnaSeqLibraryAnnotatedSamplePipelineSummary (BigDecimal meanRefIntergenicDistribution,
            BigDecimal sdRefIntergenicDistribution, BigDecimal pValueThreshold,
            Integer allUMIsCount, Integer mappedUMIsCount,
            BigDecimal libraryMaxRank, Integer libraryDistinctRankCount) {
        super(libraryDistinctRankCount, libraryMaxRank);
        this.meanRefIntergenicDistribution = meanRefIntergenicDistribution;
        this.sdRefIntergenicDistribution = sdRefIntergenicDistribution;
        this.pValueThreshold = pValueThreshold;
        this.allUMIsCount = allUMIsCount;
        this.mappedUMIsCount = mappedUMIsCount;
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
    public Integer getAllUMIsCount() {
        return allUMIsCount;
    }
    public Integer getMappedUMIsCount() {
        return mappedUMIsCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(allUMIsCount, mappedUMIsCount, meanRefIntergenicDistribution,
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
        RnaSeqLibraryAnnotatedSamplePipelineSummary other = (RnaSeqLibraryAnnotatedSamplePipelineSummary) obj;
        return Objects.equals(allUMIsCount, other.allUMIsCount)
                && Objects.equals(mappedUMIsCount, other.mappedUMIsCount)
                && Objects.equals(meanRefIntergenicDistribution, other.meanRefIntergenicDistribution)
                && Objects.equals(pValueThreshold, other.pValueThreshold)
                && Objects.equals(sdRefIntergenicDistribution, other.sdRefIntergenicDistribution);
    }

    @Override
    public String toString() {
        return "RnaSeqLibraryAnnotatedSamplePipelineSummary [meanRefIntergenicDistribution="
                + meanRefIntergenicDistribution + ", sdRefIntergenicDistribution=" + sdRefIntergenicDistribution
                + ", pValueThreshold=" + pValueThreshold + ", allUMIsCount=" + allUMIsCount + ", mappedUMIsCount="
                + mappedUMIsCount + ", getDistinctRankCount()=" + getDistinctRankCount() + ", getMaxRank()="
                + getMaxRank() + "]";
    }

}
