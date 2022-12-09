package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.est.ESTDataType;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataType;
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
//Note: if we create data-type specific RawDataPostFilters, it should also be added
//as a generic type to this class for convenient casting of returned filters in RawDataService
public abstract class RawDataDataType<T extends RawDataContainer<?, ?>, U extends RawDataCountContainer> {
    private final static Logger log = LogManager.getLogger(RawDataDataType.class.getName());

    public final static AffymetrixDataType AFFYMETRIX = new AffymetrixDataType();
    public final static RnaSeqDataType BULK_RNA_SEQ = new RnaSeqDataType(DataType.RNA_SEQ);
    public final static RnaSeqDataType SC_RNA_SEQ = new RnaSeqDataType(DataType.FULL_LENGTH);
    public final static ESTDataType EST = new ESTDataType();
    public final static InSituDataType IN_SITU = new InSituDataType();

    private final static Map<DataType, RawDataDataType<?, ?>> DATA_TYPE_TO_RAW_DATA_DATA_TYPE =
            Collections.unmodifiableMap(Map.ofEntries(
                    Map.entry(AFFYMETRIX.getDataType(), AFFYMETRIX),
                    Map.entry(BULK_RNA_SEQ.getDataType(), BULK_RNA_SEQ),
                    Map.entry(SC_RNA_SEQ.getDataType(), SC_RNA_SEQ),
                    Map.entry(EST.getDataType(), EST),
                    Map.entry(IN_SITU.getDataType(), IN_SITU)));

    private final static Map<DataType,
    RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>,
            ? extends RawDataCountContainerWithExperiment>>
    DATA_TYPE_TO_RAW_DATA_DATA_TYPE_WITH_EXPERIMENT = Collections.unmodifiableMap(Map.ofEntries(
                    Map.entry(AFFYMETRIX.getDataType(), AFFYMETRIX),
                    Map.entry(BULK_RNA_SEQ.getDataType(), BULK_RNA_SEQ),
                    Map.entry(SC_RNA_SEQ.getDataType(), SC_RNA_SEQ),
                    Map.entry(IN_SITU.getDataType(), IN_SITU)));

    public static RawDataDataType<?, ?> getRawDataDataType(DataType dataType) {
        log.traceEntry("{}", dataType);
        RawDataDataType<?, ?> rawDataDataType = DATA_TYPE_TO_RAW_DATA_DATA_TYPE.get(dataType);
        if (rawDataDataType == null) {
            throw log.throwing(new IllegalStateException("Unmapped DataType: " + dataType));
        }
        return log.traceExit(rawDataDataType);
    }
    public static RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>,
            ? extends RawDataCountContainerWithExperiment>
    getRawDataDataTypeWithExperiment(DataType dataType) {
        log.traceEntry("{}", dataType);
        return log.traceExit(DATA_TYPE_TO_RAW_DATA_DATA_TYPE_WITH_EXPERIMENT.get(dataType));
    }

    private final DataType dataType;
    private final Class<T> rawDataContainerClass;
    private final Class<U> rawDataCountContainerClass;

    protected RawDataDataType(DataType dataType, Class<T> rawDataContainerClass,
            Class<U> rawDataCountContainerClass) {
        if (dataType == null) {
            throw new IllegalArgumentException("Data type cannot be null");
        }
        if (rawDataContainerClass == null) {
            throw new IllegalArgumentException("rawDataContainerClass cannot be null");
        }
        if (rawDataCountContainerClass == null) {
            throw new IllegalArgumentException("rawDataCountContainerClass cannot be null");
        }
        this.dataType = dataType;
        this.rawDataContainerClass = rawDataContainerClass;
        this.rawDataCountContainerClass = rawDataCountContainerClass;
    }

    //It IS checked
    @SuppressWarnings("unchecked")
    public RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>,
            ? extends RawDataCountContainerWithExperiment> castToDataTypeWithExperiment() {
        log.traceEntry();
        if (RawDataContainerWithExperiment.class.isAssignableFrom(
                this.getRawDataContainerClass())) {
            assert RawDataCountContainerWithExperiment.class.isAssignableFrom(
                    this.getRawDataCountContainerClass());
            return (RawDataDataType<? extends RawDataContainerWithExperiment<?, ?, ?>,
                    ? extends RawDataCountContainerWithExperiment>) this;
        }
        throw log.throwing(new ClassCastException("RawDataDataType not with experiment"));
    }

    /**
     * @return  The {@code DataType} that corresponds to this {@code RawDataType}.
     */
    public DataType getDataType() {
        return dataType;
    }
    public Class<T> getRawDataContainerClass() {
        return rawDataContainerClass;
    }
    public Class<U> getRawDataCountContainerClass() {
        return rawDataCountContainerClass;
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