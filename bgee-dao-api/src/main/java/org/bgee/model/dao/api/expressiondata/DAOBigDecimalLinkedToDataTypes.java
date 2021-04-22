package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Parent class of all classes that need to store a {@code BigDecimal} value and that depends
 * on a selection of {@code DAODataType}s.
 * <p>
 *  Only the dataTypes are considered for the hashCode/equals methods.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since Bgee 15.0, Apr. 2021
 */
public abstract class DAOBigDecimalLinkedToDataTypes {
    private final BigDecimal value;
    private final EnumSet<DAODataType> dataTypes;

    public DAOBigDecimalLinkedToDataTypes(BigDecimal value, Collection<DAODataType> dataTypes) {
        if (value == null) {
            throw new IllegalArgumentException("A value must be provided");
        }
        if (dataTypes == null || dataTypes.isEmpty()) {
            throw new IllegalArgumentException("Some data types must be provided");
        }
        this.value = value;
        this.dataTypes = EnumSet.copyOf(dataTypes);
    }

    //Subclasses will rename this method, this is why it is protected.
    protected BigDecimal getValue() {
        return value;
    }
    public EnumSet<DAODataType> getDataTypes() {
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
        DAOBigDecimalLinkedToDataTypes other = (DAOBigDecimalLinkedToDataTypes) obj;
        if (dataTypes == null) {
            if (other.dataTypes != null) {
                return false;
            }
        } else if (!dataTypes.equals(other.dataTypes)) {
            return false;
        }
        return true;
    }
}