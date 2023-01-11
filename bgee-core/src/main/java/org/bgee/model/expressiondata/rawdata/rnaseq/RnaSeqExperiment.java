package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentWithDataDownload;
import org.bgee.model.source.Source;

public class RnaSeqExperiment extends ExperimentWithDataDownload<String>{

    public RnaSeqExperiment(String id, String name, String description, Source dataSource,
            String downloadUrl, int assayCount) {
        super(id, name, description, dataSource, downloadUrl, assayCount);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
