package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.baseelements.AssayPartOfExp;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataPipelineSummary;

//Note that in the database, inSituEvidence are not associated with a condition,
//it is at the level of the inSituSpot that it is done.
//But for convenience, we want this class to be associated with conditions,
//so we generally retrieve the spots associated to an evidence, and create
//one object InSituEvidence per evidence and condition.
public class InSituEvidence extends Entity<String> implements AssayPartOfExp<InSituExperiment> {
    private final static Logger log = LogManager.getLogger(InSituEvidence.class.getName());

    private final InSituExperiment experiment;
    private final RawDataAnnotation annotation;
    private final RawDataPipelineSummary pipelineSummary;

    public InSituEvidence(String id, InSituExperiment experiment, RawDataAnnotation annotation,
            RawDataPipelineSummary pipelineSummary)
            throws IllegalArgumentException {
        super(id);
        if (experiment == null) {
            throw log.throwing(new IllegalArgumentException("Experiment cannot be null"));
        }
        this.experiment = experiment;
        this.annotation = annotation;
        this.pipelineSummary = pipelineSummary;
    }

    @Override
    public InSituExperiment getExperiment() {
        return this.experiment;
    }
    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }

    public RawDataPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }

    //InSituEvidence IDs are unique in the Bgee database, but since we create one InSituEvidence
    //per evidence and condition, we need to reimplement hashCode/equals.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(annotation, experiment, pipelineSummary);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InSituEvidence other = (InSituEvidence) obj;
        return Objects.equals(annotation, other.annotation)
                && Objects.equals(experiment, other.experiment)
                && Objects.equals(pipelineSummary, other.pipelineSummary);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InSituEvidence [")
               .append("id=").append(getId())
               .append(", experiment=").append(experiment)
               .append(", annotation=").append(annotation)
               .append(", pipelineSummary=").append(pipelineSummary)
               .append("]");
        return builder.toString();
    }
}
