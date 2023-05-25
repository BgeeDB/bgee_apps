package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentWithDataDownload;
import org.bgee.model.source.Source;

public class RnaSeqExperiment extends ExperimentWithDataDownload<String>{

    private final boolean isTargetBase;

    public RnaSeqExperiment(String id, String name, String description, Source dataSource,
            String downloadUrl, int assayCount, boolean isTargetBase) {
        super(id, name, description, dataSource, downloadUrl, assayCount);
        this.isTargetBase = isTargetBase;
    }

    public boolean isTargetBase() {
        return isTargetBase;
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
