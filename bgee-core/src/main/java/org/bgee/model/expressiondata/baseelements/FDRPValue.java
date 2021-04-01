package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Store a FDR corrected p-value, produced by retrieving all p-values
 * resulting from tests to detect active signal of expression of a gene in a condition
 * and its descendant conditions, using the {@code DataType}s stored in this class.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Mar. 2021
 * @since Bgee 15.0, Mar. 2021
 */
public class FDRPValue {
    private final static Logger log = LogManager.getLogger(FDRPValue.class.getName());

    //Only the dataTypes are considered for the hashCode/equals methods
    private final BigDecimal fdrPValue;
    //Only the dataTypes are considered for the hashCode/equals methods
    private final EnumSet<DataType> dataTypes;

    public FDRPValue(BigDecimal fdrPValue, Collection<DataType> dataTypes) {
        if (fdrPValue == null || fdrPValue.compareTo(new BigDecimal(0)) == -1 ||
                fdrPValue.compareTo(new BigDecimal(1)) == 1) {
            throw log.throwing(new IllegalArgumentException(
                    "fdrPValue cannot be null or less than 0 or greater than 1"));
        }
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some data types must be provided"));
        }
        this.fdrPValue = fdrPValue;
        this.dataTypes = EnumSet.copyOf(dataTypes);
    }

    /**
     * @return  The FDR-corrected p-value. Not taken into account in hashCode/equals methods.
     */
    public BigDecimal getFdrPValue() {
        return fdrPValue;
    }
    public EnumSet<DataType> getDataTypes() {
        //Defensive copying, there is no unmodifiableEnumSet
        return EnumSet.copyOf(dataTypes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataTypes == null) ? 0 : dataTypes.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FDRPValue other = (FDRPValue) obj;
        if (dataTypes == null) {
            if (other.dataTypes != null) {
                return false;
            }
        } else if (!dataTypes.equals(other.dataTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FDRPValue [fdrPValue=").append(fdrPValue)
        .append(", dataTypes=").append(dataTypes).append("]");
        return builder.toString();
    }
}