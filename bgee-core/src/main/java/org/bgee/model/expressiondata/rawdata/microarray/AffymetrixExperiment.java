package org.bgee.model.expressiondata.rawdata.microarray;

import java.util.List;

import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentWithDataDownload;
import org.bgee.model.file.ExperimentDownloadFile;
import org.bgee.model.source.Source;

public class AffymetrixExperiment extends ExperimentWithDataDownload<String> {

    public AffymetrixExperiment(String id, String name, String description, Source dataSource,
            List<ExperimentDownloadFile> downloadFiles, int assayCount)
            throws IllegalArgumentException {
        //DOI is set to null as it is not yet provided for in affymetrix data
        super(id, name, description, null, dataSource, downloadFiles, assayCount);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
