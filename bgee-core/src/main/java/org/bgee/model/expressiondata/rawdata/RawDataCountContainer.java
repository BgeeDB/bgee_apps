package org.bgee.model.expressiondata.rawdata;

import java.util.EnumSet;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.DataContainer;

/**
 * A class allowing to contain count for all potential raw data resulting
 * from a query to {@code RawDataContainerService}
 * 
 * @author Julien Wollbrett
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RawDataCountContainer extends DataContainer {
    private final static Logger log = LogManager.getLogger(RawDataCountContainer.class.getName());

    private static EnumSet<DataType> computeRequestedDataTypes(
            Integer insituExperimentCount, Integer insituAssayCount, Integer insituCallCount,
            Integer bulkRnaSeqExperimentCount,  Integer bulkRnaSeqAssayCount,
            Integer bulkRnaSeqLibraryCount,  Integer bulkRnaSeqCallCount,
            Integer singleCellRnaSeqExperimentCount, Integer singleCellRnaSeqAssayCount,
            Integer singleCellRnaSeqLibraryCount, Integer singleCellRnaSeqCallCount) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                insituExperimentCount, insituAssayCount, insituCallCount,
                bulkRnaSeqExperimentCount, bulkRnaSeqAssayCount,
                bulkRnaSeqLibraryCount, bulkRnaSeqCallCount,
                singleCellRnaSeqExperimentCount, singleCellRnaSeqAssayCount,
                singleCellRnaSeqLibraryCount, singleCellRnaSeqCallCount);

        EnumSet<DataType> requestedDataTypes = EnumSet.noneOf(DataType.class);
        if (insituExperimentCount != null ||
                insituAssayCount != null ||
                insituCallCount != null) {
            requestedDataTypes.add(DataType.IN_SITU);
        }
        if (bulkRnaSeqExperimentCount != null ||
                bulkRnaSeqAssayCount != null ||
                bulkRnaSeqLibraryCount != null ||
                bulkRnaSeqCallCount != null) {
            requestedDataTypes.add(DataType.RNA_SEQ);
        }
        if (singleCellRnaSeqExperimentCount != null ||
                singleCellRnaSeqAssayCount != null ||
                singleCellRnaSeqLibraryCount != null ||
                singleCellRnaSeqCallCount != null) {
            requestedDataTypes.add(DataType.SC_RNA_SEQ);
        }

        return log.traceExit(requestedDataTypes);
    }
    private static EnumSet<DataType> computeDataTypesWithResults(
            Integer insituExperimentCount, Integer insituAssayCount, Integer insituCallCount,
            Integer bulkRnaSeqExperimentCount,  Integer bulkRnaSeqAssayCount,
            Integer bulkRnaSeqLibraryCount,  Integer bulkRnaSeqCallCount,
            Integer singleCellRnaSeqExperimentCount, Integer singleCellRnaSeqAssayCount,
            Integer singleCellRnaSeqLibraryCount, Integer singleCellRnaSeqCallCount) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                insituExperimentCount, insituAssayCount, insituCallCount,
                bulkRnaSeqExperimentCount, bulkRnaSeqAssayCount,
                bulkRnaSeqLibraryCount, bulkRnaSeqCallCount,
                singleCellRnaSeqExperimentCount, singleCellRnaSeqAssayCount,
                singleCellRnaSeqLibraryCount, singleCellRnaSeqCallCount);

        EnumSet<DataType> dataTypesWithResults = EnumSet.noneOf(DataType.class);
        if (insituExperimentCount != null && insituExperimentCount != 0 ||
                insituAssayCount != null && insituAssayCount != 0 ||
                insituCallCount != null && insituCallCount != 0) {
            dataTypesWithResults.add(DataType.IN_SITU);
        }
        if (bulkRnaSeqExperimentCount != null && bulkRnaSeqExperimentCount != 0 ||
                bulkRnaSeqAssayCount != null && bulkRnaSeqAssayCount != 0 ||
                bulkRnaSeqLibraryCount != null && bulkRnaSeqLibraryCount != 0 ||
                bulkRnaSeqCallCount != null && bulkRnaSeqCallCount != 0) {
            dataTypesWithResults.add(DataType.RNA_SEQ);
        }
        if (singleCellRnaSeqExperimentCount != null && singleCellRnaSeqExperimentCount != 0 ||
                singleCellRnaSeqAssayCount != null && singleCellRnaSeqAssayCount != 0 ||
                singleCellRnaSeqLibraryCount != null && singleCellRnaSeqLibraryCount != 0 ||
                singleCellRnaSeqCallCount != null && singleCellRnaSeqCallCount != 0) {
            dataTypesWithResults.add(DataType.SC_RNA_SEQ);
        }

        return log.traceExit(dataTypesWithResults);
    }

    private final Integer insituExperimentCount;
    private final Integer insituAssayCount;
    private final Integer insituCallCount;
    private final Integer bulkRnaSeqExperimentCount;
    private final Integer bulkRnaSeqAssayCount;
    private final Integer bulkRnaSeqLibraryCount;
    private final Integer bulkRnaSeqCallCount;
    private final Integer singleCellRnaSeqExperimentCount;
    private final Integer singleCellRnaSeqAssayCount;
    private final Integer singleCellRnaSeqLibraryCount;
    private final Integer singleCellRnaSeqCallCount;
    
    // null if not queried and 0 if no results
    public RawDataCountContainer(Integer insituExperimentCount, Integer insituAssayCount,
            Integer insituCallCount,
            Integer bulkRnaSeqExperimentCount,  Integer bulkRnaSeqAssayCount,
            Integer bulkRnaSeqLibraryCount,  Integer bulkRnaSeqCallCount,
            Integer singleCellRnaSeqExperimentCount, Integer singleCellRnaSeqAssayCount,
            Integer singleCellRnaSeqLibraryCount, Integer singleCellRnaSeqCallCount) {
        super(computeRequestedDataTypes(
                insituExperimentCount, insituAssayCount, insituCallCount,
                bulkRnaSeqExperimentCount, bulkRnaSeqAssayCount,
                bulkRnaSeqLibraryCount, bulkRnaSeqCallCount,
                singleCellRnaSeqExperimentCount, singleCellRnaSeqAssayCount,
                singleCellRnaSeqLibraryCount, singleCellRnaSeqCallCount),
              computeDataTypesWithResults(
                insituExperimentCount, insituAssayCount, insituCallCount,
                bulkRnaSeqExperimentCount, bulkRnaSeqAssayCount,
                bulkRnaSeqLibraryCount, bulkRnaSeqCallCount,
                singleCellRnaSeqExperimentCount, singleCellRnaSeqAssayCount,
                singleCellRnaSeqLibraryCount, singleCellRnaSeqCallCount));

        this.insituExperimentCount = insituExperimentCount;
        this.insituAssayCount = insituAssayCount;
        this.insituCallCount = insituCallCount;

        this.bulkRnaSeqExperimentCount = bulkRnaSeqExperimentCount;
        this.bulkRnaSeqAssayCount = bulkRnaSeqAssayCount;
        this.bulkRnaSeqLibraryCount = bulkRnaSeqLibraryCount;
        this.bulkRnaSeqCallCount = bulkRnaSeqCallCount;

        this.singleCellRnaSeqExperimentCount = singleCellRnaSeqExperimentCount;
        this.singleCellRnaSeqAssayCount = singleCellRnaSeqAssayCount;
        this.singleCellRnaSeqLibraryCount = singleCellRnaSeqLibraryCount;
        this.singleCellRnaSeqCallCount = singleCellRnaSeqCallCount;
    }

    public Integer getInsituExperimentCount() {
        return insituExperimentCount;
    }
    public Integer getInsituAssayCount() {
        return insituAssayCount;
    }
    public Integer getInsituCallCount() {
        return insituCallCount;
    }
     public Integer getBulkRnaSeqExperimentCount() {
        return bulkRnaSeqExperimentCount;
    }
    public Integer getBulkRnaSeqAssayCount() {
        return bulkRnaSeqAssayCount;
    }
    public Integer getBulkRnaSeqLibraryCount() {
        return bulkRnaSeqLibraryCount;
    }
    public Integer getBulkRnaSeqCallCount() {
        return bulkRnaSeqCallCount;
    }
    public Integer getSingleCellRnaSeqExperimentCount() {
        return singleCellRnaSeqExperimentCount;
    }
    public Integer getSingleCellRnaSeqAssayCount() {
        return singleCellRnaSeqAssayCount;
    }
    public Integer getSingleCellRnaSeqLibraryCount() {
        return singleCellRnaSeqLibraryCount;
    }
    public Integer getSingleCellRnaSeqCallCount() {
        return singleCellRnaSeqCallCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(
                bulkRnaSeqAssayCount, bulkRnaSeqCallCount, bulkRnaSeqExperimentCount, bulkRnaSeqLibraryCount,
                insituAssayCount, insituCallCount, insituExperimentCount,
                singleCellRnaSeqAssayCount, singleCellRnaSeqCallCount, singleCellRnaSeqExperimentCount,
                singleCellRnaSeqLibraryCount);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataCountContainer other = (RawDataCountContainer) obj;
        return Objects.equals(bulkRnaSeqAssayCount, other.bulkRnaSeqAssayCount)
                && Objects.equals(bulkRnaSeqCallCount, other.bulkRnaSeqCallCount)
                && Objects.equals(bulkRnaSeqExperimentCount, other.bulkRnaSeqExperimentCount)
                && Objects.equals(bulkRnaSeqLibraryCount, other.bulkRnaSeqLibraryCount)
                && Objects.equals(insituAssayCount, other.insituAssayCount)
                && Objects.equals(insituCallCount, other.insituCallCount)
                && Objects.equals(insituExperimentCount, other.insituExperimentCount)
                && Objects.equals(singleCellRnaSeqAssayCount, other.singleCellRnaSeqAssayCount)
                && Objects.equals(singleCellRnaSeqCallCount, other.singleCellRnaSeqCallCount)
                && Objects.equals(singleCellRnaSeqExperimentCount, other.singleCellRnaSeqExperimentCount)
                && Objects.equals(singleCellRnaSeqLibraryCount, other.singleCellRnaSeqLibraryCount);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCountContainer [")
               .append(", insituExperimentCount=").append(insituExperimentCount)
               .append(", insituAssayCount=").append(insituAssayCount)
               .append(", insituCallCount=").append(insituCallCount)
               .append(", bulkRnaSeqExperimentCount=").append(bulkRnaSeqExperimentCount)
               .append(", bulkRnaSeqAssayCount=").append(bulkRnaSeqAssayCount)
               .append(", bulkRnaSeqCallCount=").append(bulkRnaSeqCallCount)
               .append(", singleCellRnaSeqExperimentCount=").append(singleCellRnaSeqExperimentCount)
               .append(", singleCellRnaSeqAssayCount=").append(singleCellRnaSeqAssayCount)
               .append(", singleCellRnaSeqLibraryCount=").append(singleCellRnaSeqLibraryCount)
               .append(", singleCellRnaSeqCallCount=").append(singleCellRnaSeqCallCount)
               .append(", requestedDataTypes=").append(this.getRequestedDataTypes())
               .append(", dataTypesWithResults=").append(this.getDataTypesWithResults())
               .append("]");
        return builder.toString();
    }
}
