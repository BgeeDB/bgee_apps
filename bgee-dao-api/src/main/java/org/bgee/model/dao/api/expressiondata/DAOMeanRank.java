package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

public class DAOMeanRank extends DAOBigDecimalLinkedToDataTypes {

    public DAOMeanRank(BigDecimal meanRank, Collection<DAODataType> dataTypes) {
        super(meanRank, dataTypes);
        if (meanRank.compareTo(new BigDecimal(1)) == -1) {
            throw new IllegalArgumentException("meanRank cannot be less than 1");
        }
    }

    /**
     * @return  A {@code BigDecimal} that is the mean rank computed for the selection
     *          of {@code DAODataType}s returned by {@link #getDataTypes()}.
     *          Not taken into account in hashCode/equals methods.
     */
    public BigDecimal getMeanRank() {
        return super.getValue();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOMeanRank [meanRank=").append(this.getMeanRank())
               .append(", dataTypes=").append(this.getDataTypes())
               .append("]");
        return builder.toString();
    }
}
