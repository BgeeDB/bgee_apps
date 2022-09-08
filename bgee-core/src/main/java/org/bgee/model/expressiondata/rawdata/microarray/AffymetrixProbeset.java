package org.bgee.model.expressiondata.rawdata.microarray;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.RawCall;
import org.bgee.model.expressiondata.rawdata.RawCallSource;

public class AffymetrixProbeset implements RawCallSource<AffymetrixChip> {
    private final static Logger log = LogManager.getLogger(AffymetrixProbeset.class.getName());

    private final String id;
    private final AffymetrixChip assay;
    private final RawCall rawCall;
    private final BigDecimal normalizedSignalIntensity;
    private final BigDecimal qValue;
    private final BigDecimal rank;

    public AffymetrixProbeset(String id, AffymetrixChip assay, RawCall rawCall,
            BigDecimal normalizedSignalIntensity, BigDecimal qValue, BigDecimal rank) {
        if (StringUtils.isBlank(id)) {
            throw log.throwing(new IllegalArgumentException("ID cannot be blank"));
        }
        this.id = id;
        if (rawCall == null) {
            throw log.throwing(new IllegalArgumentException("RawCall cannot be null"));
        }
        this.rawCall = rawCall;
        this.assay = assay;
        this.normalizedSignalIntensity = normalizedSignalIntensity;
        this.qValue = qValue;
        this.rank = rank;
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
    public BigDecimal getNormalizedSignalIntensity() {
        return normalizedSignalIntensity;
    }
    public BigDecimal getqValue() {
        return qValue;
    }
    public BigDecimal getRank() {
        return rank;
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

    @Override
    public String toString() {
        return "AffymetrixProbeset [id=" + id + ", assay=" + assay + ", rawCall=" + rawCall
                + ", normalizedSignalIntensity=" + normalizedSignalIntensity + ", qValue=" + qValue + ", rank=" + rank
                + "]";
    }
    
    
}
