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
 * @version Bgee 15.0, Jan. 2023
 * @since Bgee 15.0, Mar. 2021
 */
public class FDRPValue extends PValue {
    private final static Logger log = LogManager.getLogger(FDRPValue.class.getName());

    //Only the dataTypes are considered for the hashCode/equals methods
    private final EnumSet<DataType> dataTypes;

    public FDRPValue(BigDecimal fdrPValue, Collection<DataType> dataTypes) {
        super(fdrPValue);
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some data types must be provided"));
        }
        this.dataTypes = EnumSet.copyOf(dataTypes);
    }

    public EnumSet<DataType> getDataTypes() {
        //Defensive copying, there is no unmodifiableEnumSet
        return EnumSet.copyOf(dataTypes);
    }

    /**
     * Note that only the data types returned by {@link #getDataTypes()} are taken into account
     * for {@code hashCode()} and {@code equals()}.
     */
    @Override
    //We don't use super.hashCode() because only the dataTypes
    //are considered for the hashCode/equals methods
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataTypes == null) ? 0 : dataTypes.hashCode());
        return result;
    }
    /**
     * Note that only the data types returned by {@link #getDataTypes()} are taken into account
     * for {@code hashCode()} and {@code equals()}.
     */
    @Override
    //We don't use super.hashCode() because only the dataTypes
    //are considered for the hashCode/equals methods
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
        builder.append("FDRPValue [fdrPValue=").append(getPValue())
        .append(", dataTypes=").append(dataTypes).append("]");
        return builder.toString();
    }
}