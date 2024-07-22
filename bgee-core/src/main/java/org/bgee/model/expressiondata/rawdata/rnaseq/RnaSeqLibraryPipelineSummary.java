package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Objects;

public class RnaSeqLibraryPipelineSummary {


    private final Long allReadsCount;
    private final Long mappedReadsCount;
    private final Integer minReadLength;
    private final Integer maxReadLength;

    public RnaSeqLibraryPipelineSummary (Long allReadsCount, Long mappedReadsCount,
           Integer minReadLength, Integer maxReadLength) {
        this.allReadsCount = allReadsCount;
        this.mappedReadsCount = mappedReadsCount;
        this.minReadLength = minReadLength;
        this.maxReadLength = maxReadLength;
    }

    public Long getAllReadsCount() {
        return allReadsCount;
    }
    public Long getMappedReadsCount() {
        return mappedReadsCount;
    }
    public Integer getMinReadLength() {
        return minReadLength;
    }
    public Integer getMaxReadLength() {
        return maxReadLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allReadsCount, mappedReadsCount, maxReadLength, minReadLength);
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
        return Objects.equals(allReadsCount, other.allReadsCount)
                && Objects.equals(mappedReadsCount, other.mappedReadsCount)
                && Objects.equals(maxReadLength, other.maxReadLength)
                && Objects.equals(minReadLength, other.minReadLength);
    }

    @Override
    public String toString() {
        return "RnaSeqLibraryPipelineSummary [allReadsCount=" + allReadsCount + ", mappedReadsCount=" + mappedReadsCount
                + ", minReadLength=" + minReadLength + ", maxReadLength=" + maxReadLength + "]";
    }

}
