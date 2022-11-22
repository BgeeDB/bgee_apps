package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixDataType;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqDataType;

/**
 * Class used over {@link org.bgee.model.expressiondata.baseelements.DataType DataType}
 * to specify generic types per data types, for convenient generic type casting
 * of other objects. Therefore, each data type needs to be a specific class,
 * and not an {@code enum} as for {@code DataType}.
 * <p>
 * Each {@code RawDataDataType} is associated with an underlying {@code DataType}
 * (see {@link #getDataType()}).
 * <p>
 * Convenient static public attributes are provided to obtain data-type specific implementations,
 * in order to mimic an {@code enum}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 *
 * @param <T>   The type of {@code RawDataContainer} containing raw data results
 *              for the underlying data type.
 * @param <U>   The type of {@code RawDataCountContainer} containing raw data result counts
 *              for the underlying data type.
 */
public abstract class RawDataDataType<T extends RawDataContainer<?, ?>, U extends RawDataCountContainer> {

    public final static AffymetrixDataType AFFYMETRIX = new AffymetrixDataType();
    public final static RnaSeqDataType BULK_RNA_SEQ = new RnaSeqDataType(DataType.RNA_SEQ);
    public final static RnaSeqDataType SC_RNA_SEQ = new RnaSeqDataType(DataType.FULL_LENGTH);

    private final DataType dataType;

    protected RawDataDataType(DataType dataType) {
        if (dataType == null) {
            throw new IllegalArgumentException("Data type cannot be null");
        }
        this.dataType = dataType;
    }

    /**
     * @return  The {@code DataType} that corresponds to this {@code RawDataType}.
     */
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataDataType<?, ?> other = (RawDataDataType<?, ?>) obj;
        return dataType == other.dataType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataDataType [dataType=").append(dataType).append("]");
        return builder.toString();
    }
}