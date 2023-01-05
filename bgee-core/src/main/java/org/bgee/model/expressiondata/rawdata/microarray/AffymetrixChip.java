package org.bgee.model.expressiondata.rawdata.microarray;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.AssayPartOfExp;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;

//AffymetrixChip IDs are not unique, they are unique inside a given experiment.
//This is why this class does not extend Entity.
public class AffymetrixChip implements AssayPartOfExp<AffymetrixExperiment>, RawDataAnnotated {
    private final static Logger log = LogManager.getLogger(AffymetrixChip.class.getName());

    private final String id;
    private final AffymetrixExperiment experiment;
    private final RawDataAnnotation annotation;
    private final ChipType chipType;
    private final AffymetrixChipPipelineSummary pipelineSummary;

    /**
     * @param id    A {@code String} that is the ID of the {@code AffymetrixChip}
     * @throws IllegalArgumentException If {@code id} is blank, or {@code experiment} is {@code null}.
     */
    public AffymetrixChip(String id, AffymetrixExperiment experiment, RawDataAnnotation annotation,
            ChipType chipType, AffymetrixChipPipelineSummary pipelineSummary)
                    throws IllegalArgumentException {
        if (StringUtils.isBlank(id)) {
            throw log.throwing(new IllegalArgumentException("ID cannot be blank"));
        }
        this.id = id;
        if (experiment == null) {
            throw log.throwing(new IllegalArgumentException("Experiment cannot be null"));
        }
        this.experiment = experiment;
        this.annotation = annotation;
        this.chipType = chipType;
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

    public ChipType getChipType() {
        return chipType;
    }

    public AffymetrixChipPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }

    //AffymetrixChip IDs are not unique, they are unique inside a given experiment.
    //We use the ID and the experiment as primary key
    @Override
    public int hashCode() {
        return Objects.hash(experiment, id);
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
        return Objects.equals(experiment, other.experiment) && Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AffymetrixChip [")
               .append("id=").append(id)
               .append(", experiment=").append(experiment)
               .append(", annotation=").append(annotation)
               .append(", chipType=").append(chipType)
               .append(", pipelineSummary=").append(pipelineSummary)
               .append("]");
        return builder.toString();
    }
}