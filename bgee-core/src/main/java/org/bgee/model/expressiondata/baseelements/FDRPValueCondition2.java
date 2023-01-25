package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.expressiondata.call.Condition2;

public class FDRPValueCondition2 extends FDRPValue {

    private final Condition2 condition;

    public FDRPValueCondition2(BigDecimal fdrPValue, Collection<DataType> dataTypes,
            Condition2 condition) {
        super(fdrPValue, dataTypes);
        this.condition = condition;
    }

    /**
     * @return  A {@code Condition} that is were this FDR p-value has been computed for.
     */
    public Condition2 getCondition() {
        return condition;
    }

    //no equals/hashCode implementation, we rely on the methods from FDRPValue
    //that only uses the DataTypes for that


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FDRPValueCondition [fdrPValue=").append(getPValue())
        .append(", condition=").append(getCondition())
        .append(", dataTypes=").append(getDataTypes())
        .append("]");
        return builder.toString();
    }
}
