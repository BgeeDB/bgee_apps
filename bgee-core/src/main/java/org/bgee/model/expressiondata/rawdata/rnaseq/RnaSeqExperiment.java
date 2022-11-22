package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.source.Source;

public class RnaSeqExperiment extends Experiment<String>{

    public RnaSeqExperiment(String id, String name, String description, Source dataSource,
            int assayCount) {
        super(id, name, description, dataSource, assayCount);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
