package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DAOFDRPValue extends DAOBigDecimalLinkedToDataTypes {
    private final static Logger log = LogManager.getLogger(DAOFDRPValue.class.getName());
    
    //Only the dataTypes are considered for the hashCode/equals methods
    private final Integer conditionId;

    public DAOFDRPValue(BigDecimal fdrPValue, Collection<DAODataType> dataTypes) {
        this(fdrPValue, null, dataTypes);
    }
    public DAOFDRPValue(BigDecimal fdrPValue, Integer conditionId, Collection<DAODataType> dataTypes) {
        super(fdrPValue, dataTypes);
        if (fdrPValue.compareTo(new BigDecimal(0)) == -1 || fdrPValue.compareTo(new BigDecimal(1)) == 1) {
            throw log.throwing(new IllegalArgumentException(
                    "fdrPValue cannot be null or less than 0 or greater than 1"));
        }
        this.conditionId = conditionId;
    }
    
    /**
     * @return  A {@code BigDecimal} that is the FDR-corrected p-value.
     *          Not taken into account in hashCode/equals methods.
     */
    public BigDecimal getFdrPValue() {
        return super.getValue();
    }
    /**
     * @return  An {@code Integer} that is the globalConditionId where this p-value has been observed.
     *          Useful when inserting/retrieving the best p-value among descendant conditions.
     *          Not taken into account in hashCode/equals methods.
     */
    public Integer getConditionId() {
        return conditionId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOFDRPValue [fdrPValue=").append(this.getFdrPValue())
               .append(", conditionId=").append(conditionId)
               .append(", dataTypes=").append(this.getDataTypes())
               .append("]");
        return builder.toString();
    }
}
