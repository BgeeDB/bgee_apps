package org.bgee.model.expressiondata.rawdata.microarray;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.RawCall;
import org.bgee.model.expressiondata.rawdata.RawCallSource;

public class AffymetrixProbeset implements RawCallSource<AffymetrixChip> {
    private final static Logger log = LogManager.getLogger(AffymetrixProbeset.class.getName());

    private final String id;
    private final AffymetrixChip assay;
    private final RawCall rawCall;

    public AffymetrixProbeset(String id, AffymetrixChip assay, RawCall rawCall) {
        if (StringUtils.isBlank(id)) {
            throw log.throwing(new IllegalArgumentException("ID cannot be blank"));
        }
        this.id = id;
        if (assay == null) {
            throw log.throwing(new IllegalArgumentException("AffymetrixChip cannot be null"));
        }
        this.assay = assay;
        if (rawCall == null) {
            throw log.throwing(new IllegalArgumentException("RawCall cannot be null"));
        }
        this.rawCall = rawCall;
    }

    public String getId() {
        return this.id;
    }
    @Override
    public AffymetrixChip getAssay() {
        return this.assay;
    }
    @Override
    public RawCall getRawCall() {
        return this.rawCall;
    }

    //AffymetrixProbeset IDs are not unique, they are unique inside a given AffymetrixChip.
    //This is why we reimplement hashCode/equals rather than using the 'Entity' implementation.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assay == null) ? 0 : assay.hashCode());
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
        AffymetrixProbeset other = (AffymetrixProbeset) obj;
        if (assay == null) {
            if (other.assay != null)
                return false;
        } else if (!assay.equals(other.assay))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
