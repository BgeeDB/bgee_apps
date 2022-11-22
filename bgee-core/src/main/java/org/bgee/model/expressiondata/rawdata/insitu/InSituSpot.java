package org.bgee.model.expressiondata.rawdata.insitu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCallSource;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;

public class InSituSpot extends Entity<String> implements RawCallSource<InSituEvidence>, RawDataAnnotated {
    private final static Logger log = LogManager.getLogger(InSituSpot.class.getName());

    private final InSituEvidence assay;
    private final RawCall rawCall;
    private final RawDataAnnotation annotation;

    public InSituSpot(String id, InSituEvidence assay, RawDataAnnotation annotation, RawCall rawCall)
            throws IllegalArgumentException {
        super(id);
        if (assay == null) {
            throw log.throwing(new IllegalArgumentException("InSituEvidence cannot be null"));
        }
        this.assay = assay;
        if (annotation == null) {
            throw log.throwing(new IllegalArgumentException("RawDataAnnotation cannot be null"));
        }
        this.annotation = annotation;
        if (rawCall == null) {
            throw log.throwing(new IllegalArgumentException("RawCall cannot be null"));
        }
        this.rawCall = rawCall;
    }

    @Override
    public InSituEvidence getAssay() {
        return this.assay;
    }
    @Override
    public RawCall getRawCall() {
        return this.rawCall;
    }
    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }

    //InSituSpot IDs are unique in the Bgee database, so we don't need to reimplement hashCode/equals,
    //we rely on the implementation from the 'Entity' class.
}
