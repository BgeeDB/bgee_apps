package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.Experiment;
import org.bgee.model.source.Source;

public class InSituExperiment extends Experiment<String> {

    public InSituExperiment(String id, String name, String description, Source dataSource) {
        super(id, name, description, dataSource);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
