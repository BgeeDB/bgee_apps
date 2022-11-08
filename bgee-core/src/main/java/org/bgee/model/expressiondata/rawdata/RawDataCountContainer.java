package org.bgee.model.expressiondata.rawdata;

import java.util.EnumSet;
import java.util.Objects;

import org.bgee.model.expressiondata.baseelements.DataType;

/**
 * A class allowing to contain count for all potential raw data resulting
 * from a query to {@code RawDataContainerService}
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RawDataCountContainer {

    private final Integer affymetrixExperimentCount;
    private final Integer affymetrixAssayCount;
    private final Integer affymetrixCallsCount;
    private final Integer insituExperimentCount;
    private final Integer insituAssayCount;
    private final Integer insituCallsCount;
    private final Integer estAssayCount;
    private final Integer estCallsCount;
    private final Integer bulkRnaSeqExperimentCount;
    private final Integer bulkRnaSeqAssayCount;
    private final Integer bulkRnaSeqCallsCount;
    private final Integer singleCellRnaSeqExperimentCount;
    private final Integer singleCellRnaSeqAssayCount;
    private final Integer singleCellRnaSeqLibraryCount;
    private final Integer singleCellRnaSeqCallsCount;

    //Not used in equals/hashCode since it is derived from the other fields
    private final EnumSet<DataType> requestedDataTypes;
    
    // null if not queried and 0 if no results
    //FIXME: we also need bulkRnaSeqLibraryCount (example: BRB-Seq data)
    public RawDataCountContainer(Integer affymetrixExperimentCount, Integer affymetrixAssayCount,
            Integer affymetrixCallsCount, Integer insituExperimentCount, Integer insituAssayCount,
            Integer insituCallsCount, Integer estAssayCount, Integer estCallsCount, Integer bulkRnaSeqExperimentCount,
            Integer bulkRnaSeqAssayCount, Integer bulkRnaSeqCallsCount, Integer singleCellRnaSeqExperimentCount,
            Integer singleCellRnaSeqAssayCount, Integer singleCellRnaSeqLibraryCount,
            Integer singleCellRnaSeqCallsCount) {
        EnumSet<DataType> dataTypes = EnumSet.noneOf(DataType.class);

        this.affymetrixExperimentCount = affymetrixExperimentCount;
        this.affymetrixAssayCount = affymetrixAssayCount;
        this.affymetrixCallsCount = affymetrixCallsCount;
        if (this.affymetrixExperimentCount != null ||
                this.affymetrixAssayCount != null ||
                this.affymetrixCallsCount != null) {
            dataTypes.add(DataType.AFFYMETRIX);
        }

        this.insituExperimentCount = insituExperimentCount;
        this.insituAssayCount = insituAssayCount;
        this.insituCallsCount = insituCallsCount;
        if (this.insituExperimentCount != null ||
                this.insituAssayCount != null ||
                this.insituCallsCount != null) {
            dataTypes.add(DataType.IN_SITU);
        }

        this.estAssayCount = estAssayCount;
        this.estCallsCount = estCallsCount;
        if (this.estAssayCount != null ||
                this.estCallsCount != null) {
            dataTypes.add(DataType.EST);
        }

        this.bulkRnaSeqExperimentCount = bulkRnaSeqExperimentCount;
        this.bulkRnaSeqAssayCount = bulkRnaSeqAssayCount;
        this.bulkRnaSeqCallsCount = bulkRnaSeqCallsCount;
        if (this.bulkRnaSeqExperimentCount != null ||
                this.bulkRnaSeqAssayCount != null ||
                this.bulkRnaSeqCallsCount != null) {
            dataTypes.add(DataType.RNA_SEQ);
        }

        this.singleCellRnaSeqExperimentCount = singleCellRnaSeqExperimentCount;
        this.singleCellRnaSeqAssayCount = singleCellRnaSeqAssayCount;
        this.singleCellRnaSeqLibraryCount = singleCellRnaSeqLibraryCount;
        this.singleCellRnaSeqCallsCount = singleCellRnaSeqCallsCount;
        if (this.singleCellRnaSeqExperimentCount != null ||
                this.singleCellRnaSeqAssayCount != null ||
                this.singleCellRnaSeqLibraryCount != null ||
                this.singleCellRnaSeqCallsCount != null) {
            dataTypes.add(DataType.FULL_LENGTH);
        }

        this.requestedDataTypes = dataTypes;
    }

    public Integer getAffymetrixExperimentCount() {
        return affymetrixExperimentCount;
    }
    public Integer getAffymetrixAssayCount() {
        return affymetrixAssayCount;
    }
    public Integer getAffymetrixCallsCount() {
        return affymetrixCallsCount;
    }
    public Integer getInsituExperimentCount() {
        return insituExperimentCount;
    }
    public Integer getInsituAssayCount() {
        return insituAssayCount;
    }
    public Integer getInsituCallsCount() {
        return insituCallsCount;
    }
    public Integer getEstAssayCount() {
        return estAssayCount;
    }
    public Integer getEstCallsCount() {
        return estCallsCount;
    }
    public Integer getBulkRnaSeqExperimentCount() {
        return bulkRnaSeqExperimentCount;
    }
    public Integer getBulkRnaSeqAssayCount() {
        return bulkRnaSeqAssayCount;
    }
    public Integer getBulkRnaSeqCallsCount() {
        return bulkRnaSeqCallsCount;
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
    public Integer getSingleCellRnaSeqCallsCount() {
        return singleCellRnaSeqCallsCount;
    }

    public EnumSet<DataType> getRequestedDataTypes() {
        return requestedDataTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(affymetrixAssayCount, affymetrixCallsCount, bulkRnaSeqAssayCount, bulkRnaSeqCallsCount,
                bulkRnaSeqExperimentCount, estAssayCount, estCallsCount, insituAssayCount, insituCallsCount,
                insituExperimentCount, affymetrixExperimentCount, singleCellRnaSeqAssayCount,
                singleCellRnaSeqCallsCount, singleCellRnaSeqExperimentCount, singleCellRnaSeqLibraryCount);
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
                && Objects.equals(affymetrixCallsCount, other.affymetrixCallsCount)
                && Objects.equals(bulkRnaSeqAssayCount, other.bulkRnaSeqAssayCount)
                && Objects.equals(bulkRnaSeqCallsCount, other.bulkRnaSeqCallsCount)
                && Objects.equals(bulkRnaSeqExperimentCount, other.bulkRnaSeqExperimentCount)
                && Objects.equals(estAssayCount, other.estAssayCount)
                && Objects.equals(estCallsCount, other.estCallsCount)
                && Objects.equals(insituAssayCount, other.insituAssayCount)
                && Objects.equals(insituCallsCount, other.insituCallsCount)
                && Objects.equals(insituExperimentCount, other.insituExperimentCount)
                && Objects.equals(affymetrixExperimentCount, other.affymetrixExperimentCount)
                && Objects.equals(singleCellRnaSeqAssayCount, other.singleCellRnaSeqAssayCount)
                && Objects.equals(singleCellRnaSeqCallsCount, other.singleCellRnaSeqCallsCount)
                && Objects.equals(singleCellRnaSeqExperimentCount, other.singleCellRnaSeqExperimentCount)
                && Objects.equals(singleCellRnaSeqLibraryCount, other.singleCellRnaSeqLibraryCount);
    }

    @Override
    public String toString() {
        return "RawDataCountContainer [affymetrixExperimentCount=" + affymetrixExperimentCount
                + ", affymetrixAssayCount=" + affymetrixAssayCount + ", affymetrixCallsCount=" + affymetrixCallsCount
                + ", insituExperimentCount=" + insituExperimentCount + ", insituAssayCount=" + insituAssayCount
                + ", insituCallsCount=" + insituCallsCount + ", estAssayCount=" + estAssayCount + ", estCallsCount="
                + estCallsCount + ", bulkRnaSeqExperimentCount=" + bulkRnaSeqExperimentCount + ", bulkRnaSeqAssayCount="
                + bulkRnaSeqAssayCount + ", bulkRnaSeqCallsCount=" + bulkRnaSeqCallsCount
                + ", singleCellRnaSeqExperimentCount=" + singleCellRnaSeqExperimentCount
                + ", singleCellRnaSeqAssayCount=" + singleCellRnaSeqAssayCount + ", singleCellRnaSeqLibraryCount="
                + singleCellRnaSeqLibraryCount + ", singleCellRnaSeqCallsCount=" + singleCellRnaSeqCallsCount
                + ", requestedDataTypes=" + requestedDataTypes + "]";
    }
}
