package org.bgee.model.expressiondata.rawdata.microarray;

import org.bgee.model.expressiondata.rawdata.Experiment;
import org.bgee.model.source.Source;

public class AffymetrixExperiment extends Experiment<String> {

    public AffymetrixExperiment(String id, String name, String description, Source dataSource)
            throws IllegalArgumentException {
        super(id, name, description, dataSource);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
