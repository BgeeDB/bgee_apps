package org.bgee.model.expressiondata.rawdata.microarray;

import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentWithDataDownload;
import org.bgee.model.source.Source;

public class AffymetrixExperiment extends ExperimentWithDataDownload<String> {

    public AffymetrixExperiment(String id, String name, String description, Source dataSource,
            String downloadUrl, int assayCount)
            throws IllegalArgumentException {
        super(id, name, description, dataSource, downloadUrl, assayCount);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
