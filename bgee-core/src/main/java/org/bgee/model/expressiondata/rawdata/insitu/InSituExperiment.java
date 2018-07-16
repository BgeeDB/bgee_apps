package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.Experiment;

public class InSituExperiment extends Experiment<String> {

    public InSituExperiment(String id, String name, String description) {
        super(id, name, description);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
