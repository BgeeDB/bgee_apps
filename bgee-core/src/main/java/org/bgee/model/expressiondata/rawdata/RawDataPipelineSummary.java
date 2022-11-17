package org.bgee.model.expressiondata.rawdata;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Class allowing to store summaries of pipeline steps common to all datatypes
 * in Bgee.
 *
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 *
 */
public class RawDataPipelineSummary {

    private final Integer distinctRankCount;
    private final BigDecimal maxRank;

    public RawDataPipelineSummary(Integer distinctRankCount, BigDecimal maxRank) {
        this.distinctRankCount = distinctRankCount;
        this.maxRank = maxRank;
    }

    public Integer getDistinctRankCount() {
        return distinctRankCount;
    }
    public BigDecimal getMaxRank() {
        return maxRank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distinctRankCount, maxRank);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataPipelineSummary other = (RawDataPipelineSummary) obj;
        return Objects.equals(distinctRankCount, other.distinctRankCount) &&
                Objects.equals(maxRank, other.maxRank);
    }

    @Override
    public String toString() {
        return "RawDataPipelineSummary [distinctRankCount=" + distinctRankCount + ", maxRank="
                + maxRank + "]";
    }


    

}
