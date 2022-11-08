package org.bgee.model.expressiondata.rawdata;

import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;

/**
 * A class allowing to contain count for all potential raw data resulting
 * from a query to {@code RawDataContainerService}
 * 
 * @author Julien Wollbrett
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RawDataCountContainer {

    private final Integer affymetrixExperimentCount;
    private final Integer affymetrixAssayCount;
    private final Integer affymetrixCallCount;
    private final Integer insituExperimentCount;
    private final Integer insituAssayCount;
    private final Integer insituCallCount;
    private final Integer estAssayCount;
    private final Integer estCallCount;
    private final Integer bulkRnaSeqExperimentCount;
    private final Integer bulkRnaSeqAssayCount;
    private final Integer bulkRnaSeqLibraryCount;
    private final Integer bulkRnaSeqCallCount;
    private final Integer singleCellRnaSeqExperimentCount;
    private final Integer singleCellRnaSeqAssayCount;
    private final Integer singleCellRnaSeqLibraryCount;
    private final Integer singleCellRnaSeqCallCount;

    private final EnumSet<DataType> requestedDataTypes;
    
    // null if not queried and 0 if no results
    public RawDataCountContainer(Integer affymetrixExperimentCount, Integer affymetrixAssayCount,
            Integer affymetrixCallCount, Integer insituExperimentCount, Integer insituAssayCount,
            Integer insituCallCount, Integer estAssayCount, Integer estCallCount,
            Integer bulkRnaSeqExperimentCount,  Integer bulkRnaSeqAssayCount,
            Integer bulkRnaSeqLibraryCount,  Integer bulkRnaSeqCallCount,
            Integer singleCellRnaSeqExperimentCount, Integer singleCellRnaSeqAssayCount,
            Integer singleCellRnaSeqLibraryCount, Integer singleCellRnaSeqCallCount) {
        EnumSet<DataType> dataTypes = EnumSet.noneOf(DataType.class);

        this.affymetrixExperimentCount = affymetrixExperimentCount;
        this.affymetrixAssayCount = affymetrixAssayCount;
        this.affymetrixCallCount = affymetrixCallCount;
        if (this.affymetrixExperimentCount != null ||
                this.affymetrixAssayCount != null ||
                this.affymetrixCallCount != null) {
            dataTypes.add(DataType.AFFYMETRIX);
        }

        this.insituExperimentCount = insituExperimentCount;
        this.insituAssayCount = insituAssayCount;
        this.insituCallCount = insituCallCount;
        if (this.insituExperimentCount != null ||
                this.insituAssayCount != null ||
                this.insituCallCount != null) {
            dataTypes.add(DataType.IN_SITU);
        }

        this.estAssayCount = estAssayCount;
        this.estCallCount = estCallCount;
        if (this.estAssayCount != null ||
                this.estCallCount != null) {
            dataTypes.add(DataType.EST);
        }

        this.bulkRnaSeqExperimentCount = bulkRnaSeqExperimentCount;
        this.bulkRnaSeqAssayCount = bulkRnaSeqAssayCount;
        this.bulkRnaSeqLibraryCount = bulkRnaSeqLibraryCount;
        this.bulkRnaSeqCallCount = bulkRnaSeqCallCount;
        if (this.bulkRnaSeqExperimentCount != null ||
                this.bulkRnaSeqAssayCount != null ||
                this.bulkRnaSeqLibraryCount != null ||
                this.bulkRnaSeqCallCount != null) {
            dataTypes.add(DataType.RNA_SEQ);
        }

        this.singleCellRnaSeqExperimentCount = singleCellRnaSeqExperimentCount;
        this.singleCellRnaSeqAssayCount = singleCellRnaSeqAssayCount;
        this.singleCellRnaSeqLibraryCount = singleCellRnaSeqLibraryCount;
        this.singleCellRnaSeqCallCount = singleCellRnaSeqCallCount;
        if (this.singleCellRnaSeqExperimentCount != null ||
                this.singleCellRnaSeqAssayCount != null ||
                this.singleCellRnaSeqLibraryCount != null ||
                this.singleCellRnaSeqCallCount != null) {
            dataTypes.add(DataType.FULL_LENGTH);
        }

        //We will use defensive copying in the getter, there is no unmodifiable EnumSet
        this.requestedDataTypes = dataTypes;
    }

    public Integer getAffymetrixExperimentCount() {
        return affymetrixExperimentCount;
    }
    public Integer getAffymetrixAssayCount() {
        return affymetrixAssayCount;
    }
    public Integer getAffymetrixCallCount() {
        return affymetrixCallCount;
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
    public Integer getEstAssayCount() {
        return estAssayCount;
    }
    public Integer getEstCallCount() {
        return estCallCount;
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

    public EnumSet<DataType> getRequestedDataTypes() {
        //Defensive copying, there is no unmodifiable EnumSet
        return EnumSet.copyOf(requestedDataTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(affymetrixAssayCount, affymetrixCallCount,
                affymetrixExperimentCount, bulkRnaSeqAssayCount,
                bulkRnaSeqCallCount, bulkRnaSeqExperimentCount, bulkRnaSeqLibraryCount,
                estAssayCount, estCallCount, insituAssayCount, insituCallCount,
                insituExperimentCount, requestedDataTypes, singleCellRnaSeqAssayCount,
                singleCellRnaSeqCallCount, singleCellRnaSeqExperimentCount,
                singleCellRnaSeqLibraryCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataCountContainer other = (RawDataCountContainer) obj;
        return Objects.equals(affymetrixAssayCount, other.affymetrixAssayCount)
                && Objects.equals(affymetrixCallCount, other.affymetrixCallCount)
                && Objects.equals(affymetrixExperimentCount, other.affymetrixExperimentCount)
                && Objects.equals(bulkRnaSeqAssayCount, other.bulkRnaSeqAssayCount)
                && Objects.equals(bulkRnaSeqCallCount, other.bulkRnaSeqCallCount)
                && Objects.equals(bulkRnaSeqExperimentCount, other.bulkRnaSeqExperimentCount)
                && Objects.equals(bulkRnaSeqLibraryCount, other.bulkRnaSeqLibraryCount)
                && Objects.equals(estAssayCount, other.estAssayCount)
                && Objects.equals(estCallCount, other.estCallCount)
                && Objects.equals(insituAssayCount, other.insituAssayCount)
                && Objects.equals(insituCallCount, other.insituCallCount)
                && Objects.equals(insituExperimentCount, other.insituExperimentCount)
                && Objects.equals(requestedDataTypes, other.requestedDataTypes)
                && Objects.equals(singleCellRnaSeqAssayCount, other.singleCellRnaSeqAssayCount)
                && Objects.equals(singleCellRnaSeqCallCount, other.singleCellRnaSeqCallCount)
                && Objects.equals(singleCellRnaSeqExperimentCount, other.singleCellRnaSeqExperimentCount)
                && Objects.equals(singleCellRnaSeqLibraryCount, other.singleCellRnaSeqLibraryCount);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCountContainer [")
               .append("affymetrixExperimentCount=").append(affymetrixExperimentCount)
               .append(", affymetrixAssayCount=").append(affymetrixAssayCount)
               .append(", affymetrixCallCount=").append(affymetrixCallCount)
               .append(", insituExperimentCount=").append(insituExperimentCount)
               .append(", insituAssayCount=").append(insituAssayCount)
               .append(", insituCallCount=").append(insituCallCount)
               .append(", estAssayCount=").append(estAssayCount)
               .append(", estCallCount=").append(estCallCount)
               .append(", bulkRnaSeqExperimentCount=").append(bulkRnaSeqExperimentCount)
               .append(", bulkRnaSeqAssayCount=").append(bulkRnaSeqAssayCount)
               .append(", bulkRnaSeqCallCount=").append(bulkRnaSeqCallCount)
               .append(", singleCellRnaSeqExperimentCount=").append(singleCellRnaSeqExperimentCount)
               .append(", singleCellRnaSeqAssayCount=").append(singleCellRnaSeqAssayCount)
               .append(", singleCellRnaSeqLibraryCount=").append(singleCellRnaSeqLibraryCount)
               .append(", singleCellRnaSeqCallCount=").append(singleCellRnaSeqCallCount)
               .append(", requestedDataTypes=").append(requestedDataTypes)
               .append("]");
        return builder.toString();
    }
}
