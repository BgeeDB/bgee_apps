package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;

public class ExpressionObservation {

    private final BigDecimal pValue;
    private final Integer numberObservation;
    private final BigDecimal rank;
    private final BigDecimal weight;

    public ExpressionObservation(BigDecimal pValue, Integer numberObservation, BigDecimal rank,
            BigDecimal weight) {
        this.numberObservation = numberObservation;
        this.pValue = pValue;
        this.rank = rank;
        this.weight = weight;
    }

    public BigDecimal getpValue() {
        return pValue;
    }
    public Integer getNumberObservation() {
        return numberObservation;
    }
    public BigDecimal getRank() {
        return rank;
    }
    public BigDecimal getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "ExpressionObservation [pValue=" + pValue + ", numberObservation=" + numberObservation + ", rank=" + rank
                + ", weight=" + weight + "]";
    }

}
