package org.bgee.model.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.util.Objects;

import org.bgee.model.Entity;

public class ChipType extends Entity<String>{

    private final String name;
    private final String cdfName;
    private final boolean isCompatible;
    private final BigDecimal qualityScoreThreshold;
    private final BigDecimal percentPresentThreshold;
    private final BigDecimal maxRank;

    public ChipType(String id, String name, String cdfName, boolean isCompatible, BigDecimal qualityScoreThreshold,
            BigDecimal percentPresentThreshold, BigDecimal maxRank) throws IllegalArgumentException {
        super(id);
        this.name = name;
        this.cdfName = cdfName;
        this.isCompatible = isCompatible;
        this.qualityScoreThreshold = qualityScoreThreshold;
        this.percentPresentThreshold = percentPresentThreshold;
        this.maxRank = maxRank;
    }


    public String getName() {
        return name;
    }

    public String getCdfName() {
        return cdfName;
    }

    public boolean isCompatible() {
        return isCompatible;
    }

    public BigDecimal getQualityScoreThreshold() {
        return qualityScoreThreshold;
    }

    public BigDecimal getPercentPresentThreshold() {
        return percentPresentThreshold;
    }

    public BigDecimal getMaxRank() {
        return maxRank;
    }

    //ChiType IDs are unique. Reuse hashCode/equals from 'Entity'.
    @Override
    public String toString() {
        return "ChipType [name=" + name + ", cdfName=" + cdfName + ", isCompatible=" + isCompatible
                + ", qualityScoreThreshold=" + qualityScoreThreshold + ", percentPresentThreshold="
                + percentPresentThreshold + ", maxRank=" + maxRank + "]";
    }

}
