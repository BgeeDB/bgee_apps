package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.Entity;

public class RnaSeqLibrary extends Entity<String>{
    
    private final RnaSeqTechnology technology;
    private final RnaSeqLibraryPipelineSummary pipelineSummary;
    private final RnaSeqExperiment experiment;

    public RnaSeqLibrary(String id) throws IllegalArgumentException {
        super(id);
        technology = null;
        experiment = null;
        pipelineSummary = null;
    }
    
    public RnaSeqLibrary(String id, RnaSeqTechnology technology, 
            RnaSeqLibraryPipelineSummary pipelineSummary, RnaSeqExperiment experiment)
            throws IllegalArgumentException {
        super(id);
        if (experiment == null) {
            throw new IllegalArgumentException("An experiment must be provided");
        }
        this.technology = technology;
        this.pipelineSummary = pipelineSummary;
        this.experiment = experiment;
    }
    public RnaSeqTechnology getTechnology() {
        return technology;
    }
    public RnaSeqLibraryPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }
    public RnaSeqExperiment getExperiment() {
        return experiment;
    }

    @Override
    public String toString() {
        return "RnaSeqLibrary [technology=" + technology + ", pipelineSummary="
                + pipelineSummary + ", experiment=" + experiment + ", getId()=" + getId() + "]";
    }
    
}