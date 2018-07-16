package org.bgee.model.expressiondata.rawdata.affymetrix;

import org.bgee.model.expressiondata.rawdata.Experiment;

public class AffymetrixExperiment extends Experiment<String> {

    /**
     * 
     * @param id
     * @param name
     * @param description
     * @throws IllegalArgumentException If {@code id} is blank
     */
    public AffymetrixExperiment(String id, String name, String description)
            throws IllegalArgumentException {
        super(id, name, description);
    }

    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
