package org.bgee.model.expressiondata.rawdata.microarray;

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

    /**
     * @param id    A {@code String} that is the ID of the {@code AffymetrixChip}
     * @throws IllegalArgumentException If {@code id} is blank, or {@code experiment} is {@code null}.
     */
    public AffymetrixChip(String id, AffymetrixExperiment experiment, RawDataAnnotation annotation) throws IllegalArgumentException {
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


    //AffymetrixChip IDs are not unique, they are unique inside a given experiment.
    //This is why we reimplement hashCode/equals rather than using the 'Entity' implementation.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((experiment == null) ? 0 : experiment.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
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
        if (experiment == null) {
            if (other.experiment != null)
                return false;
        } else if (!experiment.equals(other.experiment))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
