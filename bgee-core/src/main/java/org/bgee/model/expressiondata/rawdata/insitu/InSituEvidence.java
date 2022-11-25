package org.bgee.model.expressiondata.rawdata.insitu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.baseelements.AssayPartOfExp;

public class InSituEvidence extends Entity<String> implements AssayPartOfExp<InSituExperiment> {
    private final static Logger log = LogManager.getLogger(InSituEvidence.class.getName());

    private final InSituExperiment experiment;

    public InSituEvidence(String id, InSituExperiment experiment) throws IllegalArgumentException {
        super(id);
        if (experiment == null) {
            throw log.throwing(new IllegalArgumentException("Experiment cannot be null"));
        }
        this.experiment = experiment;
    }

    @Override
    public InSituExperiment getExperiment() {
        return this.experiment;
    }

    //InSituEvidence IDs are unique in the Bgee database, so we don't need to reimplement hashCode/equals,
    //we rely on the implementation from the 'Entity' class inherited from the 'Assay' class.
}
