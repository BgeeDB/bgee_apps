package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.expressiondata.call.Condition;

/**
 * Store a FDR corrected p-value, produced by retrieving all p-values
 * resulting from tests to detect active signal of expression of a gene in the {@code Condition}
 * stored in this class and its descendant conditions, using the {@code DataType}s
 * stored in this class. This class is meant to be used for identifying the descendant condition
 * that produced the best descendant FDR-corrected p-value for a call of a gene in a parent condition.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Mar. 2021
 * @since Bgee 15.0, Mar. 2021
 */
public class FDRPValueCondition extends FDRPValue {

    private final Condition condition;

    public FDRPValueCondition(BigDecimal fdrPValue, Collection<DataType> dataTypes,
            Condition condition) {
        super(fdrPValue, dataTypes);
        this.condition = condition;
    }

    /**
     * @return  A {@code Condition} that is were this FDR p-value has been computed for.
     */
    public Condition getCondition() {
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