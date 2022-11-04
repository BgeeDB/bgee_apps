package org.bgee.model.expressiondata.rawdata;

import java.util.Objects;

/**
 * A class allowing to contain count for all potential raw data resulting
 * from a query to {@code RawDataContainerService}
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RawDataCountContainer {

    private final Integer microarrayExperimentCount;
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

    
    // null if not queried and 0 if no results
    public RawDataCountContainer(Integer microarrayExperimentCount, Integer affymetrixAssayCount,
            Integer affymetrixCallsCount, Integer insituExperimentCount, Integer insituAssayCount,
            Integer insituCallsCount, Integer estAssayCount, Integer estCallsCount, Integer bulkRnaSeqExperimentCount,
            Integer bulkRnaSeqAssayCount, Integer bulkRnaSeqCallsCount, Integer singleCellRnaSeqExperimentCount,
            Integer singleCellRnaSeqAssayCount, Integer singleCellRnaSeqLibraryCount,
            Integer singleCellRnaSeqCallsCount) {
        this.microarrayExperimentCount = microarrayExperimentCount;
        this.affymetrixAssayCount = affymetrixAssayCount;
        this.affymetrixCallsCount = affymetrixCallsCount;
        this.insituExperimentCount = insituExperimentCount;
        this.insituAssayCount = insituAssayCount;
        this.insituCallsCount = insituCallsCount;
        this.estAssayCount = estAssayCount;
        this.estCallsCount = estCallsCount;
        this.bulkRnaSeqExperimentCount = bulkRnaSeqExperimentCount;
        this.bulkRnaSeqAssayCount = bulkRnaSeqAssayCount;
        this.bulkRnaSeqCallsCount = bulkRnaSeqCallsCount;
        this.singleCellRnaSeqExperimentCount = singleCellRnaSeqExperimentCount;
        this.singleCellRnaSeqAssayCount = singleCellRnaSeqAssayCount;
        this.singleCellRnaSeqLibraryCount = singleCellRnaSeqLibraryCount;
        this.singleCellRnaSeqCallsCount = singleCellRnaSeqCallsCount;
    }

    public Integer getMicroarrayExperimentCount() {
        return microarrayExperimentCount;
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
    @Override
    public int hashCode() {
        return Objects.hash(affymetrixAssayCount, affymetrixCallsCount, bulkRnaSeqAssayCount, bulkRnaSeqCallsCount,
                bulkRnaSeqExperimentCount, estAssayCount, estCallsCount, insituAssayCount, insituCallsCount,
                insituExperimentCount, microarrayExperimentCount, singleCellRnaSeqAssayCount,
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
                && Objects.equals(microarrayExperimentCount, other.microarrayExperimentCount)
                && Objects.equals(singleCellRnaSeqAssayCount, other.singleCellRnaSeqAssayCount)
                && Objects.equals(singleCellRnaSeqCallsCount, other.singleCellRnaSeqCallsCount)
                && Objects.equals(singleCellRnaSeqExperimentCount, other.singleCellRnaSeqExperimentCount)
                && Objects.equals(singleCellRnaSeqLibraryCount, other.singleCellRnaSeqLibraryCount);
    }
    @Override
    public String toString() {
        return "RawDataCountContainer [microarrayExperimentCount=" + microarrayExperimentCount
                + ", affymetrixAssayCount=" + affymetrixAssayCount + ", affymetrixCallsCount=" + affymetrixCallsCount
                + ", insituExperimentCount=" + insituExperimentCount + ", insituAssayCount=" + insituAssayCount
                + ", insituCallsCount=" + insituCallsCount + ", estAssayCount=" + estAssayCount + ", estCallsCount="
                + estCallsCount + ", bulkRnaSeqExperimentCount=" + bulkRnaSeqExperimentCount + ", bulkRnaSeqAssayCount="
                + bulkRnaSeqAssayCount + ", bulkRnaSeqCallsCount=" + bulkRnaSeqCallsCount
                + ", singleCellRnaSeqExperimentCount=" + singleCellRnaSeqExperimentCount
                + ", singleCellRnaSeqAssayCount=" + singleCellRnaSeqAssayCount + ", singleCellRnaSeqLibraryCount="
                + singleCellRnaSeqLibraryCount + ", singleCellRnaSeqCallsCount=" + singleCellRnaSeqCallsCount + "]";
    }

    
}
