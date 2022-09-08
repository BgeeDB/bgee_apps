package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.Entity;

public class RnaSeqLibrary extends Entity<String>{
    
    private final RnaSeqTechnology technology;
    private final RnaSeqExperiment experiment;

    public RnaSeqLibrary(String id) throws IllegalArgumentException {
        super(id);
        technology = null;
        experiment = null;
    }
    
    public RnaSeqLibrary(String id, RnaSeqTechnology technology, RnaSeqExperiment experiment)
            throws IllegalArgumentException {
        super(id);
        this.technology = technology;
        this.experiment = experiment;
    }
    public RnaSeqTechnology getTechnology() {
        return technology;
    }
    public RnaSeqExperiment getExperiment() {
        return experiment;
    }

}
