package org.bgee.model.dao.api.expressiondata.rawdata.call;

import java.math.BigDecimal;
import java.util.Objects;

public class DAORawCallValues {
    private final BigDecimal score;
    private final BigDecimal pValue;
    private final BigDecimal weight;

    public DAORawCallValues(BigDecimal score, BigDecimal pValue, BigDecimal weight) {
        this.score = score;
        this.pValue = pValue;
        this.weight = weight;
    }

    public BigDecimal getScore() {
        return score;
    }

    public BigDecimal getpValue() {
        return pValue;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "DAODRawCallValues [score=" + score + ", pValue=" + pValue + ", weight=" + weight + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(pValue, score, weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAORawCallValues other = (DAORawCallValues) obj;
        return Objects.equals(pValue, other.pValue) && Objects.equals(score, other.score)
                && Objects.equals(weight, other.weight);
    }

}
