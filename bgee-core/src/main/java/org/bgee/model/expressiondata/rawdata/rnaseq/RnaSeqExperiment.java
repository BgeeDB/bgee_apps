package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.List;

import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentWithDataDownload;
import org.bgee.model.file.ExperimentDownloadFile;
import org.bgee.model.source.Source;

public class RnaSeqExperiment extends ExperimentWithDataDownload<String>{

    private final boolean sampleMultiplexing;
    private final Integer numberOfAnnotatedCells;

    public RnaSeqExperiment(String id, String name, String description, String dOI, Source dataSource,
            List<ExperimentDownloadFile> downloadFiles, int assayCount, boolean sampleMultiplexing,
            Integer numberOfAnnotatedCells) {
        super(id, name, description, dOI, dataSource, downloadFiles, assayCount);
        this.sampleMultiplexing = sampleMultiplexing;
        this.numberOfAnnotatedCells = numberOfAnnotatedCells;
    }

    public boolean isSampleMultiplexing() {
        return sampleMultiplexing;
    }

    public Integer getNumberOfAnnotatedCells() {
        return numberOfAnnotatedCells;
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
