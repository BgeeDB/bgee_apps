package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;

public abstract class DataContainer {

    private final EnumSet<DataType> requestedDataTypes;
    private final EnumSet<DataType> dataTypesWithResults;

    protected DataContainer(EnumSet<DataType> requestedDataTypes, EnumSet<DataType> dataTypesWithResults) {
        //No sanity checks, these values are computed by implementing classes.
        //And for dataTypesWithResults, an empty EnumSet means that there was no result,
        //we don't want to replace this value with 'EnumSet.allOf' as we usually do.
        //We will use defensive copying in the getters
        this.requestedDataTypes = requestedDataTypes;
        this.dataTypesWithResults = dataTypesWithResults;
    }

    /**
     * @return  An {@code EnumSet} of {@code DataType}s specifying the data types that were requested.
     *          This {@code EnumSet} is a copy of the attribute (defensive copying).
     */
    public EnumSet<DataType> getRequestedDataTypes() {
        //defensive copying
        return EnumSet.copyOf(this.requestedDataTypes);
    }
    /**
     * @return  An {@code EnumSet} of {@code DataType}s specifying the data types
     *          for which results exist.
     *          This {@code EnumSet} is a copy of the attribute (defensive copying).
     */
    public EnumSet<DataType> getDataTypesWithResults() {
        //defensive copying
        return EnumSet.copyOf(this.dataTypesWithResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataTypesWithResults, requestedDataTypes);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataContainer other = (DataContainer) obj;
        return Objects.equals(dataTypesWithResults, other.dataTypesWithResults)
                && Objects.equals(requestedDataTypes, other.requestedDataTypes);
    }
}
