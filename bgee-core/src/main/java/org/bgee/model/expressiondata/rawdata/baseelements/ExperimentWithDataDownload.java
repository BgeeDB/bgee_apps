package org.bgee.model.expressiondata.rawdata.baseelements;

import org.bgee.model.source.Source;

public abstract class ExperimentWithDataDownload<T extends Comparable<T>> extends Experiment<T> {

    private final String downloadUrl;

    protected ExperimentWithDataDownload(T id, String name, String description, Source dataSource,
            String downloadUrl, int assayCount) {
        this(id, id, name, description, dataSource, downloadUrl, assayCount);
    }
    protected ExperimentWithDataDownload(T id, T xRefId, String name, String description, Source dataSource,
            String downloadUrl, int assayCount) {
        super(id, xRefId, name, description, dataSource, assayCount);
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Experiment [")
               .append("dataSource=").append(getDataSource())
               .append(", xRef=").append(getXRef())
               .append(", downloadUrl=").append(downloadUrl)
               .append(", assayCount=").append(getAssayCount())
               .append("]");
        return builder.toString();
    }
}
