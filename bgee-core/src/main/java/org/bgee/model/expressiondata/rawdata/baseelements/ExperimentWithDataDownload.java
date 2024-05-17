package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.List;

import org.bgee.model.file.ExperimentDownloadFile;
import org.bgee.model.source.Source;

public abstract class ExperimentWithDataDownload<T extends Comparable<T>> extends Experiment<T> {

    private final List<ExperimentDownloadFile> downloadFiles;

    protected ExperimentWithDataDownload(T id, String name, String description, String dOI, Source dataSource,
            List<ExperimentDownloadFile> downloadFiles, int assayCount) {
        this(id, id, name, description, dOI, dataSource, downloadFiles, assayCount);
    }
    protected ExperimentWithDataDownload(T id, T xRefId, String name, String description, String dOI, Source dataSource,
            List<ExperimentDownloadFile> downloadFiles, int assayCount) {
        super(id, xRefId, name, description, dOI, dataSource, assayCount);
        this.downloadFiles = downloadFiles == null? List.of(): List.copyOf(downloadFiles);
    }

    public List<ExperimentDownloadFile> getDownloadFiles() {
        return downloadFiles;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Experiment [")
               .append("dataSource=").append(getDataSource())
               .append(", xRef=").append(getXRef())
               .append(", downloadFiles=").append(downloadFiles)
               .append(", assayCount=").append(getAssayCount())
               .append("]");
        return builder.toString();
    }
}
