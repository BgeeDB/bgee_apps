package org.bgee.model.expressiondata.rawdata.insitu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCall;
import org.bgee.model.expressiondata.rawdata.baseelements.RawCallSource;


//Note that in the database, inSituSpot are associated with a condition.
//But for convenience, we want InSituEvidence to be associated with conditions,
//not the InSituSpot.
public class InSituSpot extends Entity<String> implements RawCallSource<InSituEvidence> {
    private final static Logger log = LogManager.getLogger(InSituSpot.class.getName());

    private final InSituEvidence assay;
    private final RawCall rawCall;

    public InSituSpot(String id, InSituEvidence assay, RawCall rawCall)
            throws IllegalArgumentException {
        super(id);
        if (assay == null) {
            throw log.throwing(new IllegalArgumentException("InSituEvidence cannot be null"));
        }
        this.assay = assay;
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

    //InSituSpot IDs are unique in the Bgee database, so we don't need to reimplement hashCode/equals,
    //we rely on the implementation from the 'Entity' class.
}
