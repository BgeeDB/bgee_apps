package org.bgee.model.expressiondata.rawdata.microarray;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.AssayPartOfExp;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotation;

public class AffymetrixChip implements AssayPartOfExp<String, AffymetrixExperiment>, RawDataAnnotated {
    private final static Logger log = LogManager.getLogger(AffymetrixChip.class.getName());

    private final String id;
    private final AffymetrixExperiment experiment;
    private final RawDataAnnotation annotation;
    private final AffymetrixChipPipelineSummary pipelineSummary;

    /**
     * @param id    A {@code String} that is the ID of the {@code AffymetrixChip}
     * @throws IllegalArgumentException If {@code id} is blank, or {@code experiment} is {@code null}.
     */
    public AffymetrixChip(String id, AffymetrixExperiment experiment, RawDataAnnotation annotation,
            AffymetrixChipPipelineSummary pipelineSummary) throws IllegalArgumentException {
        if (StringUtils.isBlank(id)) {
            throw log.throwing(new IllegalArgumentException("ID cannot be blank"));
        }
        this.id = id;
        if (experiment == null) {
            throw log.throwing(new IllegalArgumentException("Experiment cannot be null"));
        }
        this.experiment = experiment;
        if (annotation == null) {
            throw log.throwing(new IllegalArgumentException("Annotation cannot be null"));
        }
        this.annotation = annotation;
        this.pipelineSummary = pipelineSummary;
    }

    public String getId() {
        return this.id;
    }
    @Override
    public AffymetrixExperiment getExperiment() {
        return this.experiment;
    }
    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }

    public AffymetrixChipPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }

    //AffymetrixChip IDs are not unique, they are unique inside a given experiment.
    //This is why we reimplement hashCode/equals rather than using the 'Entity' implementation.
    @Override
    public int hashCode() {
        return Objects.hash(annotation, experiment, id, pipelineSummary);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AffymetrixChip other = (AffymetrixChip) obj;
        return Objects.equals(annotation, other.annotation) && Objects.equals(experiment, other.experiment)
                && Objects.equals(id, other.id) && Objects.equals(pipelineSummary, other.pipelineSummary);
    }

    @Override
    public String toString() {
        return "AffymetrixChip [id=" + id + ", experiment=" + experiment + ", annotation=" + annotation
                + ", pipelineSummary=" + pipelineSummary + "]";
    }
}
